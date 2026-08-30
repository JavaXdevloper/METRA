# Full-stack and file-storage integration

- [x] Upgrade the existing static project to the full-stack web-db-user template.
- [x] Review generated full-stack schema, storage helpers, tRPC client, and auth scaffolding.
- [x] Add persistent inspection and evidence metadata models without storing binary files in the database.
- [x] Add protected procedures for issuing upload URLs, recording uploaded evidence, creating inspections, and reading inspection records.
- [x] Replace the demo-only upload path with managed storage upload and backend record creation.
- [x] Preserve graceful loading, upload failure, authentication failure, and retry states.
- [x] Run schema generation/migration, type checks, tests, and production build.
- [x] Save a checkpoint and deliver the upgraded project.

- [x] Locate the original generated SIH image files and verify their exact filenames and dimensions.
- [x] Audit the entire project for `/manus-storage/` references and identify additional local-runtime risks.
- [x] Copy the original assets into `client/public/` with the requested local filenames.
- [x] Update `client/src/App.tsx` to reference local public assets.
- [x] Run local checks/build and save a checkpoint with the asset package.

- [x] Add an `inspections` table plus an inspection-to-stored-files relationship.
- [x] Implement protected tRPC procedures for creating inspections, listing inspections, fetching details, and associating evidence.
- [x] Replace the demo `localStorage` auth flow with useAuth and Manus OAuth protected routing.
- [x] Update NewInspection to create a persisted inspection and navigate to backend-backed result/details pages.
- [x] Add explicit retry controls and complete auth/upload error states.
- [x] Deliver the upgraded full-stack project after a fresh checkpoint.

- [x] Replace the always-visible sidebar sign-out control with a clickable officer profile menu.
- [x] Add minimal dropdown styling, outside-click handling, and menu action behavior without changing navigation or branding.
- [x] Run npm run build and save a checkpoint for the sidebar-only update.

- [x] Preserve and verify the sidebar profile sign-out popup behavior.
- [x] Change all visible application text to Times New Roman using only index.css.
- [x] Add a subtle CSS pulse animation only to the System operational green dot.
- [x] Run the requested build and verify only App.tsx/index.css changed for implementation.

- [x] Remove the View profile button from the sidebar officer dropdown while preserving Sign out behavior.
- [x] Run npm run build and save a checkpoint for the focused sidebar change.

- [x] Inspect App.tsx usage of trpc, useAuth, and startLogin.
- [x] Audit whether broken imports needed removal. No removal was made because all three imports are required and resolve in the active project; removing them would break existing functionality.
- [x] Run npm run build and record the import-resolution validation. Build passed; no source change was required in the active project.

- [x] Remove trpc, useAuth, and startLogin imports and dependent full-stack calls from App.tsx.
- [x] Preserve localStorage/JWT-style login, logout, routes, sidebar, and mock inspection behavior in App.tsx.
- [x] Run npm run build and save a checkpoint for the standalone App.tsx rewrite.

- [x] Change login labeling and localStorage auth to support a persisted username.
- [x] Add an in-App.tsx signup form with username/password confirmation validation.
- [x] Display the logged-in username and generated initials in the dashboard/sidebar while preserving logout.
- [x] Add inline hover/focus lift styling to the four dashboard statistic cards only.
- [x] Run npm run build, verify only App.tsx changed for this request, and save a checkpoint.

- [x] Make the main content comfortably scrollable while keeping the sidebar static.
- [x] Increase readable application typography without forcing all content into one viewport.
- [x] Stack Sign up below Continue securely and update the username placeholder to a random name.
- [x] Change the dashboard greeting to Hello, username and add Change password to the profile menu.
- [x] Run npm run build and save a checkpoint for the viewport and interaction update.

- [x] Change the username placeholder to “e.g. Kavita Nair”.
- [x] Run npm run build and save a checkpoint for the focused placeholder change.

- [x] Remove unused full-stack imports and query-cache/mutation subscriptions from client/src/main.tsx.
- [x] Preserve the standalone App bootstrap and run npm run build.
- [x] Save a checkpoint for the main.tsx-only fix.
