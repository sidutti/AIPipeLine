import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/subject-selection/subject-selection.component').then(c => c.SubjectSelectionComponent)
  },
  {
    path: 'chat',
    loadComponent: () => import('./components/chat-interface/chat-interface.component').then(c => c.ChatInterfaceComponent)
  },
  {
    path: 'history',
    loadComponent: () => import('./components/session-history/session-history.component').then(c => c.SessionHistoryComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
