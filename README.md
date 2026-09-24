# fullstack-craft

> A fullstack learning hub for building modern apps with **React + TypeScript**, **Java Spring Boot**, and **AI model integration**.

## Tech Stack

### Frontend — React + TypeScript
- **React 18 + TypeScript** for type-safe, component-based UI
- **Vite** for fast dev + build
- **React Router** for routing, **TanStack Query / Axios** for data fetching
- **Tailwind CSS / shadcn** for styling

### Backend — Java Spring
- **Java 17+ + Spring Boot 3** (Spring Web, Spring Data JPA, Spring Security)
- **REST APIs** consumed by the React frontend
- **PostgreSQL / MySQL** with Flyway/Liquibase migrations
- **Maven / Gradle** builds, JWT auth, validation + exception handling

### AI Model Integration
- **REST / OpenAI-compatible endpoints** for chat, embeddings, summarization
- **Spring AI / LangChain4j** on the backend to orchestrate prompts + tools
- **Vector store (pgvector / Qdrant)** for RAG over docs/code
- **Streaming (SSE)** to React for token-by-token UI updates

### How it fits together
```
[React + TS (Vite)] --REST/SSE--> [Spring Boot API] --SDK/REST--> [AI Model + Vector DB]
```

Frontend learning hub — fullstack craft projects, notes, and experiments.

## Connect Local Project to Remote Repo (Git + GitHub)

Your current remote:

```bash
git remote -v
# origin  git@github.com:tukue/fullstack-craft.git (fetch)
# origin  git@github.com:tukue/fullstack-craft.git (push)
```

### 1. First-time setup (new local project)

```bash
# 1. Initialize git
git init

# 2. Check status
git status

# 3. Stage all files
git add .

# 4. Commit
git commit -m "Initial commit"

# 5. Rename branch to main (GitHub default)
git branch -M main

# 6. Add remote (SSH)
git remote add origin git@github.com:tukue/fullstack-craft.git

# Or with HTTPS:
# git remote add origin https://github.com/tukue/fullstack-craft.git

# 7. Verify remote
git remote -v

# 8. Push to GitHub and set upstream
git push -u origin main
```

### 2. Daily workflow

```bash
# Check status / branch
git status
git branch

# Pull latest before working
git pull origin main

# Stage + commit + push
git add .
git commit -m "Your message"
git push
```

### 3. Connect an existing repo / clone it elsewhere

```bash
# Clone via SSH
git clone git@github.com:tukue/fullstack-craft.git

# Or via HTTPS
git clone https://github.com/tukue/fullstack-craft.git

cd fullstack-craft
```

### 4. Fix / change remote URL

```bash
# List remotes
git remote -v

# Remove old remote
git remote remove origin

# Add correct one
git remote add origin git@github.com:tukue/fullstack-craft.git

# Push again
git push -u origin main
```
