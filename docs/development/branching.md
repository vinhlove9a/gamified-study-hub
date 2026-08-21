# Branching & Commit Convention

Keep the repository small and easy to navigate: **`main` is the only long-lived
branch**. Everything else is a short-lived topic branch that is deleted right
after it merges.

## Branch naming

Format: `<type>/<short-kebab-case-description>`

```
feat/email-rate-limit
fix/oauth-redirect-origin
refactor/auth-session-service
chore/bump-spring-boot
ci/cache-maven-deps
docs/branching-convention
test/auth-controller-coverage
security/csrf-cookie-hardening
```

| `type`      | Use for                                             |
|-------------|-----------------------------------------------------|
| `feat/`     | New feature or user-facing capability               |
| `fix/`      | Bug fix                                             |
| `refactor/` | Restructure without changing behavior               |
| `chore/`    | Config, dependencies, tooling, housekeeping         |
| `ci/`       | CI/CD pipeline, GitHub Actions                      |
| `docs/`     | Documentation only                                  |
| `test/`     | Adding or fixing tests only                         |
| `security/` | Security hardening / vulnerability fix              |

Guidelines:
- Keep it short and descriptive — the type prefix already carries the intent.
- Optionally suffix an issue number: `feat/email-rate-limit-42`.
- Lowercase, words separated by `-` (kebab-case).

## Commit messages — Conventional Commits

`<type>(<optional-scope>): <summary in imperative mood>`

```
feat(auth): add MFA TOTP enrollment
fix(oauth): resolve redirect origin from Host header
security(auth): rate-limit forgot-password and resend-verification
```

Use the same `type` set as branches. Keep the summary under ~72 chars; put the
"why" in the body.

## Workflow

1. Branch off the latest `main`:
   ```bash
   git switch main && git pull
   git switch -c feat/my-thing
   ```
2. Commit in small, logical steps using Conventional Commits.
3. Push and open a Pull Request into `main`.
4. Merge (prefer **Squash and merge** to keep `main` history linear).
5. **Delete the branch** after merge — locally and on the remote.

## The three rules that keep the repo from bloating

1. **Short-lived branches** — open from `main`, delete immediately after merge.
2. **Only `main` is permanent** — no long-lived `develop`/`release` branches for
   a project this size.
3. **Auto-delete on merge** — enable GitHub's *Settings → General → "Automatically
   delete head branches"* so merged branches never pile up.

> Note: pushing to `main` triggers the CI/CD pipeline (build → GHCR → deploy to
> EC2). Treat `main` as production.
