import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import {GlobeService} from "../services/globe.service";
import {AppConfig} from "../services/appConfig";
import {RouterModule} from "@angular/router";
import {DialogNewSnowglobe, HomePage} from "../pages/home.page";
import {ConnectionPage} from "../pages/connection.page";

import {EnvironmentPage} from "../pages/environment.page";
import {DialogApply, DialogDestroy, DialogValidate, GlobePage} from "pages/globe.page";
import {CodemirrorModule} from "ng2-codemirror";
import {MatTabsModule} from '@angular/material/tabs';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {
  MAT_PLACEHOLDER_GLOBAL_OPTIONS, MatButton, MatButtonModule, MatCardModule, MatCheckbox,
  MatCheckboxModule,
  MatDialogModule, MatFormFieldModule, MatIconModule, MatInputModule, MatOptionModule,
  MatProgressBarModule,
  MatProgressSpinner,
  MatProgressSpinnerModule,
  MatSelectModule, MatToolbarModule
} from "@angular/material";
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import {WindowRefService} from "../services/windowref.service";
import {STOMPService} from "../services/stomp/stomp.service";
import {ProgressService} from "../services/progress.service";
import {AppNavBarModule} from "../components/app.navbar";
import {DialogDelete} from "../pages/globe.page";

@NgModule({
  declarations: [
    AppComponent, HomePage, EnvironmentPage, ConnectionPage, GlobePage,
    DialogNewSnowglobe, DialogApply,DialogValidate, DialogDestroy, DialogDelete
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    CodemirrorModule,
    BrowserAnimationsModule,
    MatTabsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatOptionModule,
    MatSelectModule,
    MatInputModule,
    MatToolbarModule,
    MatIconModule,
    MatCardModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    AppNavBarModule,
    RouterModule.forRoot([
                           {
                             path: '',
                             //  name: 'Home',
                             component: HomePage

                             //   useAsDefault: true
                           },
                           {
                             path: 'environment/:id',
                             //  name: 'Home',
                             component: EnvironmentPage,

                             //   useAsDefault: true
                           },
                           {
                             path: 'globe/:id',
                             //  name: 'Home',
                             component: GlobePage,

                             //   useAsDefault: true
                           },
                           {
                             path: 'connection/:id',
                             //  name: 'Home',
                             component: ConnectionPage

                             //   useAsDefault: true
                           }])
  ],
  providers: [GlobeService, ProgressService, AppConfig, WindowRefService, {provide: MAT_PLACEHOLDER_GLOBAL_OPTIONS, useValue: {float: 'always'}}],
  bootstrap: [AppComponent],
  entryComponents: [DialogNewSnowglobe, DialogApply, DialogValidate, DialogDestroy, DialogDelete]
})
export class AppModule { }
