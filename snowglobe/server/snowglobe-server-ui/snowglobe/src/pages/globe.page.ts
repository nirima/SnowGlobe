import {Component, Inject, ViewChild} from '@angular/core';


import {Injectable} from '@angular/core';

import {GlobeService} from "../services/globe.service";
import {ActivatedRoute, Router} from "@angular/router";

import 'codemirror/mode/groovy/groovy'
import {AppConfig} from "../services/appConfig";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material";
import {DialogNewSnowglobe} from "./home.page";

@Component({
             template: `
               
               
               <style>
                 .example-icon {
                   padding: 0 14px;
                 }

                 .example-spacer {
                   flex: 1 1 auto;
                 }

               </style>

               <style>
                 .main-content {
                   padding-left: 15px;
                 }
               </style>


               <mat-toolbar color="primary">
                 <mat-toolbar-row>
                
                     <a mat-button   [disabled]="processing"  (click)="validate()"><mat-icon>playlist_add_check</mat-icon>Validate</a>

                     <a mat-button   [disabled]="processing"  (click)="apply()"><mat-icon>play_arrow</mat-icon>Apply</a>

                     
                   
                     <a mat-button   [disabled]="processing"  (click)="refresh()">   <mat-icon>refresh</mat-icon> Refresh</a>

                     <a mat-button   [disabled]="processing"  (click)="destroy()"><mat-icon>cancel</mat-icon>Destroy</a>

                   <a mat-button   [disabled]="processing"  (click)="deleteIt()"><mat-icon>delete</mat-icon>Remove</a>
                   
                 </mat-toolbar-row>
                 <mat-toolbar-row class="breadcrumb">
                   <mat-icon>home</mat-icon><a [routerLink]="['/']"> Snowglobes </a> &gt; {{id}}
                 </mat-toolbar-row>
               </mat-toolbar>                                        
               <div class="home-list">
               
               
               

               <div *ngIf="error">
                 <B>ERROR</B>
                 <pre>
                 {{error._body}}
                   </pre>
                 <button (click)="clearError()">Clear!</button>
               </div>

               <div *ngIf="globe != null">
               
               <mat-tab-group [(selectedIndex)]="selectedIndex" class="demo-tab-group">
                <mat-tab label="Source">  
                  <div>
                    <div class="button-row">

                    

                    <mat-form-field>
                      <mat-select placeholder="Source file" [(value)]="selectedFile">
                        <mat-option *ngFor="let file of files" [value]="file">
                          {{ file.name }}
                        </mat-option>
                      </mat-select>
                    </mat-form-field>

                      <button  [disabled]="processing" mat-button mat-raised-button color="primary" (click)="saveCode()">Save</button>
                    </div>

                    <div>
                      <codemirror id="cmConfig" *ngIf="selectedFile" [(ngModel)]="selectedFile.content"  [config]="config">

                    </codemirror>
                    </div>
                    
                    
                  </div>
                </mat-tab>

                 <mat-tab label="State">
                   <codemirror #cmstate [(ngModel)]="state"  [config]="config">

                   </codemirror>

                   <div class="button-row">

                     <button  [disabled]="processing" mat-button mat-raised-button color="primary" (click)="saveState()">Save</button>
                   </div>

                 </mat-tab>
                 <mat-tab label="Graph">
                   <img [src]="imgUrl">
                 </mat-tab>
                 

                 
</mat-tab-group>
               </div>
                   <br>
               
               </div>
             `



           })

@Injectable()
export class GlobePage {

  @ViewChild('cmstate') cmstate:any;

  data:any = {};
  globe:any;
  id:string;

  validationStatus:string;

  // which code file ?
  private _selectedFile:EditorItem;
  files:Array<EditorItem> = [];

  state:string;
  imgUrl:string;

  processing:boolean = false;
  config;
  error:Object;

  private _selectedIndex:number;


  constructor( private _route:ActivatedRoute, private router: Router,
               public globeService:GlobeService,
    public dialog: MatDialog,
    public appConfig:AppConfig) {
    this.config = { lineNumbers: true, mode: 'text/x-groovy' };

  }

  /*get selectedFile(): EditorItem {
    return this._codeItem;
  }

  set selectedFile(value: EditorItem) {
    console.log("Set codeItem = " + value);
    this._codeItem = value;
    if( this.codeMap[value] == null )
    {
      this.globeService.getConfig(this.id, this._codeItem).subscribe(it => {this.code = it;
      this.codeMap[value] = it;
      } );
    } else {
      this.code = this.codeMap[value];
    }
  } */

  get selectedFile(): EditorItem {
    return this._selectedFile;
  }

  set selectedFile(value: EditorItem) {
    console.log("Selected file " + value.name);
    this._selectedFile = value;
    if( value.content == null ) {
      this.globeService.getConfig(this.id, value.name).subscribe(it => {value.content = it;
      } );
    }
  }

  clearError() {
    this.error = null;
    this.processing = false;
  }

  async saveCode() {
    // save all
    try {
      this.processing = true;
      for (let entry of this.files) {
        console.log("Save code " + entry.name);

        if( entry.content != null ) {
          await this.globeService.saveConfig(this.id, entry.name, entry.content);
        }
      }

       await this.refresh();
    } catch(e) {
      this.error = e;

      console.log(e) ;
    }

    this.processing = false;
  }

  async saveState() {
    try {
      this.processing = true;

      await this.globeService.saveState(this.id, this.state);
      await this.refresh();
    } catch(e) {
      this.error = e;

      console.log(e) ;
    }
    this.processing = false;
  }

  async validate() {
    console.log("Validate");
    try {
      this.processing = true;

      let dialogRef = this.dialog.open(DialogValidate, {
        width: '800px',
        data: this.id
      });

      dialogRef.afterClosed().subscribe(result => {
        console.log('The dialog was closed ' + result);
        this.processing = false;
        this.refresh();
        console.log("validate done");
      });

      // this.validationStatus = "";
      // await this.globeService.validate(this.id);
      // this.validationStatus = "OK";
    } catch(e) {
      this.error = e;

      this.validationStatus = "Error";

      console.log(e) ;
    }
    this.processing = false;
  }

  async apply() {
    console.log("Apply");
    try {
      this.processing = true;


      let dialogRef = this.dialog.open(DialogApply, {
        width: '800px',
        data: this.id
      });

      dialogRef.afterClosed().subscribe(result => {
        console.log('The dialog was closed ' + result);
        this.processing = false;
        this.refresh();
        console.log("Apply done");
      });

    } catch(e) {
      this.error = e;

      console.log(e) ;
    }

  }

  async initialize() {
    this.files = [];

    this.globe = await this.globeService.getDetails(this.id);
    for(let name of this.globe.configFiles ) {
      let ei = new EditorItem ();
      ei.name = name;
      console.log("Add file " + ei.name);
      this.files.push(  ei );
    }
    this.selectedFile = this.files[0];
  }

  async refresh() {


    this.state = await this.globeService.getState(this.id);
    //this.state = "FOO";
    this.imgUrl = `${this.appConfig.baseUrl}/data/globe/${this.id}/graph?seq=` + new Date().getMilliseconds();

  }

  async destroy() {
    console.log("destroy");
    try {
      this.processing = true;


      let dialogRef = this.dialog.open(DialogDestroy, {
        width: '800px',
        data: this.id
      });

      dialogRef.afterClosed().subscribe(result => {
        console.log('The dialog was closed ' + result);
        this.processing = false;
        this.refresh();
        console.log("Destroy done");
      });

    } catch(e) {
      this.error = e;

      console.log(e) ;
    }
  }

  async deleteIt() {

    try {
      this.processing = true;


      let dialogRef = this.dialog.open(DialogDelete, {
        width: '800px',
        data: this.id
      });

      dialogRef.afterClosed().subscribe(result => {
        console.log('The dialog was closed ' + result);
        this.processing = false;

        this.router.navigate(['/']);


      });

    } catch(e) {
      this.error = e;

      console.log(e) ;
    }
  }

  get selectedIndex(): number {
    return this._selectedIndex;
  }

  set selectedIndex(value: number) {
    this._selectedIndex = value;

    setTimeout( () => {
      this.cmstate.instance.refresh();
      console.log("AA");
    });
  }

  async ngOnInit() {
    console.log(this._route.params);
    this._route.params.map(p => p['id'])
      .subscribe((id) => {
        console.log(id);
        this.id = id;
        this.initialize();



        this.refresh();




      });

  }

}



class EditorItem {


  name:string;
  content:string;

  saved:boolean;
}


@Component({
             selector: 'dialog-apply',
             template: `<h1 mat-dialog-title>Apply Snowglobe Settings</h1>

             <mat-progress-bar *ngIf="processing"  mode="indeterminate"></mat-progress-bar>
             
             <div mat-dialog-content>

               <div *ngIf="!complete" >
               <h2>Settings:</h2>
               <mat-form-field class="example-full-width">
                  <textarea matInput placeholder="Settings to pass" [(ngModel)]="settings" matTextareaAutosize matAutosizeMinRows="2"
                            matAutosizeMaxRows="5"></textarea>
               </mat-form-field>

               </div>
               
               <div *ngIf="complete">
                <h2>Response:</h2>
               <!--<mat-progress-spinner *ngIf="processing" mode="indeterminate"></mat-progress-spinner>-->
               <codemirror  *ngIf="response != null" [ngModel]="response"  [config]="config">

               </codemirror>
               </div>
                 
             </div>
             <div *ngIf="!complete" mat-dialog-actions>
               <button mat-button [disabled]="processing" (click)="onApply()">Apply</button>
               <button mat-button  [disabled]="processing" (click)="onNoClick()" tabindex="-1">Cancel</button>
             </div>
             <div *ngIf="complete" mat-dialog-actions>
               
               <button mat-button (click)="onNoClick()" tabindex="-1">Dismiss</button>
             </div>
             `,
           })
export class DialogApply {

  processing:boolean = false;

   response:string;
   settings:string;

   complete:boolean = false;

  constructor(
    public dialogRef: MatDialogRef<DialogApply>, public globeService:GlobeService,
    @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  async process(id) {
    try {
      this.processing = true;
      console.log("Apply " + this.settings)
      this.response = await this.globeService.apply(id, this.settings);
    } catch(e) {
      this.response = "Error: " + e._body;
      console.log(e);
    }
    this.processing = false;
    this.complete = true;
  }

  onApply():void {
    this.process(this.data);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}

@Component({
             selector: 'dialog-clone',
             template: `<h1 mat-dialog-title>Clone Snowglobe</h1>
             <mat-progress-bar *ngIf="processing"  mode="indeterminate"></mat-progress-bar>

             <div mat-dialog-content>

               <h2>Settings:</h2>
               <mat-form-field class="example-full-width">
                 <input matInput placeholder="Name">
               </mat-form-field>
               
                <h2>Response:</h2>
                   <mat-progress-spinner *ngIf="response == null" mode="indeterminate"></mat-progress-spinner>
                   <codemirror  *ngIf="response != null" [ngModel]="response"  [config]="config">
    
                   </codemirror>
               
             </div>
             <div mat-dialog-actions>
               <button mat-button (click)="onClone()" tabindex="-1">Clone</button>
               <button mat-button (click)="onNoClick()" tabindex="-1">Cancel</button>
             </div>
             `,
           })
export class DialogClone {

  processing:boolean = false;

  response:string;
   newId:string;

  constructor(
    public dialogRef: MatDialogRef<DialogClone>, public globeService:GlobeService,
    @Inject(MAT_DIALOG_DATA) public data: any) {

  }

  async process(id) {
    try {
      this.response = await this.globeService.clone(id,this.newId);
    } catch(e) {
      this.response = "Error: " + e._body;
      console.log(e);
    }

  }

  onClone():void {
    this.process(this.data);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}


@Component({
             selector: 'dialog-destroy',
             template: `<h1 mat-dialog-title>Destroy Snowglobe</h1>
             <mat-progress-bar *ngIf="processing"  mode="indeterminate"></mat-progress-bar>

             <div mat-dialog-content>

               <div *ngIf="complete">
                 <h2>Response:</h2>
                 <!--<mat-progress-spinner *ngIf="processing" mode="indeterminate"></mat-progress-spinner>-->
                 <codemirror  *ngIf="response != null" [ngModel]="response"  [config]="config">

                 </codemirror>
               </div>

             </div>
             <div *ngIf="!complete" mat-dialog-actions>
               <button mat-button [disabled]="processing" (click)="onApply()">Destroy</button>
               <button mat-button  [disabled]="processing" (click)="onNoClick()" tabindex="-1">Cancel</button>
             </div>
             <div *ngIf="complete" mat-dialog-actions>

               <button mat-button (click)="onNoClick()" tabindex="-1">Dismiss</button>
             </div>
             `,
           })
export class DialogDestroy {
  processing:boolean = false;
  complete:boolean = false;
  response:string;
  newId:string;

  constructor(
    public dialogRef: MatDialogRef<DialogDestroy>, public globeService:GlobeService,
    @Inject(MAT_DIALOG_DATA) public data: any) {

  }

  async process(id) {
    try {
      this.processing = true;
      this.response = await this.globeService.destroy(id);
      this.processing = false;
    } catch(e) {
      this.processing = false;
      this.response = "Error: " + e._body;
      console.log(e);
    }

    this.complete = true;

  }

  onApply():void {
    this.process(this.data);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}

@Component({
             selector: 'dialog-validate',
             template: `<h1 mat-dialog-title>Validate Snowglobe</h1>
             <mat-progress-bar *ngIf="processing"  mode="indeterminate"></mat-progress-bar>

             <div mat-dialog-content>

             
               <div *ngIf="!processing">
                <h2>Response:</h2>
                    
                    <div *ngIf="response == null"><mat-icon>check</mat-icon> Validates OK</div>
                 <div *ngIf="response != null"><mat-icon>error</mat-icon> Validation Errors</div>
                   <codemirror  *ngIf="response != null" [ngModel]="response"  [config]="config">
    
                   </codemirror>
               </div>
             </div>
             <div mat-dialog-actions>
               
               <button [disabled]="processing" mat-button (click)="onNoClick()" tabindex="-1">Close</button>
             </div>
             `,
           })
export class DialogValidate {
  processing:boolean = false;
  response:string;
  newId:string;

  constructor(
    public dialogRef: MatDialogRef<DialogValidate>, public globeService:GlobeService,
    @Inject(MAT_DIALOG_DATA) public data: any) {

  }

  ngOnInit() {
    this.process(this.data);
  }

  async process(id) {
    try {
      this.processing = true;
       await this.globeService.validate(this.data);
      this.processing = false;
    } catch(e) {
      this.processing = false;
      this.response = "Error: " + e;
      console.log(e);
    }

  }

  onClone():void {
    this.process(this.data);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}



@Component({
             selector: 'dialog-delete',
             template: `<h1 mat-dialog-title>Remove Snowglobe</h1>
             <mat-progress-bar *ngIf="processing"  mode="indeterminate"></mat-progress-bar>

             <div mat-dialog-content>

               <div *ngIf="complete">
                 <h2>Response:</h2>
                 <!--<mat-progress-spinner *ngIf="processing" mode="indeterminate"></mat-progress-spinner>-->
                 <codemirror  *ngIf="response != null" [ngModel]="response"  [config]="config">

                 </codemirror>
               </div>

             </div>
             <div *ngIf="!complete" mat-dialog-actions>
               <button mat-button [disabled]="processing" (click)="onApply()">Remove</button>
               <button mat-button  [disabled]="processing" (click)="onNoClick()" tabindex="-1">Cancel</button>
             </div>
             <div *ngIf="complete" mat-dialog-actions>

               <button mat-button (click)="onNoClick()" tabindex="-1">Dismiss</button>
             </div>
             `,
           })
export class DialogDelete {
  processing:boolean = false;
  complete:boolean = false;
  response:string;
  newId:string;

  constructor(
    public dialogRef: MatDialogRef<DialogDelete>, public globeService:GlobeService,
    @Inject(MAT_DIALOG_DATA) public data: any) {

  }

  async process(id) {
    try {
      this.processing = true;
       await this.globeService.delete(id);
      this.response = "done";
      this.processing = false;
    } catch(e) {
      this.processing = false;
      this.response = "Error: " + e._body;
      console.log(e);
    }

    this.complete = true;

  }

  onApply():void {
    this.process(this.data);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
