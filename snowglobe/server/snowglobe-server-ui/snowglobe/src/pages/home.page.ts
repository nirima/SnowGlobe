import {Component, Inject} from '@angular/core';


import {Injectable} from '@angular/core';
import { Message } from 'stompjs';
import {GlobeService} from "../services/globe.service";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material";
import {STOMPService, STOMPState} from "../services/stomp/stomp.service";
import {StompConfig} from "../services/stomp/stomp.config";
import {Observable} from "rxjs/Observable";
import {ProgressService} from "../services/progress.service";
import * as moment from 'moment';

@Component({
             template: `
               
               <style>
                 .home-list {
                   padding-left: 15px;
                   
                 }

                 .snowglobe-card {
                   width: 400px;
                 }

                 .cardbox {
                   display: flex;
                   flex-wrap: wrap;
                 }
                 
                
                 
                 .tag-_empty {
                     background-color: darkgrey;
                   }
                 
                 .tag-_state_error {
                   background-color: red;
                 }
                
                 .type-git {
                   border: 1px solid green;
                 }
                 
                 mat-card {
                   margin-bottom: 10px;
                   margin-right: 10px;
                 }

                 mat-card:hover {
                   background-color: lightskyblue;
                 }
               </style>


               <mat-toolbar color="primary">
                 <mat-toolbar-row>

                   <a mat-button class="docs-button docs-navbar-hide-small" (click)="newSnowglobe()"
                      >
                     
                     <mat-icon>library_add</mat-icon>
                     
                     Create New
                   </a>

                   <a mat-button class="docs-button docs-navbar-hide-small" (click)="cloneSnowglobe()"
                   >

                     <mat-icon>cloud_download</mat-icon>

                     Clone
                   </a>

                   <a mat-button class="docs-button docs-navbar-hide-small" (click)="duplicateSnowglobe()"
                   >

                     <mat-icon>file_copy</mat-icon>

                     Duplicate
                   </a>
                   

                 </mat-toolbar-row>
               </mat-toolbar>
<div class="home-list">

  
  <H1>{{state|async}}</H1>
  <H1>Snowglobes</H1>


  
<div class="cardbox">
  <mat-card *ngFor="let globe of globes" class="snowglobe-card" [routerLink]="['/globe', globe.id]" [ngClass]="tagClasses(globe)">
    <mat-card-header>
      <mat-card-title><b>{{globe.name}}</b></mat-card-title>
    <mat-card-subtitle *ngIf="globe.lastUpdate">Last Update:{{formatDate(globe.lastUpdate)}}</mat-card-subtitle>
      <div mat-card-avatar class="snowglobe-header-image"></div>
    </mat-card-header>
    <mat-card-content>
      <a >{{globe.name}}</a>
    </mat-card-content>
    <mat-chip-list>
      <mat-chip *ngFor="let tag of filterTags(globe.tags)" color="primary" selected="true">{{tag}}</mat-chip>
     
    </mat-chip-list>
  </mat-card>
  <hr>
       
        <!--<div class="col-md-12">-->
          <!--<table class="table">-->
            <!---->
            <!--<tbody>-->
              <!--<tr *ngFor="let env of globe.environments">-->
                <!--<td><a [routerLink]="['/environment', env.id]">{{env.name}}</a></td>-->
                <!---->
              <!--</tr>              -->
            <!--</tbody>-->
          <!--</table>-->
        <!--</div>-->
      <!---->
      <!--</div>-->


     </div>

 
</div>

`

})

@Injectable()
export class HomePage {
  globes = [];
  public state: Observable<string>;


  constructor(public globeService:GlobeService, public dialog: MatDialog,
              private _progressService: ProgressService) {}

  async ngOnInit() {
    this.refresh();


    console.log(" -- connected -- ");


  }
  refresh() {
    this.globeService.getList().subscribe( it => this.globes = it );
  }

  tagClasses(g):Array<string> {
    let t = [];
    g.tags.forEach(  gt => {t = t.concat(`tag-${gt}`)});

    if( g.type != null ) {
      t = t.concat(`type-${g.type}`);
    }

    return t;
  }

  filterTags(tags):Array<string> {
    return tags.filter( (t: string) => {


      return t.length > 0 && !t.startsWith("_");});
  }

  formatDate(dt):string {
    if( dt == null )
      return "";

    return moment(dt).fromNow();//.humanize();
  }

  async newSnowglobe() {

      let dialogRef = this.dialog.open(DialogNewSnowglobe, {
        width: '250px',
        data: {}
      });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed ' + result);
      this.create(result);
    });
  }

  async cloneSnowglobe() {

    let dialogRef = this.dialog.open(DialogCloneSnowglobe, {
      width: '500px',
      data: {}
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed ');
      console.log(result);
      this.clone(result.url, result.username, result.password);

    });
  }

  async duplicateSnowglobe() {

    let dialogRef = this.dialog.open(DialogDuplicateSnowglobe, {
      width: '250px',
      data: this.globes
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed ');
      console.log(result);
      this.duplicate(result.from, result.to);
    });
  }

  async create(id) {
    await this.globeService.create(id);
    this.refresh();
  }

  async clone(url, username, password) {
    await this.globeService.newCloneWithPassword(url,username,password);
    this.refresh();
  }

  async duplicate(from,to) {
    await this.globeService.duplicate(from, to);
    this.refresh();
  }
}


@Component({
             selector: 'dialog-new-snowglobe',
             template: `<h1 mat-dialog-title>Create Snowglobe</h1>
             <div mat-dialog-content>
               <p>Identifier for Snowglobe?</p>
               <mat-form-field>
                 <input matInput tabindex="1" [(ngModel)]="data.id">
               </mat-form-field>
             </div>
             <div mat-dialog-actions>
               <button mat-button [mat-dialog-close]="data.id" tabindex="2">Ok</button>
               <button mat-button (click)="onNoClick()" tabindex="-1">Cancel</button>
             </div>
             `,
           })
export class DialogNewSnowglobe {

  constructor(
    public dialogRef: MatDialogRef<DialogNewSnowglobe>,
    @Inject(MAT_DIALOG_DATA) public data: any) { }

  onNoClick(): void {
    this.dialogRef.close();
  }

}


@Component({
             selector: 'dialog-clone-snowglobe',
             template: `
               <style>
                 .dialog-content {
                   display: flex;
                   flex-direction: column;
                 }

                 .dialog-content > * {
                   width: 100%;
                 }
               </style>
               
               <h1 mat-dialog-title>Clone Snowglobe</h1>
             <div class="dialog-content" mat-dialog-content>
               <p>Clone Snowglobe</p>
               <mat-form-field>
                 <input matInput placeholder="Name to use" tabindex="1" [(ngModel)]="name">
               </mat-form-field>
               <mat-form-field>
                 <input matInput placeholder="URL to clone from" tabindex="2" [(ngModel)]="url">
               </mat-form-field>
               <mat-form-field>
                 <input matInput placeholder="Username (if required)" tabindex="3" [(ngModel)]="username">
               </mat-form-field>
               <mat-form-field>
                 <input type="password" matInput placeholder="Password (if required)" tabindex="4" [(ngModel)]="password">
               </mat-form-field>
             </div>
             <div mat-dialog-actions>
               <button mat-button [mat-dialog-close]="dialogData" tabindex="5">Ok</button>
               <button mat-button (click)="onNoClick()" tabindex="-1">Cancel</button>
             </div>
             `,
           })
export class DialogCloneSnowglobe {

  name:string;
  url:string;
  username:string;
  password:string;

  get dialogData():any {
    return {url: this.url, name: this.name, username: this.username, password: this.password };
  }

  constructor(
    public dialogRef: MatDialogRef<DialogCloneSnowglobe>,
    @Inject(MAT_DIALOG_DATA) public data: any) { }

  onNoClick(): void {
    this.dialogRef.close();
  }

}

@Component({
             selector: 'dialog-duplicate-snowglobe',
             template: `<h1 mat-dialog-title>Duplicate Snowglobe</h1>
             <div mat-dialog-content>
               <p>Duplicate Snowglobe</p>
               <mat-form-field>
                 <mat-select placeholder="Existing Snowglobe" [(ngModel)]="oldName">
                   <mat-option *ngFor="let globe of data" [value]="globe.id">
                     {{ globe?.id }}
                   </mat-option>
                 </mat-select>
               </mat-form-field>
               <mat-form-field>
                 <input matInput placeholder="New Name" tabindex="1" [(ngModel)]="newName">
               </mat-form-field>
             </div>
             <div mat-dialog-actions>
               <button mat-button [mat-dialog-close]="createData" tabindex="2">Ok</button>
               <button mat-button (click)="onNoClick()" tabindex="-1">Cancel</button>
             </div>
             `,
           })
export class DialogDuplicateSnowglobe {

  oldName:any;
  newName:string;

  get createData():any {
    if( this.oldName == null || this.newName == null ) {
      return null;
    }
    return {from:this.oldName, to:this.newName};
  }

  constructor(
    public dialogRef: MatDialogRef<DialogDuplicateSnowglobe>,
    @Inject(MAT_DIALOG_DATA) public data: any) { }

  onNoClick(): void {
    this.dialogRef.close();
  }

}

