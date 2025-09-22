import { Component, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ChatSessionService } from '../../services/chat-session.service';
import { ChatMessage, ChatSession } from '../../models/chat.models';

@Component({
  selector: 'app-chat-interface',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="chat-container">
      <!-- Header -->
      <div class="chat-header">
        <div class="session-info" *ngIf="currentSession">
          <div class="student-avatar">{{getInitials(currentSession.studentName)}}</div>
          <div class="session-details">
            <h3>{{currentSession.studentName}}</h3>
            <p>{{currentSession.subject | titlecase}} - Grade {{currentSession.grade}}</p>
          </div>
        </div>

        <div class="session-status">
          <div class="status-indicator" [class]="sessionStatus"></div>
          <span class="status-text">{{getStatusText()}}</span>
        </div>

        <div class="header-actions">
          <button class="action-btn" (click)="requestHint()" [disabled]="!canRequestHint()" title="Get Hint">
            💡
          </button>
          <button class="action-btn" (click)="pauseSession()" [disabled]="sessionStatus !== 'active'" title="Pause Session">
            ⏸️
          </button>
          <button class="action-btn" (click)="endSession()" [disabled]="sessionStatus === 'ended'" title="End Session">
            🔚
          </button>
          <button class="action-btn" (click)="goHome()" title="Home">
            🏠
          </button>
        </div>
      </div>

      <!-- Messages Area -->
      <div class="messages-container" #messagesContainer>
        <div class="messages-list">
          <!-- Welcome message when no messages -->
          <div class="welcome-message" *ngIf="messages.length === 0 && sessionStatus !== 'idle'">
            <div class="welcome-icon">🤖</div>
            <h4>Getting ready...</h4>
            <p>I'm preparing your personalized learning session!</p>
          </div>

          <!-- Messages -->
          <div
            class="message"
            *ngFor="let message of messages; trackBy: trackByMessageId"
            [class.user-message]="message.isUser"
            [class.bot-message]="!message.isUser">

            <div class="message-avatar" *ngIf="!message.isUser">
              <span class="bot-avatar">🤖</span>
            </div>

            <div class="message-content">
              <div class="message-bubble">
                <div class="message-text" [innerHTML]="formatMessage(message.message)"></div>
                <div class="message-meta">
                  <span class="message-time">{{formatTime(message.timestamp)}}</span>
                  <span class="message-subject" *ngIf="message.subject">{{message.subject}}</span>
                  <span class="difficulty-badge" *ngIf="message.difficulty">
                    Level {{message.difficulty}}
                  </span>
                </div>
              </div>
            </div>

            <div class="message-avatar" *ngIf="message.isUser">
              <span class="user-avatar">{{getInitials(currentSession?.studentName || 'U')}}</span>
            </div>
          </div>

          <!-- Typing indicator -->
          <div class="message bot-message" *ngIf="isTyping">
            <div class="message-avatar">
              <span class="bot-avatar">🤖</span>
            </div>
            <div class="message-content">
              <div class="message-bubble typing-bubble">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="input-container" *ngIf="sessionStatus === 'active'">
        <div class="quick-actions">
          <button class="quick-btn" (click)="sendQuickMessage('hint')" [disabled]="!canRequestHint()">
            💡 Hint
          </button>
          <button class="quick-btn" (click)="sendQuickMessage('help')">
            ❓ Help
          </button>
          <button class="quick-btn" (click)="sendQuickMessage('next')" *ngIf="canRequestNext()">
            ⏭️ Next
          </button>
        </div>

        <div class="input-box">
          <input
            type="text"
            [(ngModel)]="currentMessage"
            (keyup.enter)="sendMessage()"
            placeholder="Type your answer or message..."
            class="message-input"
            [disabled]="sessionStatus !== 'active' || isTyping"
            #messageInput>

          <button
            class="send-btn"
            (click)="sendMessage()"
            [disabled]="!canSend()">
            <span *ngIf="!isTyping">📤</span>
            <span *ngIf="isTyping" class="loading-spinner">⏳</span>
          </button>
        </div>

        <div class="input-help">
          <p>💬 Type your answer, ask for hints, or request help!</p>
        </div>
      </div>

      <!-- Session ended screen -->
      <div class="session-ended" *ngIf="sessionStatus === 'ended'">
        <div class="ended-content">
          <div class="ended-icon">🎉</div>
          <h3>Great job!</h3>
          <p>Your learning session has ended. Check the final summary above!</p>
          <div class="ended-actions">
            <button class="primary-btn" (click)="startNewSession()">
              🆕 New Session
            </button>
            <button class="secondary-btn" (click)="goHome()">
              🏠 Home
            </button>
          </div>
        </div>
      </div>

      <!-- No session screen -->
      <div class="no-session" *ngIf="sessionStatus === 'idle' && !currentSession">
        <div class="no-session-content">
          <div class="no-session-icon">📚</div>
          <h3>No Active Session</h3>
          <p>Start a new learning session to begin chatting with your AI tutor!</p>
          <button class="primary-btn" (click)="goHome()">
            🚀 Start New Session
          </button>
        </div>
      </div>
    </div>
  `,
  styleUrl: './chat-interface.component.scss'
})
export class ChatInterfaceComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  @ViewChild('messageInput') private messageInput!: ElementRef;

  currentSession: ChatSession | null = null;
  messages: ChatMessage[] = [];
  currentMessage = '';
  isTyping = false;
  sessionStatus: 'idle' | 'active' | 'waiting' | 'ended' = 'idle';
  lastMessageId = '';

  private subscriptions: Subscription[] = [];
  private shouldScrollToBottom = false;

  constructor(
    private chatSessionService: ChatSessionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.subscriptions.push(
      this.chatSessionService.getCurrentSession().subscribe(session => {
        this.currentSession = session;
      }),

      this.chatSessionService.getMessages().subscribe(messages => {
        this.messages = messages;
        this.shouldScrollToBottom = true;
      }),

      this.chatSessionService.getTypingStatus().subscribe(typing => {
        this.isTyping = typing;
        if (typing) this.shouldScrollToBottom = true;
      }),

      this.chatSessionService.getSessionStatus().subscribe(status => {
        this.sessionStatus = status;
      }),

      this.chatSessionService.getNewMessage().subscribe(message => {
        // Auto-scroll when new messages arrive
        this.shouldScrollToBottom = true;

        // Focus input if it's user's turn to respond
        if (!message.isUser && this.sessionStatus === 'active') {
          setTimeout(() => {
            this.focusInput();
          }, 1000);
        }
      })
    );
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  trackByMessageId(index: number, message: ChatMessage): string {
    return message.id;
  }

  sendMessage(): void {
    if (!this.canSend()) return;

    const message = this.currentMessage.trim();
    this.currentMessage = '';
    this.chatSessionService.sendMessage(message);
  }

  sendQuickMessage(action: string): void {
    this.currentMessage = action;
    this.sendMessage();
  }

  requestHint(): void {
    this.chatSessionService.requestHint();
  }

  pauseSession(): void {
    this.chatSessionService.pauseSession();
  }

  endSession(): void {
    if (confirm('Are you sure you want to end this session?')) {
      this.chatSessionService.endSession();
    }
  }

  startNewSession(): void {
    this.goHome();
  }

  goHome(): void {
    this.router.navigate(['/']);
  }

  canSend(): boolean {
    return this.currentMessage.trim().length > 0 &&
           this.sessionStatus === 'active' &&
           !this.isTyping;
  }

  canRequestHint(): boolean {
    return this.sessionStatus === 'active' &&
           this.messages.length > 0 &&
           this.isLastMessageQuestion();
  }

  canRequestNext(): boolean {
    return this.sessionStatus === 'active' &&
           !this.isLastMessageQuestion();
  }

  getStatusText(): string {
    switch (this.sessionStatus) {
      case 'idle': return 'Not started';
      case 'active': return 'Learning active';
      case 'waiting': return 'Thinking...';
      case 'ended': return 'Session completed';
      default: return 'Unknown';
    }
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  formatMessage(message: string): string {
    // Convert newlines to <br> tags and add basic formatting
    return message
      .replace(/\n/g, '<br>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') // Bold
      .replace(/\*(.*?)\*/g, '<em>$1</em>') // Italic
      .replace(/`(.*?)`/g, '<code>$1</code>') // Code
      .replace(/Options:/g, '<br><strong>Options:</strong>')
      .replace(/([A-D])\)/g, '<br><strong>$1)</strong>'); // Multiple choice options
  }

  formatTime(timestamp: Date | string): string {
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  private isLastMessageQuestion(): boolean {
    if (this.messages.length === 0) return false;
    const lastBotMessage = [...this.messages].reverse().find(m => !m.isUser);
    return lastBotMessage ? this.isQuestion(lastBotMessage.message) : false;
  }

  private isQuestion(message: string): boolean {
    return message.includes('?') || message.includes('Options:') ||
           message.includes('Calculate') || message.includes('Solve');
  }

  private scrollToBottom(): void {
    try {
      const container = this.messagesContainer?.nativeElement;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    } catch (err) {
      console.error('Could not scroll to bottom:', err);
    }
  }

  private focusInput(): void {
    try {
      this.messageInput?.nativeElement?.focus();
    } catch (err) {
      console.error('Could not focus input:', err);
    }
  }
}