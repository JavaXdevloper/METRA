# SIH 2026 Compliance Portal — Design Direction

## Three Possible Directions

### Theme Name: Field Ledger
Very Brief Intro: A warm, editorial operations console inspired by bound inspection ledgers and official case files. Dense information is balanced with generous paper-like surfaces and precise signal colors.
Probability: 0.07

### Theme Name: Signal Room
Very Brief Intro: A high-contrast control-room interface with deep navy panels, amber alerts, and restrained cyan instrumentation. It emphasizes active monitoring and rapid triage.
Probability: 0.04

### Theme Name: Civic Atlas
Very Brief Intro: A daylight civic interface using quiet blue-gray fields, map-like linework, and structured cards to make compliance evidence feel legible and public-service oriented.
Probability: 0.08

## Chosen Approach: Field Ledger

### Design Movement
Contemporary editorial brutalism softened with archival government-document cues: strong typographic hierarchy, visible structure, tactile paper surfaces, and deliberately utilitarian controls.

### Core Principles
1. **Evidence first:** inspection images, declarations, violations, and rules are visually prioritized over ornament.
2. **Operational clarity:** every screen communicates state, next action, and recovery path without ambiguity.
3. **Quiet authority:** typography and spacing create confidence; color is reserved for status and urgency.
4. **Tactile recordkeeping:** thin rules, index labels, stamped status markers, and paper-like surfaces make the portal feel like a trusted official record.

### Color Philosophy
The base is deep ink navy and chalk-white paper, echoing official forms without looking dated. A signature safety amber marks active work and attention, while green and red are reserved for compliance outcomes. Muted blue-gray provides context without competing with evidence.

### Layout Paradigm
Use a persistent left rail for navigation and a wide, asymmetric working canvas. Screens are composed as a sequence of evidence panels, split columns, and anchored side notes rather than a uniform centered grid. The new-inspection workflow gets a broad primary column and a narrow operational guide column.

### Signature Elements
1. A vertical amber signal bar at the left edge of active workflow surfaces.
2. Small uppercase index labels with mono numerals for IDs, dates, rules, and evidence counts.
3. Status markers that resemble stamped annotations: clear pill labels, a dot indicator, and a compact supporting line.

### Interaction Philosophy
Interactions should feel immediate and accountable. Buttons visibly acknowledge press, upload states narrate progress, and destructive actions ask for a clear recovery path. Hover states use small shifts and rule-color changes instead of flashy effects.

### Animation
Use short 160–220ms ease-out transitions for buttons, cards, and navigation. Page sections enter with subtle upward motion and opacity, staggered by 40ms. Upload processing uses a calm progress pulse; avoid looping effects that suggest an unstable system. Respect reduced-motion preferences.

### Typography System
Display: Fraunces, 600–700, for page titles and key status statements. Body: DM Sans, 400–600, for UI text. Metadata: IBM Plex Mono, 500, uppercase and letter-spaced for IDs, timestamps, rules, and counts. Headline scale should be editorial and compact; body copy should remain highly readable at 14–16px.

### Brand Essence
A focused compliance workspace for authorized metrology officials who need to turn package evidence into defensible inspection records. Personality: **exact, calm, accountable**.

### Brand Voice
Headlines are direct and action-oriented. CTAs describe the real operation, never generic onboarding language. Microcopy is concise, respectful, and recovery-focused.

Example lines:
- “Turn package evidence into a decision.”
- “Review the declarations before filing the record.”

### Wordmark & Logo
Use a custom symbol combining a simplified package box outline with a vertical measurement tick and a small amber seal. The mark should work without text in the sidebar and favicon; the wordmark is set separately in a confident serif display face.

### Signature Brand Color
**Compliance Amber — #DFAF4B**, used for active workflow, evidence emphasis, and attention states.

## File-level reminder
Every new CSS/component/page file should begin with a short comment reminding the implementer: Field Ledger direction, evidence-first hierarchy, ink navy + paper + compliance amber, editorial serif with sans body, mono metadata, and no generic rounded dashboard treatment.
