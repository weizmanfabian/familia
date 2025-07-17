import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PersonaList } from './components/persona-list/persona-list';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [PersonaList, CommonModule],
  template: `
    <div class="app-container">
      <app-persona-list></app-persona-list>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background-color: #f8f9fa;
    }
    .app-container {
      padding: 20px;
    }
  `]
})
export class App {}