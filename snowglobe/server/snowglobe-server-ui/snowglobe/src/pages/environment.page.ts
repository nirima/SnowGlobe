import {Component}     from '@angular/core';


import {Injectable} from '@angular/core';

import {GlobeService} from "../services/globe.service";
import {ActivatedRoute} from "@angular/router";

@Component({
             template: `
<h1>Site {{data.fullId}}</h1>

<div *ngFor="let host of data.hosts">
    <H2>{{host.fullId}}</H2>
    
    <div *ngFor="let service of host.services">
            <H3>{{service.id}}</H3>
            <a [routerLink]="['/connection', service.fullId]">Launch</a>
    </div>
</div>
    



`

           })

@Injectable()
export class EnvironmentPage {
  data:any = {};
  id:string;


  constructor( private _route:ActivatedRoute, public globeService:GlobeService) {}



    ngOnInit() {
      console.log(this._route.params);
      this._route.params.map(p => p['id'])
        .subscribe((id) => {
          console.log(id);
          this.id = id;

          this.globeService.getSite(id).subscribe( it => this.data = it );



        });


  }

}


