///<reference path="../../node_modules/@angular/core/src/metadata/ng_module.d.ts"/>
import {Component, NgModule} from "@angular/core";
import {MatButtonModule, MatMenuModule} from "@angular/material";

@Component({
             selector: 'app-navbar',
             templateUrl: './app.navbar.html',
             styleUrls: ['./app.navbar.scss']
           })
export class NavBar {

}

@NgModule({
            imports: [MatButtonModule, MatMenuModule],
            exports: [NavBar],
            declarations: [NavBar],
          })
export class AppNavBarModule {}
