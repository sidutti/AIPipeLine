import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ChatSessionService } from '../../services/chat-session.service';
import { JavaApiService } from '../../services/java-api.service';

@Component({
  selector: 'app-subject-selection',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="subject-selection-container">
      <div class="header">
        <h1>🎓 Math & Physics Tutor</h1>
        <p>Interactive learning for 9th grade students</p>
      </div>

      <div class="connection-status" [ngClass]="{'connected': isConnected, 'disconnected': !isConnected}">
        <div class="status-indicator"></div>
        <span>{{ isConnected ? 'Connected to Java Backend' : 'Connecting...' }}</span>
      </div>

      <div class="selection-form" *ngIf="!isStarting">
        <div class="form-group">
          <label for="studentName">Student Name</label>
          <input
            type="text"
            id="studentName"
            [(ngModel)]="studentName"
            placeholder="Enter your name"
            class="form-control"
            [disabled]="!isConnected">
        </div>

        <div class="form-group">
          <label>Choose Your Subject</label>
          <div class="subject-options">
            <div class="subject-card"
                 [class.selected]="selectedSubject === 'math'"
                 (click)="selectSubject('math')"
                 [class.disabled]="!isConnected">
              <div class="subject-icon">🧮</div>
              <h3>Mathematics</h3>
              <p>Algebra, Geometry, and more</p>
              <ul>
                <li>Linear equations</li>
                <li>Quadratic functions</li>
                <li>Geometric proofs</li>
                <li>Statistics & probability</li>
              </ul>
            </div>

            <div class="subject-card"
                 [class.selected]="selectedSubject === 'physics'"
                 (click)="selectSubject('physics')"
                 [class.disabled]="!isConnected">
              <div class="subject-icon">⚡</div>
              <h3>Physics</h3>
              <p>Mechanics, Energy, and Forces</p>
              <ul>
                <li>Motion & kinematics</li>
                <li>Forces & Newton's laws</li>
                <li>Work & energy</li>
                <li>Waves & sound</li>
              </ul>
            </div>
          </div>
        </div>

        <div class="grade-selection">
          <label for="grade">Grade Level</label>
          <select id="grade" [(ngModel)]="grade" class="form-control" [disabled]="!isConnected">
            <option value="9">9th Grade</option>
            <option value="8">8th Grade</option>
            <option value="10">10th Grade</option>
          </select>
        </div>

        <button
          class="start-btn"
          (click)="startSession()"
          [disabled]="!canStart()"
          [class.loading]="isStarting">
          <span *ngIf="!isStarting">Start Learning Session 🚀</span>
          <span *ngIf="isStarting">Starting Session... ⏳</span>
        </button>

        <div class="recent-sessions" *ngIf="recentSessions.length > 0">
          <h4>Recent Sessions</h4>
          <div class="session-list">
            <div
              class="session-item"
              *ngFor="let session of recentSessions"
              (click)="resumeSession(session.id)">
              <div class="session-info">
                <span class="subject-badge" [class]="session.subject">{{session.subject}}</span>
                <span class="student-name">{{session.studentName}}</span>
                <span class="date">{{formatDate(session.startTime)}}</span>
              </div>
              <div class="session-stats" *ngIf="session.score">
                <span class="score">{{session.score}}%</span>
                <span class="questions">{{session.correctAnswers}}/{{session.totalQuestions}}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="loading-screen" *ngIf="isStarting">
        <div class="loading-animation">
          <div class="spinner"></div>
          <h3>Setting up your {{ selectedSubject }} session...</h3>
          <p>Connecting to the AI tutor and preparing personalized questions</p>
        </div>
      </div>
    </div>
  `,
  styleUrl: './subject-selection.component.scss'
})
export class SubjectSelectionComponent {
  studentName = '';
  selectedSubject: 'math' | 'physics' | null = null;
  grade = 9;
  isStarting = false;
  isConnected = false;
  recentSessions: any[] = [];

  constructor(
    private chatSessionService: ChatSessionService,
    private javaApiService: JavaApiService,
    private router: Router
  ) {
    this.checkConnection();
    this.loadRecentSessions();
  }

  selectSubject(subject: 'math' | 'physics'): void {
    if (!this.isConnected) return;
    this.selectedSubject = subject;
  }

  canStart(): boolean {
    return this.isConnected &&
           this.studentName.trim().length > 0 &&
           this.selectedSubject !== null &&
           !this.isStarting;
  }

  async startSession(): Promise<void> {
    if (!this.canStart()) return;

    this.isStarting = true;

    try {
      await this.chatSessionService.startNewSession(
        this.studentName.trim(),
        this.selectedSubject!,
        this.grade
      );

      // Navigate to chat interface
      this.router.navigate(['/chat']);
    } catch (error) {
      console.error('Failed to start session:', error);
      alert('Failed to start the session. Please try again.');
    } finally {
      this.isStarting = false;
    }
  }

  resumeSession(sessionId: string): void {
    // Implementation to resume a previous session
    this.router.navigate(['/chat'], { queryParams: { sessionId } });
  }

  private checkConnection(): void {
    this.javaApiService.checkConnection().subscribe(connected => {
      this.isConnected = connected;
    });

    // Subscribe to connection status updates
    this.javaApiService.getConnectionStatus().subscribe(connected => {
      this.isConnected = connected;
    });
  }

  private loadRecentSessions(): void {
    const savedName = localStorage.getItem('lastStudentName');
    if (savedName) {
      this.studentName = savedName;
      this.javaApiService.getSessionHistory(savedName).subscribe(
        sessions => {
          this.recentSessions = sessions.slice(0, 5); // Show last 5 sessions
        },
        error => {
          console.log('No recent sessions found');
        }
      );
    }
  }

  formatDate(date: Date | string): string {
    const d = new Date(date);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
}