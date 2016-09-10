import {Inject, Injectable} from "@angular/core";
import {WindowRefService} from "./windowref.service";


@Injectable()
export class AppConfig  {


  public baseUrl:String;


  private incomingHost;
  private incomingPort;


  constructor( private _windowRef: WindowRefService) {

    console.debug("Construct AppConfig");


    let _window = _windowRef.nativeWindow;

    console.debug(".... dun dun dun: " + _window.location.hostname);

    // white label = ampliview, jlr, etc
    // not white label = audaera



    this.incomingHost = _window.location.hostname;
    this.incomingPort = _window.location.port;



    if( this.incomingHost == "localhost" ) {
      // Special case for devevlopment
      let port:string = "";
      if( _window.location.port != "80" )
        port = ":" + _window.location.port;

      if( port == ":4200" ) {
        port = ":8808";
      }

      this.baseUrl = "http://localhost" + port;

      return;
    }

    this.baseUrl = _window.location.origin;


  }



}
