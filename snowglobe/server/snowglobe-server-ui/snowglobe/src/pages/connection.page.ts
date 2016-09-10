import {Component, ViewChild}     from '@angular/core';

import {Injectable} from '@angular/core';


import { HTTPTunnel, Client, Mouse, Keyboard } from 'guacamole-js/guacamole'
import {ActivatedRoute} from "@angular/router";
import {GlobeService} from "../services/globe.service";
@Component({

             template: `

              <!-- Display -->
              <div #display id="display"></div>

             <!-- Guacamole JavaScript API -->
        

`
})

@Injectable()
export class ConnectionPage {

  @ViewChild('display') display;
  id:string;

  constructor( private _route:ActivatedRoute, public globeService:GlobeService) {}


  ngOnInit() {
    console.log(this._route.params);
    this._route.params.map(p => p['id'])
      .subscribe((id) => {
        console.log(id);
        this.id = id;

        this.launch(id);


      });


  }


  launch(id:string) {
    //Guacamole.HTTPTunnel();

    var tun = new HTTPTunnel("http://localhost:8808/tunnel/" + id);

// Instantiate client, using an HTTP tunnel for communications.
    var guac = new Client( tun );

// Add client to display div
    this.display.nativeElement.appendChild(guac.getDisplay().getElement());

// Error handler
    guac.onerror = function(error) {
      alert(error);
    };
// Connect
    guac.connect();
// Disconnect on close
    window.onunload = function() {
      guac.disconnect();
    }
// Mouse
    var mouse = new Mouse(guac.getDisplay().getElement());
    mouse.onmousedown =
      mouse.onmouseup   =
        mouse.onmousemove = function(mouseState) {
          guac.sendMouseState(mouseState);
        };
// Keyboard
    var keyboard = new Keyboard(document);
    keyboard.onkeydown = function (keysym) {
      guac.sendKeyEvent(1, keysym);
    };
    keyboard.onkeyup = function (keysym) {
      guac.sendKeyEvent(0, keysym);
    };
  }
}


