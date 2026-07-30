import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { InteractionService } from '../../core/services/interaction.service';
import {
  DrugDetail,
  InteractionExplanationResponse,
  Severity
} from '../../shared/models/interaction.model';

type TabKey = 'ai' | 'raw';

@Component({
  selector: 'app-interaction-result',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './interaction-result.component.html',
  styleUrls: ['./interaction-result.component.scss']
})
export class InteractionResultComponent implements OnInit {
  result: InteractionExplanationResponse | null = null;

  /** index of the currently expanded drug panel, or null if none */
  openIndex: number | null = null;

  /** per-panel selected tab */
  activeTab: Record<number, TabKey> = {};

  constructor(
    private interactionService: InteractionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.result = this.interactionService.getResult();
    if (!this.result) {
      this.router.navigate(['/']);
    }
  }

  back(): void {
    this.router.navigate(['/']);
  }

  togglePanel(index: number): void {
    this.openIndex = this.openIndex === index ? null : index;
    if (this.openIndex === index && this.activeTab[index] === undefined) {
      this.activeTab[index] = this.defaultTab(this.result!.drugs[index]);
    }
  }

  setTab(index: number, tab: TabKey): void {
    this.activeTab[index] = tab;
  }

  currentTab(index: number, drug: DrugDetail): TabKey {
    return this.activeTab[index] ?? this.defaultTab(drug);
  }

  private defaultTab(drug: DrugDetail): TabKey {
    return drug.aiSideEffectSummary ? 'ai' : 'raw';
  }

  severityLabel(severity: Severity): string {
    switch (severity) {
      case 'LOW':    return 'Niedriges Risiko';
      case 'MEDIUM': return 'Mittleres Risiko';
      case 'HIGH':   return 'Hohes Risiko';
      default:       return 'Risiko unbekannt';
    }
  }

  severityClass(severity: Severity): string {
    return `severity-${severity.toLowerCase()}`;
  }
}
