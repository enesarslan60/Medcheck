import { Routes } from '@angular/router';

import { DrugSearchComponent } from './features/drug-search/drug-search.component';
import { InteractionResultComponent } from './features/interaction-result/interaction-result.component';

export const routes: Routes = [
  { path: '', component: DrugSearchComponent },
  { path: 'results', component: InteractionResultComponent },
  { path: '**', redirectTo: '' }
];
