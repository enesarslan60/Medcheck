import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { InteractionService } from '../../core/services/interaction.service';
import { DrugInteractionData } from '../../shared/models/interaction.model';

@Component({
  selector: 'app-interaction-result',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './interaction-result.component.html',
  styleUrls: ['./interaction-result.component.scss']
})
export class InteractionResultComponent implements OnInit {
  results: DrugInteractionData[] = [];

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

  hasAnyData(): boolean {
    return this.results.some((r) => r.interactionText !== null);
  }

  back(): void {
    this.router.navigate(['/']);
  }
}
