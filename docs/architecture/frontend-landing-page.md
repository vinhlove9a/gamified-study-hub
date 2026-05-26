# Frontend Landing Page

## Purpose
The landing page is the public product entry point for Gamified Study Hub. It is currently based on the lp-corporate template, with Nuxt and GSAP removed so it can run inside the existing Vue + Vite app.

## External Visual Reference
- The landing page is a direct port of the Nuxt 3 template at /Users/vinhlove9a/Desktop/lp-corporate.
- Nuxt-specific structure and GSAP animation hooks were removed to keep the current Vue + Vite stack.
- Template images and product icons were copied into the main app to match the visual structure.
- The external repository README claims MIT, but no LICENSE file exists in the repo root. License status is unverified and requires confirmation before production use.

## Refactor Status
- The landing page was replaced with a direct component port from lp-corporate.
- Assets were copied into apps/frontend public and src assets to match the template.
- Nuxt-only code and GSAP animation wiring were removed.

## Content Direction
- All visible landing copy has been rebranded to Vietnamese content for Gamified Study Hub.
- UI structure, sections, interactions, animations, and assets remain from the lp-corporate visual template port.
- Copy now targets learning communities, university clubs, tutors, study groups, and small education centers.

## Visual Direction
The landing page follows the lp-corporate template visual language:
- Earthy, industrial palette with warm highlights
- Soft neutral backgrounds and high-contrast typography
- Large hero imagery and structured section dividers
- Dense product iconography in the product section
- `asiaLow.svg` is reused across sections, including `LandingGeographic.vue` and as a subtle decorative background layer in the footer area of `LandingContact.vue`.

## Typography Decision
- Body and display typography use `Plus Jakarta Sans` to match the template.
- Headings rely on medium-heavy weights with tighter line height to preserve the template feel.

## Color System
| Token | Value | Usage |
|-------|-------|-------|
| `--color-primary` | `#c45b28` | Primary brand accent |
| `--color-secondary` | `#2a5c55` | Secondary brand accent |
| `--color-accent` | `#d4a24c` | Highlight accents |
| `--color-background` | `#f5f0e8` | Page background |
| `--color-dark` | `#2c2416` | Primary text |

## Final Component Map
Page composition:
- `src/pages/LandingPage.vue`

Landing sections:
- `src/components/landing/LandingNavigation.vue`
- `src/components/landing/LandingHero.vue`
- `src/components/landing/LandingStats.vue`
- `src/components/landing/LandingSustainability.vue`
- `src/components/landing/LandingMission.vue`
- `src/components/landing/LandingGeographic.vue`
- `src/components/landing/LandingProduct.vue`
- `src/components/landing/LandingContact.vue`

Small shared primitives:
- `src/components/ui/FlowParticles.vue`
- `src/components/ui/NpiLogo.vue`
- `src/components/ui/SectionDivider.vue`
- `src/components/ui/SkipLink.vue`
- `src/components/ui/icons/*`

## Custom Utilities and Classes
Defined in `src/styles/main.css`:
- `section-label`: Uppercase micro-label used across sections.
- `text-balance`, `hide-scrollbar`, `h-screen-80`, `h-screen-100` utilities.

## Responsive Strategy
- Mobile-first spacing with stacked sections on small screens.
- Navigation collapses into a mobile menu.
- Product grid and stats tiles collapse to one column on mobile and expand at tablet/desktop widths.

## Accessibility Notes
- Navigation uses semantic `nav` and skip-link support.
- Decorative background and accent layers use `aria-hidden` and `pointer-events-none`.
- Buttons and links have visible focus states.
- Reduced-motion preferences are respected globally.
- Footer in `LandingContact.vue` uses semantic `footer` and grouped `nav` regions, including product links, audience links, resource links, and developer contact links with clear focus-visible states.

## Known Limitations
- CTA links are currently landing-only actions and are not connected to auth or backend flows.
- External Google Font loading is used for `Plus Jakarta Sans`; production hardening should decide whether to self-host fonts.
- External template license is unverified and must be confirmed before production use.

## Future Integration Notes
- CTA links should route to login/register pages once auth UI exists.
- Product screenshots can replace template imagery in a later visual pass if needed.
- Confirm licensing or replace template assets with owned alternatives.
