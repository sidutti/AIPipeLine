import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import {
  QuestionRequest,
  QuestionResponse,
  AnswerRequest,
  AnswerResponse,
  SessionSummary,
  ChatSession
} from '../models/chat.models';

@Injectable({
  providedIn: 'root'
})
export class JavaApiService {
  private readonly baseUrl = 'http://localhost:8080/api/tutor';
  private connectionStatus = new BehaviorSubject<boolean>(false);

  constructor(private http: HttpClient) {
    this.checkConnection();
  }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*'
    });
  }

  checkConnection(): Observable<boolean> {
    return new Observable(observer => {
      this.http.get(`${this.baseUrl}/health`, { headers: this.getHeaders() })
        .subscribe({
          next: () => {
            this.connectionStatus.next(true);
            observer.next(true);
            observer.complete();
          },
          error: () => {
            this.connectionStatus.next(false);
            observer.next(false);
            observer.complete();
          }
        });
    });
  }

  getConnectionStatus(): Observable<boolean> {
    return this.connectionStatus.asObservable();
  }

  createSession(studentName: string, subject: 'math' | 'physics', grade: number): Observable<ChatSession> {
    const payload = {
      studentName,
      subject,
      grade,
      timestamp: new Date().toISOString()
    };

    return this.http.post<ChatSession>(
      `${this.baseUrl}/session/create`,
      payload,
      { headers: this.getHeaders() }
    );
  }

  getQuestion(request: QuestionRequest): Observable<QuestionResponse> {
    return this.http.post<QuestionResponse>(
      `${this.baseUrl}/question/generate`,
      request,
      { headers: this.getHeaders() }
    );
  }

  submitAnswer(request: AnswerRequest): Observable<AnswerResponse> {
    return this.http.post<AnswerResponse>(
      `${this.baseUrl}/answer/submit`,
      request,
      { headers: this.getHeaders() }
    );
  }

  endSession(sessionId: string): Observable<SessionSummary> {
    return this.http.post<SessionSummary>(
      `${this.baseUrl}/session/end`,
      { sessionId },
      { headers: this.getHeaders() }
    );
  }

  getSessionHistory(studentName: string): Observable<ChatSession[]> {
    return this.http.get<ChatSession[]>(
      `${this.baseUrl}/session/history/${studentName}`,
      { headers: this.getHeaders() }
    );
  }

  getSessionDetails(sessionId: string): Observable<ChatSession> {
    return this.http.get<ChatSession>(
      `${this.baseUrl}/session/${sessionId}`,
      { headers: this.getHeaders() }
    );
  }

  getHint(questionId: string, sessionId: string): Observable<{ hint: string }> {
    return this.http.post<{ hint: string }>(
      `${this.baseUrl}/question/hint`,
      { questionId, sessionId },
      { headers: this.getHeaders() }
    );
  }

  pauseSession(sessionId: string): Observable<{ success: boolean }> {
    return this.http.post<{ success: boolean }>(
      `${this.baseUrl}/session/pause`,
      { sessionId },
      { headers: this.getHeaders() }
    );
  }

  resumeSession(sessionId: string): Observable<{ success: boolean }> {
    return this.http.post<{ success: boolean }>(
      `${this.baseUrl}/session/resume`,
      { sessionId },
      { headers: this.getHeaders() }
    );
  }
}