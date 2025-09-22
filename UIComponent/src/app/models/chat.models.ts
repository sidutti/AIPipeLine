export interface ChatMessage {
  id: string;
  sessionId: string;
  message: string;
  isUser: boolean;
  timestamp: Date;
  subject?: 'math' | 'physics';
  questionType?: 'multiple-choice' | 'open-ended' | 'calculation';
  difficulty?: number;
}

export interface ChatSession {
  id: string;
  studentName: string;
  subject: 'math' | 'physics';
  grade: number;
  startTime: Date;
  endTime?: Date;
  messages: ChatMessage[];
  score?: number;
  totalQuestions?: number;
  correctAnswers?: number;
  status: 'active' | 'completed' | 'paused';
}

export interface QuestionRequest {
  sessionId: string;
  subject: 'math' | 'physics';
  grade: number;
  difficulty?: number;
  previousQuestions?: string[];
}

export interface QuestionResponse {
  question: string;
  questionId: string;
  questionType: 'multiple-choice' | 'open-ended' | 'calculation';
  options?: string[];
  correctAnswer?: string;
  explanation?: string;
  difficulty: number;
  topic: string;
}

export interface AnswerRequest {
  sessionId: string;
  questionId: string;
  userAnswer: string;
  timeSpent?: number;
}

export interface AnswerResponse {
  isCorrect: boolean;
  correctAnswer: string;
  explanation: string;
  score: number;
  feedback: string;
  nextQuestionAvailable: boolean;
}

export interface SessionSummary {
  sessionId: string;
  totalQuestions: number;
  correctAnswers: number;
  score: number;
  timeSpent: number;
  strengths: string[];
  weaknesses: string[];
  recommendations: string[];
}