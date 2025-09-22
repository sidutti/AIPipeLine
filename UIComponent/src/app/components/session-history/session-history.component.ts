import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { JavaApiService } from '../../services/java-api.service';
import { ChatSession } from '../../models/chat.models';

@Component({
  selector: 'app-session-history',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="history-container">
      <div class="header">
        <div class="header-content">
          <h1>📚 Session History</h1>
          <p>Review your learning progress and past sessions</p>
        </div>
        <button class="back-btn" (click)="goBack()">
          🏠 Home
        </button>
      </div>

      <div class="content">
        <div class="stats-overview" *ngIf="sessions.length > 0">
          <div class="stat-card">
            <div class="stat-icon">📊</div>
            <div class="stat-info">
              <span class="stat-value">{{totalSessions}}</span>
              <span class="stat-label">Total Sessions</span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">⭐</div>
            <div class="stat-info">
              <span class="stat-value">{{averageScore}}%</span>
              <span class="stat-label">Average Score</span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">📝</div>
            <div class="stat-info">
              <span class="stat-value">{{totalQuestions}}</span>
              <span class="stat-label">Questions Answered</span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">⏱️</div>
            <div class="stat-info">
              <span class="stat-value">{{totalTimeFormatted}}</span>
              <span class="stat-label">Study Time</span>
            </div>
          </div>
        </div>

        <div class="sessions-section" *ngIf="!loading">
          <div class="section-header">
            <h2>All Sessions</h2>
            <div class="filters">
              <select (change)="filterBySubject($event)" class="filter-select">
                <option value="all">All Subjects</option>
                <option value="math">Math</option>
                <option value="physics">Physics</option>
              </select>

              <select (change)="sortBy($event)" class="filter-select">
                <option value="date-desc">Newest First</option>
                <option value="date-asc">Oldest First</option>
                <option value="score-desc">Best Score</option>
                <option value="score-asc">Lowest Score</option>
              </select>
            </div>
          </div>

          <div class="sessions-list" *ngIf="filteredSessions.length > 0">
            <div
              class="session-card"
              *ngFor="let session of filteredSessions"
              [class.completed]="session.status === 'completed'"
              (click)="viewSession(session)">

              <div class="session-header">
                <div class="session-subject">
                  <span class="subject-badge" [class]="session.subject">
                    {{ session.subject === 'math' ? '🧮' : '⚡' }} {{ session.subject | titlecase }}
                  </span>
                  <span class="grade-badge">Grade {{ session.grade }}</span>
                </div>

                <div class="session-status" [class]="session.status">
                  {{ getStatusText(session.status) }}
                </div>
              </div>

              <div class="session-info">
                <div class="student-info">
                  <div class="avatar">{{ getInitials(session.studentName) }}</div>
                  <div class="details">
                    <h3>{{ session.studentName }}</h3>
                    <p class="date">{{ formatDateTime(session.startTime) }}</p>
                    <p class="duration" *ngIf="session.endTime">
                      Duration: {{ getDuration(session.startTime, session.endTime) }}
                    </p>
                  </div>
                </div>

                <div class="session-metrics" *ngIf="session.score !== undefined">
                  <div class="metric">
                    <div class="metric-value score" [class]="getScoreClass(session.score!)">
                      {{ session.score }}%
                    </div>
                    <div class="metric-label">Score</div>
                  </div>

                  <div class="metric" *ngIf="session.totalQuestions">
                    <div class="metric-value">
                      {{ session.correctAnswers }}/{{ session.totalQuestions }}
                    </div>
                    <div class="metric-label">Questions</div>
                  </div>

                  <div class="metric">
                    <div class="metric-value">{{ session.messages.length }}</div>
                    <div class="metric-label">Messages</div>
                  </div>
                </div>
              </div>

              <div class="session-preview">
                <p class="last-message" *ngIf="getLastMessage(session)">
                  "{{ getLastMessage(session) }}"
                </p>
              </div>

              <div class="session-actions">
                <button class="action-btn view" (click)="viewSession(session); $event.stopPropagation()">
                  👁️ View Details
                </button>
                <button
                  class="action-btn resume"
                  *ngIf="session.status === 'active' || session.status === 'paused'"
                  (click)="resumeSession(session); $event.stopPropagation()">
                  ▶️ Resume
                </button>
                <button class="action-btn delete" (click)="deleteSession(session); $event.stopPropagation()">
                  🗑️ Delete
                </button>
              </div>
            </div>
          </div>

          <div class="empty-state" *ngIf="filteredSessions.length === 0 && sessions.length > 0">
            <div class="empty-icon">🔍</div>
            <h3>No sessions match your filters</h3>
            <p>Try adjusting your filter criteria</p>
            <button class="primary-btn" (click)="clearFilters()">Clear Filters</button>
          </div>
        </div>

        <div class="loading-state" *ngIf="loading">
          <div class="spinner"></div>
          <p>Loading your session history...</p>
        </div>

        <div class="empty-history" *ngIf="!loading && sessions.length === 0">
          <div class="empty-icon">📚</div>
          <h2>No Sessions Yet</h2>
          <p>Start your first learning session to see your progress here!</p>
          <button class="primary-btn" (click)="startNewSession()">
            🚀 Start First Session
          </button>
        </div>
      </div>
    </div>
  `,
  styleUrl: './session-history.component.scss'
})
export class SessionHistoryComponent implements OnInit {
  sessions: ChatSession[] = [];
  filteredSessions: ChatSession[] = [];
  loading = true;
  currentFilter = 'all';
  currentSort = 'date-desc';

  // Computed stats
  totalSessions = 0;
  averageScore = 0;
  totalQuestions = 0;
  totalTimeFormatted = '0h 0m';

  constructor(
    private javaApiService: JavaApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  loadHistory(): void {
    const studentName = localStorage.getItem('lastStudentName');
    if (!studentName) {
      this.loading = false;
      return;
    }

    this.javaApiService.getSessionHistory(studentName).subscribe({
      next: (sessions) => {
        this.sessions = sessions;
        this.filteredSessions = [...sessions];
        this.calculateStats();
        this.applyFiltersAndSort();
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load session history:', error);
        this.loading = false;
      }
    });
  }

  filterBySubject(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.currentFilter = select.value;
    this.applyFiltersAndSort();
  }

  sortBy(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.currentSort = select.value;
    this.applyFiltersAndSort();
  }

  clearFilters(): void {
    this.currentFilter = 'all';
    this.currentSort = 'date-desc';
    this.applyFiltersAndSort();

    // Reset select elements
    const selects = document.querySelectorAll('.filter-select') as NodeListOf<HTMLSelectElement>;
    selects.forEach(select => {
      if (select.value !== this.currentFilter && select.value !== this.currentSort) {
        select.selectedIndex = 0;
      }
    });
  }

  viewSession(session: ChatSession): void {
    // Navigate to detailed session view (could be implemented later)
    console.log('Viewing session:', session);
  }

  resumeSession(session: ChatSession): void {
    // Resume an active session
    this.router.navigate(['/chat'], { queryParams: { sessionId: session.id } });
  }

  deleteSession(session: ChatSession): void {
    if (confirm(`Are you sure you want to delete the session from ${this.formatDateTime(session.startTime)}?`)) {
      // Implementation would call API to delete session
      this.sessions = this.sessions.filter(s => s.id !== session.id);
      this.applyFiltersAndSort();
      this.calculateStats();
    }
  }

  startNewSession(): void {
    this.router.navigate(['/']);
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'active': return 'In Progress';
      case 'completed': return 'Completed';
      case 'paused': return 'Paused';
      default: return 'Unknown';
    }
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  formatDateTime(date: Date | string): string {
    const d = new Date(date);
    return d.toLocaleDateString() + ' at ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  getDuration(start: Date | string, end: Date | string): string {
    const startTime = new Date(start).getTime();
    const endTime = new Date(end).getTime();
    const duration = endTime - startTime;

    const hours = Math.floor(duration / (1000 * 60 * 60));
    const minutes = Math.floor((duration % (1000 * 60 * 60)) / (1000 * 60));

    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    }
    return `${minutes}m`;
  }

  getScoreClass(score: number): string {
    if (score >= 90) return 'excellent';
    if (score >= 80) return 'good';
    if (score >= 70) return 'average';
    return 'needs-improvement';
  }

  getLastMessage(session: ChatSession): string {
    const lastMessage = session.messages[session.messages.length - 1];
    if (!lastMessage) return '';

    const message = lastMessage.message;
    return message.length > 100 ? message.substring(0, 100) + '...' : message;
  }

  private applyFiltersAndSort(): void {
    let filtered = [...this.sessions];

    // Apply subject filter
    if (this.currentFilter !== 'all') {
      filtered = filtered.filter(session => session.subject === this.currentFilter);
    }

    // Apply sorting
    filtered.sort((a, b) => {
      switch (this.currentSort) {
        case 'date-desc':
          return new Date(b.startTime).getTime() - new Date(a.startTime).getTime();
        case 'date-asc':
          return new Date(a.startTime).getTime() - new Date(b.startTime).getTime();
        case 'score-desc':
          return (b.score || 0) - (a.score || 0);
        case 'score-asc':
          return (a.score || 0) - (b.score || 0);
        default:
          return 0;
      }
    });

    this.filteredSessions = filtered;
  }

  private calculateStats(): void {
    this.totalSessions = this.sessions.length;

    const completedSessions = this.sessions.filter(s => s.score !== undefined);

    if (completedSessions.length > 0) {
      const totalScore = completedSessions.reduce((sum, s) => sum + (s.score || 0), 0);
      this.averageScore = Math.round(totalScore / completedSessions.length);
    }

    this.totalQuestions = this.sessions.reduce((sum, s) => sum + (s.totalQuestions || 0), 0);

    // Calculate total time
    const totalMinutes = this.sessions.reduce((sum, s) => {
      if (s.startTime && s.endTime) {
        const duration = new Date(s.endTime).getTime() - new Date(s.startTime).getTime();
        return sum + Math.floor(duration / (1000 * 60));
      }
      return sum;
    }, 0);

    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    this.totalTimeFormatted = `${hours}h ${minutes}m`;
  }
}