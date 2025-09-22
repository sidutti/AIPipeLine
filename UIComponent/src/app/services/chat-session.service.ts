import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { ChatMessage, ChatSession } from '../models/chat.models';
import { JavaApiService } from './java-api.service';

@Injectable({
  providedIn: 'root'
})
export class ChatSessionService {
  private currentSession = new BehaviorSubject<ChatSession | null>(null);
  private messages = new BehaviorSubject<ChatMessage[]>([]);
  private typing = new BehaviorSubject<boolean>(false);
  private sessionStatus = new BehaviorSubject<'idle' | 'active' | 'waiting' | 'ended'>('idle');
  private newMessage = new Subject<ChatMessage>();

  constructor(private javaApiService: JavaApiService) {
    this.loadSession();
  }

  getCurrentSession(): Observable<ChatSession | null> {
    return this.currentSession.asObservable();
  }

  getMessages(): Observable<ChatMessage[]> {
    return this.messages.asObservable();
  }

  getTypingStatus(): Observable<boolean> {
    return this.typing.asObservable();
  }

  getSessionStatus(): Observable<'idle' | 'active' | 'waiting' | 'ended'> {
    return this.sessionStatus.asObservable();
  }

  getNewMessage(): Observable<ChatMessage> {
    return this.newMessage.asObservable();
  }

  async startNewSession(studentName: string, subject: 'math' | 'physics', grade: number): Promise<void> {
    try {
      this.sessionStatus.next('waiting');

      const session = await this.javaApiService.createSession(studentName, subject, grade).toPromise();
      if (session) {
        this.currentSession.next(session);
        this.messages.next([]);
        this.sessionStatus.next('active');

        this.saveSession(session);

        // Send welcome message
        await this.sendWelcomeMessage(studentName, subject);

        // Get first question
        setTimeout(() => {
          this.requestNextQuestion();
        }, 2000);
      }
    } catch (error) {
      console.error('Failed to start session:', error);
      this.sessionStatus.next('idle');
      this.addSystemMessage('Failed to connect to the tutoring system. Please try again.');
    }
  }

  async sendMessage(messageText: string): Promise<void> {
    const session = this.currentSession.value;
    if (!session) return;

    // Add user message
    const userMessage: ChatMessage = {
      id: this.generateId(),
      sessionId: session.id,
      message: messageText,
      isUser: true,
      timestamp: new Date(),
      subject: session.subject
    };

    this.addMessage(userMessage);
    this.sessionStatus.next('waiting');
    this.typing.next(true);

    try {
      // Check if this is an answer to a question
      const lastBotMessage = this.getLastBotMessage();
      if (lastBotMessage && this.isQuestion(lastBotMessage.message)) {
        await this.handleAnswer(messageText, session);
      } else {
        // General chat or request for help
        await this.handleGeneralMessage(messageText, session);
      }
    } catch (error) {
      console.error('Failed to process message:', error);
      this.addSystemMessage('Sorry, I had trouble processing your message. Please try again.');
    } finally {
      this.typing.next(false);
      this.sessionStatus.next('active');
    }
  }

  async requestNextQuestion(): Promise<void> {
    const session = this.currentSession.value;
    if (!session) return;

    this.typing.next(true);
    this.sessionStatus.next('waiting');

    try {
      const previousQuestions = this.getPreviousQuestions();
      const questionResponse = await this.javaApiService.getQuestion({
        sessionId: session.id,
        subject: session.subject,
        grade: session.grade,
        previousQuestions
      }).toPromise();

      if (questionResponse) {
        let questionText = questionResponse.question;

        if (questionResponse.questionType === 'multiple-choice' && questionResponse.options) {
          questionText += '\n\nOptions:\n' +
            questionResponse.options.map((opt, i) => `${String.fromCharCode(65 + i)}) ${opt}`).join('\n');
        }

        const botMessage: ChatMessage = {
          id: questionResponse.questionId,
          sessionId: session.id,
          message: questionText,
          isUser: false,
          timestamp: new Date(),
          subject: session.subject,
          questionType: questionResponse.questionType,
          difficulty: questionResponse.difficulty
        };

        this.addMessage(botMessage);
      }
    } catch (error) {
      console.error('Failed to get question:', error);
      this.addSystemMessage('Sorry, I had trouble getting the next question. Please try again.');
    } finally {
      this.typing.next(false);
      this.sessionStatus.next('active');
    }
  }

  async requestHint(): Promise<void> {
    const session = this.currentSession.value;
    if (!session) return;

    const lastQuestion = this.getLastBotMessage();
    if (!lastQuestion) return;

    this.typing.next(true);

    try {
      const hintResponse = await this.javaApiService.getHint(lastQuestion.id, session.id).toPromise();
      if (hintResponse) {
        this.addSystemMessage(`💡 Hint: ${hintResponse.hint}`);
      }
    } catch (error) {
      this.addSystemMessage('Sorry, I couldn\'t get a hint for this question right now.');
    } finally {
      this.typing.next(false);
    }
  }

  async endSession(): Promise<void> {
    const session = this.currentSession.value;
    if (!session) return;

    this.sessionStatus.next('waiting');

    try {
      const summary = await this.javaApiService.endSession(session.id).toPromise();
      if (summary) {
        this.addSystemMessage(
          `🎉 Session Complete!\n\n` +
          `Score: ${summary.score}%\n` +
          `Questions: ${summary.correctAnswers}/${summary.totalQuestions}\n\n` +
          `Strengths: ${summary.strengths.join(', ')}\n` +
          `Areas to improve: ${summary.weaknesses.join(', ')}`
        );
      }

      this.sessionStatus.next('ended');
      this.clearSession();
    } catch (error) {
      console.error('Failed to end session:', error);
      this.addSystemMessage('There was an issue ending the session, but your progress has been saved.');
    }
  }

  pauseSession(): void {
    const session = this.currentSession.value;
    if (session) {
      this.javaApiService.pauseSession(session.id).subscribe();
      this.addSystemMessage('Session paused. You can resume anytime!');
      this.sessionStatus.next('idle');
    }
  }

  private async sendWelcomeMessage(studentName: string, subject: string): Promise<void> {
    const welcomeMessage =
      `Hi ${studentName}! 👋 Welcome to your ${subject} tutoring session!\n\n` +
      `I'm here to help you learn 9th grade ${subject} through interactive questions and explanations. ` +
      `Let's start with some questions to see what you know!\n\n` +
      `You can:\n` +
      `• Answer questions I give you\n` +
      `• Ask for hints by typing "hint"\n` +
      `• Request explanations\n` +
      `• Ask me to slow down or speed up\n\n` +
      `Ready? Let's begin! 📚`;

    this.addSystemMessage(welcomeMessage);
  }

  private async handleAnswer(answer: string, session: ChatSession): Promise<void> {
    const lastQuestion = this.getLastBotMessage();
    if (!lastQuestion) return;

    const answerResponse = await this.javaApiService.submitAnswer({
      sessionId: session.id,
      questionId: lastQuestion.id,
      userAnswer: answer,
      timeSpent: this.calculateTimeSpent(lastQuestion.timestamp)
    }).toPromise();

    if (answerResponse) {
      const feedback = answerResponse.isCorrect ? '✅ Correct!' : '❌ Not quite right.';
      let responseMessage = `${feedback}\n\n${answerResponse.explanation}`;

      if (answerResponse.feedback) {
        responseMessage += `\n\n💬 ${answerResponse.feedback}`;
      }

      this.addSystemMessage(responseMessage);

      // Get next question if available
      if (answerResponse.nextQuestionAvailable) {
        setTimeout(() => {
          this.requestNextQuestion();
        }, 3000);
      } else {
        setTimeout(() => {
          this.endSession();
        }, 2000);
      }
    }
  }

  private async handleGeneralMessage(message: string, session: ChatSession): Promise<void> {
    const lowerMessage = message.toLowerCase();

    if (lowerMessage.includes('hint')) {
      await this.requestHint();
    } else if (lowerMessage.includes('next') || lowerMessage.includes('skip')) {
      await this.requestNextQuestion();
    } else if (lowerMessage.includes('help')) {
      this.addSystemMessage(
        'I can help you with:\n' +
        '• Answering math and physics questions\n' +
        '• Providing hints (just type "hint")\n' +
        '• Explaining concepts\n' +
        '• Moving to the next question (type "next")\n\n' +
        'What would you like to do?'
      );
    } else {
      this.addSystemMessage(
        'I understand you want to chat! However, I work best when you answer the questions I give you. ' +
        'Would you like me to ask you a question, or do you need help with something specific?'
      );
    }
  }

  private addMessage(message: ChatMessage): void {
    const currentMessages = this.messages.value;
    const updatedMessages = [...currentMessages, message];
    this.messages.next(updatedMessages);
    this.newMessage.next(message);
  }

  private addSystemMessage(text: string): void {
    const session = this.currentSession.value;
    const message: ChatMessage = {
      id: this.generateId(),
      sessionId: session?.id || 'system',
      message: text,
      isUser: false,
      timestamp: new Date(),
      subject: session?.subject
    };
    this.addMessage(message);
  }

  private getLastBotMessage(): ChatMessage | null {
    const messages = this.messages.value;
    for (let i = messages.length - 1; i >= 0; i--) {
      if (!messages[i].isUser) {
        return messages[i];
      }
    }
    return null;
  }

  private isQuestion(message: string): boolean {
    return message.includes('?') || message.includes('Options:') || message.includes('Calculate') || message.includes('Solve');
  }

  private getPreviousQuestions(): string[] {
    const messages = this.messages.value;
    return messages
      .filter(m => !m.isUser && this.isQuestion(m.message))
      .map(m => m.message);
  }

  private calculateTimeSpent(questionTime: Date): number {
    return Math.floor((new Date().getTime() - questionTime.getTime()) / 1000);
  }

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9) + Date.now().toString(36);
  }

  private saveSession(session: ChatSession): void {
    localStorage.setItem('currentChatSession', JSON.stringify(session));
  }

  private loadSession(): void {
    const saved = localStorage.getItem('currentChatSession');
    if (saved) {
      try {
        const session = JSON.parse(saved);
        this.currentSession.next(session);
      } catch (error) {
        console.error('Failed to load saved session:', error);
      }
    }
  }

  private clearSession(): void {
    localStorage.removeItem('currentChatSession');
    this.currentSession.next(null);
  }
}