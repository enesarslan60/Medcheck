import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { debounceTime, switchMap, catchError, startWith } from 'rxjs/operators';

import { DrugService } from '../../core/services/drug.service';
import { InteractionService } from '../../core/services/interaction.service';
import { Drug } from '../../shared/models/drug.model';

interface DrugSlot {
  control: FormControl<string>;
  selected: Drug | null;
  suggestions: Drug[];
  suggestions$: Observable<Drug[]>;
  open: boolean;
  activeIndex: number;
}

@Component({
  selector: 'app-drug-search',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
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
      suggestions: [],
      open: false,
      activeIndex: -1,
      suggestions$: control.valueChanges.pipe(
        startWith(''),
        debounceTime(250),
        switchMap((value) => {
          const query = (value ?? '').trim();
          if (!query || (slot.selected && value === slot.selected.name)) {
            return of<Drug[]>([]);
          }
          return this.drugService.search(query).pipe(catchError(() => of<Drug[]>([])));
        })
      )
    };
    slot.suggestions$.subscribe((list) => {
      slot.suggestions = list;
      slot.activeIndex = list.length ? 0 : -1;
      slot.open = list.length > 0;
    });
    return slot;
  }

  onFocus(slot: DrugSlot): void {
    if (slot.suggestions.length) slot.open = true;
  }

  onBlur(slot: DrugSlot): void {
    setTimeout(() => (slot.open = false), 120);
  }

  onInputChange(slot: DrugSlot): void {
    if (slot.selected && slot.control.value !== slot.selected.name) {
      slot.selected = null;
    }
  }

  onKeydown(slot: DrugSlot, event: KeyboardEvent): void {
    if (!slot.open || !slot.suggestions.length) return;
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      slot.activeIndex = (slot.activeIndex + 1) % slot.suggestions.length;
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      slot.activeIndex =
        (slot.activeIndex - 1 + slot.suggestions.length) % slot.suggestions.length;
    } else if (event.key === 'Enter') {
      event.preventDefault();
      const drug = slot.suggestions[slot.activeIndex];
      if (drug) this.selectDrug(slot, drug);
    } else if (event.key === 'Escape') {
      slot.open = false;
    }
  }

  selectDrug(slot: DrugSlot, drug: Drug): void {
    slot.selected = drug;
    slot.control.setValue(drug.name, { emitEvent: false });
    slot.open = false;
    slot.suggestions = [];
  }

  addSlot(): void {
    this.slots.push(this.createSlot());
  }

  removeSlot(index: number): void {
    if (this.slots.length <= 2) return;
    this.slots.splice(index, 1);
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
