# Java Backend API Requirements

This Angular chat application expects a Java backend running on `http://localhost:8080` with the following REST endpoints:

## Base URL
```
http://localhost:8080/api/tutor
```

## Required Endpoints

### Health Check
```
GET /health
Returns: { "status": "ok" }
```

### Session Management

#### Create Session
```
POST /session/create
Body: {
  "studentName": "string",
  "subject": "math" | "physics",
  "grade": number,
  "timestamp": "ISO datetime string"
}
Returns: ChatSession object
```

#### End Session
```
POST /session/end
Body: { "sessionId": "string" }
Returns: SessionSummary object
```

#### Pause Session
```
POST /session/pause
Body: { "sessionId": "string" }
Returns: { "success": boolean }
```

#### Resume Session
```
POST /session/resume
Body: { "sessionId": "string" }
Returns: { "success": boolean }
```

#### Get Session History
```
GET /session/history/{studentName}
Returns: ChatSession[]
```

#### Get Session Details
```
GET /session/{sessionId}
Returns: ChatSession object
```

### Question Management

#### Generate Question
```
POST /question/generate
Body: QuestionRequest {
  "sessionId": "string",
  "subject": "math" | "physics",
  "grade": number,
  "difficulty": number (optional),
  "previousQuestions": string[] (optional)
}
Returns: QuestionResponse
```

#### Submit Answer
```
POST /answer/submit
Body: AnswerRequest {
  "sessionId": "string",
  "questionId": "string",
  "userAnswer": "string",
  "timeSpent": number (optional, in seconds)
}
Returns: AnswerResponse
```

#### Get Hint
```
POST /question/hint
Body: {
  "questionId": "string",
  "sessionId": "string"
}
Returns: { "hint": "string" }
```

## Data Models

### ChatSession
```typescript
{
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
```

### ChatMessage
```typescript
{
  id: string;
  sessionId: string;
  message: string;
  isUser: boolean;
  timestamp: Date;
  subject?: 'math' | 'physics';
  questionType?: 'multiple-choice' | 'open-ended' | 'calculation';
  difficulty?: number;
}
```

### QuestionResponse
```typescript
{
  question: string;
  questionId: string;
  questionType: 'multiple-choice' | 'open-ended' | 'calculation';
  options?: string[];
  correctAnswer?: string;
  explanation?: string;
  difficulty: number;
  topic: string;
}
```

### AnswerResponse
```typescript
{
  isCorrect: boolean;
  correctAnswer: string;
  explanation: string;
  score: number;
  feedback: string;
  nextQuestionAvailable: boolean;
}
```

### SessionSummary
```typescript
{
  sessionId: string;
  totalQuestions: number;
  correctAnswers: number;
  score: number;
  timeSpent: number;
  strengths: string[];
  weaknesses: string[];
  recommendations: string[];
}
```

## CORS Configuration

Make sure your Java backend allows CORS requests from the Angular development server:

```java
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
```

## Sample 9th Grade Topics

### Mathematics
- Linear equations and systems
- Quadratic functions and equations
- Polynomials and factoring
- Rational expressions
- Radical expressions and equations
- Coordinate geometry
- Functions and graphs
- Statistics and probability
- Sequences and series
- Basic trigonometry

### Physics
- Motion and kinematics
- Forces and Newton's laws
- Work, energy, and power
- Momentum and collisions
- Waves and sound
- Light and optics
- Electricity and magnetism basics
- Heat and temperature
- Simple machines
- Atomic structure basics

## Running the Application

1. Start your Java backend server on `http://localhost:8080`
2. Run the Angular application with `npm start`
3. Navigate to `http://localhost:4200`
4. Select a student name, subject, and start a session!

The chat interface will show connection status and guide users through interactive learning sessions.