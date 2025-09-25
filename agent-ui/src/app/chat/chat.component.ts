import {Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpClient, HttpDownloadProgressEvent, HttpEventType} from '@angular/common/http';
import {MarkdownComponent} from 'ngx-markdown';

@Component({
  selector: 'app-chat',
  imports: [
    FormsModule,
    MarkdownComponent
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent {
  query : string ="";
  response : any ;
  progress : boolean = false;
  constructor(private http : HttpClient) {
  }
  askAgent() {
    this.response="";
    this.progress=true;
    this.http.get("http://localhost:8087/askAgent?query="+this.query,
      {responseType:'text', observe : 'events', reportProgress : true})
      .subscribe({
        next:evt => {
          if( evt.type === HttpEventType.DownloadProgress){
            this.response =  (evt as HttpDownloadProgressEvent).partialText
          }

        },
        error : err => {},
        complete :() => {
          this.progress = false;
        }
      })
  }
}
