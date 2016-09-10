import {Injectable} from "@angular/core";
//import {AppConfig} from "../appConfig";


function getWindow (): any {
  return window;
}

@Injectable()
export class WindowRefService {

  constructor() {
    console.debug("Construct WindowRefService");
  }

  get nativeWindow (): any {
    return  getWindow();
  }

  /*get appConfig():AppConfig {
    return new AppConfig(this);
  } */
}
