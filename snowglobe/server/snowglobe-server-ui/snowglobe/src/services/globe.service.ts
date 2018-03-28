import {Http, Response, Headers, RequestOptions} from '@angular/http'
import {Injectable} from '@angular/core';
import {AppConfig} from "./appConfig";
import 'rxjs/add/operator/map';

@Injectable()
export class GlobeService {

  constructor(public http:Http, public appConfig:AppConfig){


  }

  getBaseURL() {
    return `${this.appConfig.baseUrl}/data`;
  }

  getList() {
    let url = `${this.getBaseURL()}/globes`;
    console.log("fetch " + url);

    return this.http.get(url)
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return responseData.json();
      });
  }

  async clone(id:string, newId:string):Promise<string> {
    let url = `${this.getBaseURL()}/globe/${id}/clone/${newId}`;
    console.log("clone " + url);

    return this.http.post(url,"")
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return responseData.text();
      }).toPromise();
  }

  async apply(id:string, settings:string):Promise<string> {
    let url = `${this.getBaseURL()}/globe/${id}/apply`;
    console.log("apply " + url);

    return this.http.post(url,settings)
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return responseData.text();
      }).toPromise();
  }

  async validate(id:string) {
    let url = `${this.getBaseURL()}/globe/${id}/validate`;

    return this.http.get(url)
      .toPromise();
  }

  async delete(id:string) {
    let url = `${this.getBaseURL()}/globe/${id}`;

    return this.http.delete(url)
      .toPromise();
  }


  async destroy(id:string):Promise<string> {
    let url = `${this.getBaseURL()}/globe/${id}/destroy`;

    return this.http.post(url,'')
      .map( (responseData) => { return responseData.text(); })
      .toPromise();
  }


  async getDetails(id:string):Promise<any> {
    return this.http.get(`${this.getBaseURL()}/globe/${id}`)
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return responseData.json();
      }).toPromise();
  }

  getConfig(id:string,name:string) {
    let url = `${this.getBaseURL()}/globe/${id}/config/${name}`;
    console.log("fetch " + url);

    return this.http.get(url)
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return responseData.text();
      });

  }

  async saveConfig(id:string, name:string, config:string) {
    let url = `${this.getBaseURL()}/globe/${id}/config/${name}`;
    return this.http.put(url, config).toPromise();
  }

  async saveState(id:string, state:string) {
    let url = `${this.getBaseURL()}/globe/${id}/state`;
    return this.http.put(url, state).toPromise();
  }

  async create(id:string) {
    let url = `${this.getBaseURL()}/globe/${id}/create`;
    return this.http.put(url, "").toPromise();
  }

  async newClone(id:string) {
    let url = `${this.getBaseURL()}/globes/clone/${id}`;
    return this.http.post(url, "").toPromise();
  }



  async getState(id: string):Promise<any> {
    let url = `${this.getBaseURL()}/globe/${id}/state`;
    console.log("fetch " + url);

    let headers = new Headers();
    headers.append('Accept', "text/plain");
    let opts = new RequestOptions();
    opts.headers = headers;


    return this.http.get(url, opts)
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return responseData.text();
      }).toPromise();
  }

  async getJSONState(id: string):Promise<any> {
    let url = `${this.getBaseURL()}/globe/${id}/state`;
    console.log("fetch " + url);

    let headers = new Headers();
    headers.append('Accept', "application/json");
    let opts = new RequestOptions();
    opts.headers = headers;


    return this.http.get(url, opts)
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return JSON.parse(responseData.text());
      }).toPromise();
  }

  getSites() {
    let url = `${this.getBaseURL()}/customers`;
    console.log("fetch " + url);

    return this.http.get(url)
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return responseData.json();
      });

  }

  getSite(id:string) {
    let url = `${this.getBaseURL()}/site/${id}`;
    console.log("fetch " + url);

    return this.http.get(url)
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return responseData.json();
      });

  }


}
