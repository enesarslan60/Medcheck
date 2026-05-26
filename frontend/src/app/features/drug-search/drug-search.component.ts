import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { debounceTime, switchMap, catchError, startWith } from 'rxjs/operators';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';

import { DrugService } from '../../core/services/drug.service';
import { InteractionService } from '../../core/services/interaction.service';
import { Drug } from '../../shared/models/drug.model';

interface DrugSlot {
  control: FormControl<string>;
  selected: Drug | null;
  suggestions$: Observable<Drug[]>;
}

@Component({
  selector: 'app-drug-search',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatCardModule
  ],
  templateUrl: './drug-search.component.html',
  styleUrls: ['./drug-search.component.scss']
})
export class DrugSearchComponent implements OnInit {
  slots: DrugSlot[] = [];
  loading = false;
  errorMessage: string | null = null;

  constructor(
    private drugService: DrugService,
    private interactionService: InteractionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.slots.push(this.createSlot());
    this.slots.push(this.createSlot());
  }

  private createSlot(): DrugSlot {
    const control = new FormControl<string>('', { nonNullable: true });
    const slot: DrugSlot = {
      control,
      selected: null,
      suggestions$: control.valueChanges.pipe(
        startWith(''),
        debounceTime(250),
        switchMap((value) => {
          const query = (typeof value === 'string' ? value : '').trim();
          if (!query) {
            return of<Drug[]>([]);
          }
          return this.drugService.search(query).pipe(catchError(() => of<Drug[]>([])));
        })
      )
    };
    return slot;
  }

  displayDrug(drug: Drug | string | null): string {
    if (!drug) return '';
    return typeof drug === 'string' ? drug : drug.name;
  }

  onSelect(slot: DrugSlot, event: MatAutocompleteSelectedEvent): void {
    const drug = event.option.value as Drug;
    slot.selected = drug;
  }

  onInputChange(slot: DrugSlot): void {
    const value = slot.control.value;
    if (typeof value === 'string' && slot.selected && value !== slot.selected.name) {
      slot.selected = null;
    }
  }

  addSlot(): void {
    this.slots.push(this.createSlot());
  }

  removeSlot(index: number): void {
    if (this.slots.length <= 2) {
      return;
    }
    this.slots.splice(index, 1);
  }

  clearSlot(slot: DrugSlot): void {
    slot.selected = null;
    slot.control.setValue('');
  }

  get selectedCount(): number {
    return this.slots.filter((s) => s.selected !== null).length;
  }

  check(): void {
    const names = this.slots
      .map((s) => s.selected?.name)
      .filter((n): n is string => !!n);

    if (names.length < 2) {
      this.errorMessage = 'Please select at least two drugs to check for interactions.';
      return;
    }
    this.errorMessage = null;
    this.loading = true;
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
