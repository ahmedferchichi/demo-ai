import {Component, OnDestroy} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpClient, HttpDownloadProgressEvent, HttpEventType} from '@angular/common/http';
import {MarkdownComponent} from 'ngx-markdown';
import {CommonModule} from '@angular/common';

interface SpeechRecognitionEvent extends Event {
  results: SpeechRecognitionResultList;
  resultIndex: number;
}

interface SpeechRecognitionErrorEvent extends Event {
  error: string;
  message: string;
}

interface SpeechRecognition extends EventTarget {
  continuous: boolean;
  interimResults: boolean;
  lang: string;
  start(): void;
  stop(): void;
  abort(): void;
  onresult: ((event: SpeechRecognitionEvent) => void) | null;
  onerror: ((event: SpeechRecognitionErrorEvent) => void) | null;
  onend: (() => void) | null;
}

declare global {
  interface Window {
    SpeechRecognition?: new () => SpeechRecognition;
    webkitSpeechRecognition?: new () => SpeechRecognition;
  }
}

@Component({
  selector: 'app-chat',
  imports: [
    FormsModule,
    MarkdownComponent,
    CommonModule
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnDestroy {
  query: string = '';
  response: string = '';
  progress: boolean = false;
  isListening: boolean = false;
  speechSupported: boolean = false;

  private recognition: SpeechRecognition | null = null;

  constructor(private http: HttpClient) {
    this.initializeSpeechRecognition();
  }

  ngOnDestroy(): void {
    if (this.recognition) {
      this.recognition.abort();
    }
  }

  private initializeSpeechRecognition(): void {
    const SpeechRecognitionAPI = window.SpeechRecognition || window.webkitSpeechRecognition;

    if (SpeechRecognitionAPI) {
      this.speechSupported = true;
      this.recognition = new SpeechRecognitionAPI();
      this.recognition.continuous = false;
      this.recognition.interimResults = true;
      const browserLang = (navigator.languages && navigator.languages.length > 0
        ? navigator.languages[0]
        : navigator.language) || 'en-US';
      this.recognition.lang = browserLang;

      this.recognition.onresult = (event: SpeechRecognitionEvent) => {
        let interimTranscript = '';
        let finalTranscript = '';

        for (let i = event.resultIndex; i < event.results.length; i++) {
          const transcript = event.results[i][0].transcript;
          if (event.results[i].isFinal) {
            finalTranscript += transcript;
          } else {
            interimTranscript += transcript;
          }
        }

        if (finalTranscript) {
          this.query = finalTranscript;
        } else if (interimTranscript) {
          this.query = interimTranscript;
        }
      };

      this.recognition.onerror = (event: SpeechRecognitionErrorEvent) => {
        console.error('Speech recognition error:', event.error);
        this.isListening = false;
        if (event.error === 'no-speech') {
          console.log('No speech detected. Please try again.');
        }
      };

      this.recognition.onend = () => {
        this.isListening = false;
      };
    } else {
      console.warn('Speech Recognition API is not supported in this browser.');
      this.speechSupported = false;
    }
  }

  toggleSpeechRecognition(): void {
    if (!this.recognition) {
      return;
    }

    if (this.isListening) {
      this.recognition.stop();
      this.isListening = false;
    } else {
      this.query = '';
      this.recognition.start();
      this.isListening = true;
    }
  }

  askAgent(): void {
    if (!this.query.trim()) {
      return;
    }

    // Stop voice recognition if active to prevent race condition
    if (this.isListening && this.recognition) {
      this.recognition.stop();
      this.isListening = false;
    }

    this.response = '';
    this.progress = true;

    this.http.get(`http://localhost:8080/api/chat/stream?message=${encodeURIComponent(this.query)}`,
      {responseType: 'text', observe: 'events', reportProgress: true})
      .subscribe({
        next: evt => {
          if (evt.type === HttpEventType.DownloadProgress) {
            this.response = (evt as HttpDownloadProgressEvent).partialText || '';
          }
        },
        error: err => {
          console.error('Error calling backend:', err);
          this.response = 'Error: Unable to connect to the backend service.';
          this.progress = false;
        },
        complete: () => {
          this.progress = false;
        }
      });
  }
}
