import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { debounceTime, switchMap, catchError, startWith } from 'rxjs/operators';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';

import { DrugService } from '../../core/services/drug.service';
import { InteractionService } from '../../core/services/interaction.service';
import { Drug } from '../../shared/models/drug.model';

@Component({
  selector: 'app-drug-search',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatChipsModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatCardModule
  ],
  templateUrl: './drug-search.component.html',
  styleUrls: ['./drug-search.component.scss']
})
export class DrugSearchComponent implements OnInit {
  searchControl = new FormControl<string>('', { nonNullable: true });
  suggestions$!: Observable<Drug[]>;
  selected: Drug[] = [];
  loading = false;
  errorMessage: string | null = null;

  constructor(
    private drugService: DrugService,
    private interactionService: InteractionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.suggestions$ = this.searchControl.valueChanges.pipe(
      startWith(''),
      debounceTime(250),
      switchMap((value) => {
        const query = (value ?? '').trim();
        if (!query) {
          return of<Drug[]>([]);
        }
        return this.drugService.search(query).pipe(catchError(() => of<Drug[]>([])));
      })
    );
  }

  displayDrug(drug: Drug | string | null): string {
    if (!drug) return '';
    return typeof drug === 'string' ? drug : drug.name;
  }

  onSelect(event: MatAutocompleteSelectedEvent): void {
    const drug = event.option.value as Drug;
    if (drug && !this.selected.some((d) => d.id === drug.id)) {
      this.selected.push(drug);
    }
    this.searchControl.setValue('');
  }

  remove(drug: Drug): void {
    this.selected = this.selected.filter((d) => d.id !== drug.id);
  }

  check(): void {
    if (this.selected.length < 2) {
      this.errorMessage = 'Please select at least two drugs to check for interactions.';
      return;
    }
    this.errorMessage = null;
    this.loading = true;
    const names = this.selected.map((d) => d.name);
    this.interactionService.check(names).subscribe({
      next: (results) => {
        this.loading = false;
        this.interactionService.setResults(results);
        this.router.navigate(['/results']);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Could not reach the server. Please try again.';
      }
    });
  }
}
