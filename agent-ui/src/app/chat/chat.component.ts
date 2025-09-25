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
  response : string = "";
  progress : boolean = false;
  constructor(private http : HttpClient) {
  }
  askAgent() {
    this.response="";
    this.progress=true;

    this.http.get(`http://localhost:8080/api/chat/stream?message=${encodeURIComponent(this.query)}`,
      {responseType:'text', observe : 'events', reportProgress : true})
      .subscribe({
        next:evt => {
          if( evt.type === HttpEventType.DownloadProgress){
            this.response =  (evt as HttpDownloadProgressEvent).partialText || '';
          }

        },
        error : err => {
          console.error('Error calling backend:', err);
          this.response = 'Error: Unable to connect to the backend service.';
          this.progress = false;
        },
        complete :() => {
          this.progress = false;
        }
      })
  }
}
