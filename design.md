# Sprintly — Design System & UI/UX Style Guide
**Version:** 1.0  
**Theme:** SaaS Admin / Modern Agile Tool  
**Primary Palette:** Deep Amethyst + Pure White  

---

## 1. Brand Identity
- **Product Name:** Sprintly
- **Tagline:** *Ship Sprints. Ship Code. Faster.*
- **Vibe:** Professional, clean, spacious, and trustworthy. The purple conveys wisdom, creativity, and control, while the white space ensures clarity and reduces cognitive load for developers.

---

## 2. Color System

We strictly adhere to a **Purple + White** minimalistic palette to maintain a premium SaaS feel.

### Primary Palette (Purple Spectrum)
| Role | Name | Hex Code | Usage |
| :--- | :--- | :--- | :--- |
| **Primary Base** | Vivid Amethyst | `#7C3AED` | Main CTAs, Primary Buttons, Active Links, Icons |
| **Primary Dark** | Deep Indigo | `#4C1D95` | Hover states on primary buttons, Footer/Headers |
| **Primary Light** | Soft Lavender | `#EDE6FF` | Backgrounds for hover states, selected menu items, progress bars |
| **Primary Glow** | Ultramarine | `#A78BFA` | Accent borders, focus rings, subtle highlights |

### Neutral Palette (White + Grays)
| Role | Name | Hex Code | Usage |
| :--- | :--- | :--- | :--- |
| **Base** | Pure White | `#FFFFFF` | Main backgrounds, card backgrounds, input fields |
| **Off-White** | Cloud Whisper | `#F9F7FC` | Sidebar backgrounds, page wrappers (to contrast white cards) |
| **Border** | Silver Mist | `#E5E1EB` | Dividers, card borders, input borders (default) |
| **Text - Secondary** | Cool Gray | `#6B6B7B` | Subtitles, placeholder texts, helper texts |
| **Text - Primary** | Dark Ink | `#1E1B2E` | Main body text, headings (almost black, with a purple tint) |

### Semantic / Status Colors (Keep them subtle)
| Status | Color | Usage |
| :--- | :--- | :--- |
| **Success (Done)** | `#10B981` (Emerald) | Done status badges, success messages |
| **Warning (In Progress)**| `#F59E0B` (Amber) | In-Progress status badges |
| **Info / Planned** | `#3B82F6` (Blue) | Planned/To-Do status badges |
| **Danger** | `#EF4444` (Red) | Delete actions, critical errors |

---

## 3. Typography

We use **Inter** as the system font (fallback to `system-ui`). It renders beautifully on both Mac and Windows.

### CSS Import
```css
@import url('https://fonts.googleapis.com/css2?family=Inter:ital,wght@0,400;0,500;0,600;0,700;1,400&display=swap');

* {
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
Text Scale
Element	Weight	Size	Line-Height	Color
H1 (Page Title)	700 (Bold)	2.25rem (36px)	2.5rem	#1E1B2E
H2 (Section Title)	600 (Semi-Bold)	1.5rem (24px)	2rem	#1E1B2E
H3 (Card Title)	600 (Semi-Bold)	1.125rem (18px)	1.75rem	#1E1B2E
Body (Regular)	400 (Regular)	0.95rem (15px)	1.5rem	#1E1B2E
Small / Meta	400 (Regular)	0.8rem (13px)	1.25rem	#6B6B7B
Button Text	500 (Medium)	0.9rem (14px)	1	#FFFFFF or #7C3AED
4. Layout & Spacing Scale
We follow an 8px Grid System. All spacing, padding, and margins should be multiples of 8.

Token	Value	Usage
xs	8px	Gap between icons and text, compact spacing
sm	16px	Padding inside cards, gap between form groups
md	24px	Padding for modals, section spacing
lg	32px	Main page padding, large gap between sections
xl	48px	Top margin for pages
Global Layout Structure:

css
/* Page Wrapper */
.page-container {
    padding: 32px;
    background: #F9F7FC; /* Off-white background */
    min-height: 100vh;
}
5. UI Components Styles
A. Buttons
Shape: Rounded-lg (8px border-radius).

Transition: Hover should have a slight scale or shadow lift (0.2s ease).

Primary Button (Purple)

css
.btn-primary {
    background: #7C3AED;
    color: #FFFFFF;
    padding: 10px 24px;
    border: none;
    border-radius: 8px;
    font-weight: 500;
    box-shadow: 0 4px 6px -1px rgba(124, 58, 237, 0.2);
    transition: all 0.2s ease;
}
.btn-primary:hover {
    background: #4C1D95; /* Darker Purple */
    transform: translateY(-2px);
    box-shadow: 0 8px 15px -3px rgba(124, 58, 237, 0.4);
}
Secondary Button (Outline)

css
.btn-secondary {
    background: transparent;
    color: #7C3AED;
    border: 1.5px solid #7C3AED;
    padding: 10px 24px;
    border-radius: 8px;
    font-weight: 500;
}
.btn-secondary:hover {
    background: #EDE6FF; /* Light purple background */
}
B. Cards / Containers
Background: #FFFFFF (Pure White).

Border: 1px solid #E5E1EB.

Shadow: Subtle elevation.

css
.card {
    background: #FFFFFF;
    border: 1px solid #E5E1EB;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.02), 0 10px 20px -10px rgba(76, 29, 149, 0.08);
    transition: box-shadow 0.3s ease;
}
.card:hover {
    box-shadow: 0 10px 30px -10px rgba(124, 58, 237, 0.15);
}
C. Forms & Inputs
Label: Semi-bold, #1E1B2E, size 14px.

Input: White background, #E5E1EB border. Focus state must glow Purple.

css
.input-field {
    width: 100%;
    padding: 12px 16px;
    border: 1.5px solid #E5E1EB;
    border-radius: 8px;
    background: #FFFFFF;
    color: #1E1B2E;
    transition: 0.2s;
}
.input-field:focus {
    outline: none;
    border-color: #7C3AED;
    box-shadow: 0 0 0 4px rgba(124, 58, 237, 0.15);
}
D. Badges / Status Tags (For Kanban)
Small, rounded-full pills.

css
.badge-todo { background: #EBF2FF; color: #3B82F6; }
.badge-progress { background: #FEF3C7; color: #D97706; }
.badge-done { background: #D1FAE5; color: #047857; }
E. Avatars
Shape: Rounded-full (50px diameter if large).

Border: 2px solid #FFFFFF (to pop against purple sidebars).

Placeholder: Use initials (e.g., "JD") with a subtle purple background #EDE6FF and purple text #7C3AED.

6. Navigation (Sidebar + Topbar)
Layout: Fixed left sidebar + Top Header (Mandatory for SaaS).

Sidebar (Purple Gradient)
Background: Linear gradient from #4C1D95 to #7C3AED (or simply #4C1D95).

Width: 260px.

Brand Logo: White text (Sprintly) + a small rocket icon (purple glow).

Nav Items: Icons + White text (opacity 0.7 default, 1.0 for active).

Active State: Left border highlight (white/light purple) + background rgba(255,255,255,0.1).

css
.sidebar {
    background: linear-gradient(180deg, #4C1D95 0%, #6C2BD9 100%);
    width: 260px;
    min-height: 100vh;
    padding: 24px 16px;
}
.sidebar .nav-link {
    color: rgba(255,255,255,0.7);
    padding: 12px 16px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 12px;
    transition: 0.2s;
}
.sidebar .nav-link.active {
    background: rgba(255,255,255,0.15);
    color: #FFFFFF;
    font-weight: 500;
}
Top Bar
Background: rgba(255,255,255,0.8) with backdrop blur (Glass effect).

Border-bottom: 1px solid #E5E1EB.

Right Side: Search bar (light purple border) + User Profile dropdown.

7. Page-Specific Layouts
A. Login / Register Page (Full Screen)
Background: Clean White with a subtle abstract purple gradient blob in the corner.

Card: Centered vertically/horizontally.

Width: Max 420px.

Button: Full width, purple primary.

Typography: Big purple heading "Welcome to Sprintly".

B. Kanban Board (Sprint View)
Columns: 3 equal width cards (To Do, In Progress, Done).

Column Header: Small title with a colored dot indicating the status.

Task Cards: Draggable (using react-beautiful-dnd), White background, small shadow, left border color matching status (e.g., Blue = To Do, Amber = In Progress, Green = Done).

Task details: Show Title, Assignee avatar (small), and a priority flag (High/Medium/Low).

C. Dashboard Analytics
Stat Cards: 4 small cards across the top (Total Tasks, In Progress, Done, Overdue).

Progress Ring: A circular SVG/canvas progress ring in Vivid Purple #7C3AED showing sprint completion.

Chart Placeholders: Clean white cards containing pure CSS bar charts (or Chart.js) where bars are colored using the purple palette.

8. Elevation (Shadows) System
Level	Token	Usage
Low	0 1px 3px rgba(0,0,0,0.02)	Cards, Tables
Medium	0 4px 15px rgba(124, 58, 237, 0.1)	Dropdowns, Hover states
High	0 15px 40px -10px rgba(76, 29, 149, 0.25)	Modals, Floating action buttons
9. Iconography
Library: Lucide React (Preferred) or Font Awesome.

Style: Stroke-based, 1.5px line thickness.

Color: Inherit text color, default to #6B6B7B, active to #7C3AED.

10. Global CSS Variables (Implementation Ready)
Put this in your index.css or App.css to instantly theme your application:

css
:root {
    --color-primary: #7C3AED;
    --color-primary-dark: #4C1D95;
    --color-primary-light: #EDE6FF;
    --color-primary-glow: #A78BFA;
    
    --color-bg: #F9F7FC;
    --color-white: #FFFFFF;
    --color-border: #E5E1EB;
    
    --color-text: #1E1B2E;
    --color-text-secondary: #6B6B7B;
    
    --color-success: #10B981;
    --color-warning: #F59E0B;
    --color-danger: #EF4444;
    --color-info: #3B82F6;

    --shadow-sm: 0 1px 3px rgba(0,0,0,0.02), 0 10px 20px -10px rgba(76, 29, 149, 0.08);
    --shadow-md: 0 4px 15px rgba(124, 58, 237, 0.10);
    --shadow-lg: 0 15px 40px -10px rgba(76, 29, 149, 0.25);
}
Design Principles to Remember:

Whitespace: Don't clutter the UI. Give elements room to breathe (use padding and margin generously).

Contrast: Always check that purple text (or white text on purple) meets AA accessibility standards.

Consistency: If a button is rounded on the login page, it must be rounded everywhere.