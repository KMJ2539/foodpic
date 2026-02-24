# foodpic monorepo scaffold

## Structure

- `backend/`: Java 21 + Spring Boot 3.x API scaffold
- `frontend/`: TypeScript + Vite React scaffold
- `.github/workflows/`: CI pipelines for backend, frontend, and e2e checks

## Branch protection assumption

Repository branch protection should require **backend-test** and **frontend-test** workflows to pass before merging pull requests. The optional **frontend-e2e** workflow can also be marked as required for stricter quality gates.
