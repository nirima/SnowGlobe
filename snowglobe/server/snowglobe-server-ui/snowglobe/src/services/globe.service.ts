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

  async duplicate(id:string, newId:string):Promise<string> {
    let url = `${this.getBaseURL()}/globe/${id}/duplicate/${newId}`;
    console.log("duplicate " + url);

    return this.http.post(url,"")
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return responseData.text();
      }).toPromise();
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

  async apply(id:string):Promise<string> {
    let url = `${this.getBaseURL()}/globe/${id}/apply`;
    console.log("apply " + url);

    return this.http.put(url,"")
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return responseData.text();
      }).toPromise();
  }

  async applyAsync(id:string, topic:string):Promise<string> {
    let url = `${this.getBaseURL()}/globe/${id}/apply?async=${topic}`;
    console.log("apply " + url);

    return this.http.put(url,"")
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



  async create(id:string) {
    let url = `${this.getBaseURL()}/globe/${id}/create`;
    return this.http.put(url, "").toPromise();
  }

  async updateAll() {
    let url = `${this.getBaseURL()}/globes/update`;
    return this.http.get(url)
      .toPromise();
  }

  async newClone(curl:string) {
    let url = `${this.getBaseURL()}/globes/clone`;

    let cloneDetails: any = {
      url: curl
    };
    return this.http.post(url, cloneDetails).toPromise();
  }

  async newCloneWithPassword(curl:string, username:string, password:string) {
    let url = `${this.getBaseURL()}/globes/clone`;

    if( username != null && username.length > 0 ) {

      let cloneDetails: any = {
        url: curl,
        credentials: {
          username: username,
          password: password
        }
      };
      return this.http.post(url, cloneDetails).toPromise();
    }
    else {
      let cloneDetails: any = {
        url: curl
      };
      return this.http.post(url, cloneDetails).toPromise();
    }

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

  async saveState(id:string, state:string) {
    let url = `${this.getBaseURL()}/globe/${id}/state`;
    return this.http.put(url, state).toPromise();
  }

  async getVars(id: string):Promise<any> {
    let url = `${this.getBaseURL()}/globe/${id}/vars`;
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

  async saveVars(id:string, state:string) {
    let url = `${this.getBaseURL()}/globe/${id}/vars`;
    return this.http.put(url, state).toPromise();
  }

  async getTags(id: string):Promise<Array<string>> {
    let url = `${this.getBaseURL()}/globe/${id}/tags`;
    console.log("fetch " + url);



    return this.http.get(url)
      .map( (responseData) => {
        console.log('Incoming' +responseData);
        return responseData.json();
      }).toPromise();
  }

  async addTag(id:string, tag:string) {
    let url = `${this.getBaseURL()}/globe/${id}/tag/${tag}`;
    return this.http.put(url, "").toPromise();
  }

  async deleteTag(id:string, tag:string) {
    let url = `${this.getBaseURL()}/globe/${id}/tag/${tag}`;
    return this.http.delete(url).toPromise();
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
        try {
          return JSON.parse(responseData.text());
        } catch(e) {
          return null;
        }
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
