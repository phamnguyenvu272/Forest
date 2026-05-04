import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface AuthResponse { token: string; userId: number; username: string; email: string; }
interface Post { id: number; title: string; content: string; authorId: number; authorUsername: string; createdAt: string; }
interface Comment { id: number; content: string; postId: number; authorId: number; authorUsername: string; createdAt: string; }

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="container">
    <h1>Forest (Version 1)</h1>
    <div class="card" *ngIf="!token()">
      <h3>Register</h3>
      <input [(ngModel)]="registerUsername" placeholder="Username" />
      <input [(ngModel)]="registerEmail" placeholder="Email" />
      <input [(ngModel)]="registerPassword" type="password" placeholder="Password" />
      <button (click)="register()">Register</button>
      <h3>Login</h3>
      <input [(ngModel)]="loginEmail" placeholder="Email" />
      <input [(ngModel)]="loginPassword" type="password" placeholder="Password" />
      <button (click)="login()">Login</button>
    </div>

    <div class="card" *ngIf="token()">
      <div class="row"><strong>Logged in as {{ currentUsername() }}</strong><button (click)="logout()">Logout</button></div>
    </div>

    <div class="card" *ngIf="token()">
      <h3>Create Post</h3>
      <input [(ngModel)]="newPostTitle" placeholder="Title" />
      <textarea [(ngModel)]="newPostContent" rows="4" placeholder="Content"></textarea>
      <button (click)="createPost()">Create</button>
    </div>

    <p class="error" *ngIf="error()">{{ error() }}</p>

    <div class="card" *ngFor="let post of posts()">
      <h3>{{ post.title }}</h3>
      <p>{{ post.content }}</p>
      <p class="small">By {{ post.authorUsername }} at {{ post.createdAt }}</p>
      <div *ngIf="token() && post.authorId === currentUserId()">
        <button (click)="startEdit(post)">Edit</button>
        <button (click)="deletePost(post.id)">Delete</button>
      </div>
      <div *ngIf="editingPostId === post.id" class="card">
        <input [(ngModel)]="editTitle" />
        <textarea [(ngModel)]="editContent" rows="3"></textarea>
        <button (click)="saveEdit(post.id)">Save</button>
        <button (click)="cancelEdit()">Cancel</button>
      </div>
      <h4>Comments</h4>
      <div class="card" *ngFor="let c of commentsByPost()[post.id] || []">
        <p>{{ c.content }}</p>
        <p class="small">{{ c.authorUsername }} - {{ c.createdAt }}</p>
      </div>
      <div *ngIf="token()">
        <textarea [(ngModel)]="commentDrafts[post.id]" rows="2" placeholder="Leave a comment"></textarea>
        <button (click)="addComment(post.id)">Comment</button>
      </div>
    </div>
  </div>
  `
})
export class AppComponent {
  private http = inject(HttpClient);
  token = signal(localStorage.getItem('forest_token') || '');
  currentUserId = signal(Number(localStorage.getItem('forest_user_id') || 0));
  currentUsername = signal(localStorage.getItem('forest_username') || '');
  posts = signal<Post[]>([]);
  commentsByPost = signal<Record<number, Comment[]>>({});
  error = signal('');

  registerUsername = ''; registerEmail = ''; registerPassword = '';
  loginEmail = ''; loginPassword = '';
  newPostTitle = ''; newPostContent = '';
  editingPostId: number | null = null; editTitle = ''; editContent = '';
  commentDrafts: Record<number, string> = {};

  constructor() { this.loadFeed(); }

  register(): void {
    this.error.set('');
    this.http.post<AuthResponse>('/api/auth/register', { username: this.registerUsername, email: this.registerEmail, password: this.registerPassword }).subscribe({
      next: (res) => this.setSession(res),
      error: (err) => this.error.set(err?.error?.error || 'Registration failed')
    });
  }

  login(): void {
    this.error.set('');
    this.http.post<AuthResponse>('/api/auth/login', { email: this.loginEmail, password: this.loginPassword }).subscribe({
      next: (res) => this.setSession(res),
      error: (err) => this.error.set(err?.error?.error || 'Login failed')
    });
  }

  logout(): void {
    localStorage.clear(); this.token.set(''); this.currentUserId.set(0); this.currentUsername.set('');
  }

  loadFeed(): void {
    this.http.get<Post[]>('/api/posts').subscribe({
      next: (data) => { this.posts.set(data); data.forEach((p) => this.loadComments(p.id)); },
      error: () => this.error.set('Unable to load feed')
    });
  }

  createPost(): void {
    this.http.post<Post>('/api/posts', { title: this.newPostTitle, content: this.newPostContent }, { headers: this.authHeaders() }).subscribe({
      next: () => { this.newPostTitle = ''; this.newPostContent = ''; this.loadFeed(); },
      error: (err) => this.error.set(err?.error?.error || 'Could not create post')
    });
  }

  startEdit(post: Post): void { this.editingPostId = post.id; this.editTitle = post.title; this.editContent = post.content; }
  cancelEdit(): void { this.editingPostId = null; this.editTitle = ''; this.editContent = ''; }

  saveEdit(postId: number): void {
    this.http.put<Post>(`/api/posts/${postId}`, { title: this.editTitle, content: this.editContent }, { headers: this.authHeaders() }).subscribe({
      next: () => { this.cancelEdit(); this.loadFeed(); },
      error: (err) => this.error.set(err?.error?.error || 'Could not update post')
    });
  }

  deletePost(postId: number): void {
    this.http.delete(`/api/posts/${postId}`, { headers: this.authHeaders() }).subscribe({
      next: () => this.loadFeed(),
      error: (err) => this.error.set(err?.error?.error || 'Could not delete post')
    });
  }

  loadComments(postId: number): void {
    this.http.get<Comment[]>(`/api/posts/${postId}/comments`).subscribe({
      next: (comments) => this.commentsByPost.update((map) => ({ ...map, [postId]: comments }))
    });
  }

  addComment(postId: number): void {
    const content = (this.commentDrafts[postId] || '').trim(); if (!content) return;
    this.http.post<Comment>(`/api/posts/${postId}/comments`, { content }, { headers: this.authHeaders() }).subscribe({
      next: () => { this.commentDrafts[postId] = ''; this.loadComments(postId); },
      error: (err) => this.error.set(err?.error?.error || 'Could not add comment')
    });
  }

  private setSession(res: AuthResponse): void {
    localStorage.setItem('forest_token', res.token);
    localStorage.setItem('forest_user_id', String(res.userId));
    localStorage.setItem('forest_username', res.username);
    this.token.set(res.token); this.currentUserId.set(res.userId); this.currentUsername.set(res.username); this.loadFeed();
  }

  private authHeaders(): HttpHeaders { return new HttpHeaders({ Authorization: `Bearer ${this.token()}` }); }
}
