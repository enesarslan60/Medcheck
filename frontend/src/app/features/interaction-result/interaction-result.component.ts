import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { InteractionService } from '../../core/services/interaction.service';
import { Interaction } from '../../shared/models/interaction.model';

@Component({
  selector: 'app-interaction-result',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  templateUrl: './interaction-result.component.html',
  styleUrls: ['./interaction-result.component.scss']
})
export class InteractionResultComponent implements OnInit {
  results: Interaction[] = [];

  constructor(
    private interactionService: InteractionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.results = this.interactionService.getResults();
    if (!this.results.length) {
      this.router.navigate(['/']);
    }
  }

  hasRealInteractions(): boolean {
    return this.results.some((r) => r.severity !== 'NONE');
  }

  back(): void {
    this.router.navigate(['/']);
  }
}
