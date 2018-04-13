
import {Subject} from "rxjs/Subject";
import {Injectable} from "@angular/core";
import {AppConfig} from "./appConfig";


export class ProgressInstance {

  private connected = false;

  public onMessage = new Subject<any>();
  public onClosed = new Subject<any>();

  constructor(public ps: ProgressService, public ws: WebSocket) {

  }

  public async connect():Promise<any> {

    // Bug out if already connected
    if( this.connected )
      return Promise.resolve(true);


    let wsx = this.ws;

    var om = this;

    return new Promise((resolve, reject) => {

      wsx.onopen = function() {
        om.connected = true;
        resolve(true);
      };

      wsx.onmessage = function (evt) {
        console.log("Received message " + evt);
        console.log(evt);

        // we are in receipt of a message./
        om.onMessage.next(evt.data);

      };

      wsx.onclose = function () {
        om.onClosed.next();
        om.connected = false;

      }  ;
      wsx.onerror = function (err) {
        console.log("error!");
        reject(err);
      } ;


    } );



  }

  public send(data) {
    console.log("Send to websocket " + data);
    this.ws.send(data);
  }

}

@Injectable()
export class ProgressService {

  constructor(public appConfig:AppConfig){
    console.log(`Creating ProgressService ${appConfig}`);
  }

  public progressForTopic(topic:String):ProgressInstance {
    let pi = new ProgressInstance(this,  new WebSocket(`${this.appConfig.ws}/progress?topic=${topic}`));


    return pi;
  }


}
