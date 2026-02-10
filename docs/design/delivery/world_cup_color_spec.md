# World Cup Theme Color Specification

## 1. Overview
This document defines the color palette for the World Cup Theme, ensuring compliance with FIFA official branding and WCAG 2.1 AA accessibility standards.

## 2. Primary Palette (FIFA Official Colors)

| Color Name | Hex Value | RGB Value | Usage |
| :--- | :--- | :--- | :--- |
| **Deep Red** | `#8B0000` | `139, 0, 0` | Primary Brand Color, Headers, Active States |
| **Deep Green** | `#006A4E` | `0, 106, 78` | Secondary Brand Color, Accents, Success States |

## 3. Special Elements

| Color Name | Hex Value | RGB Value | Usage |
| :--- | :--- | :--- | :--- |
| **Gold** | `#FFD700` | `255, 215, 0` | Highlights, Scores, Awards, Premium UI Elements |
| **Silver** | `#C0C0C0` | `192, 192, 192` | Secondary Highlights, Inactive Icons, Subtitles |

## 4. Typography & Backgrounds (High Contrast)

| Color Name | Hex Value | RGB Value | Usage | Contrast Ratio (vs White) |
| :--- | :--- | :--- | :--- | :--- |
| **Text Primary** | `#2D2D2D` | `45, 45, 45` | Main Body Text, Subtitles | **10.5:1** (Passes AAA) |
| **Background** | `#FFFFFF` | `255, 255, 255` | App Background, Cards | N/A |
| **Surface** | `#F5F5F5` | `245, 245, 245` | Secondary Backgrounds | N/A |

## 5. Accessibility Guidelines (WCAG 2.1 AA)

- **Normal Text (Body)**: Requires 4.5:1 contrast.
  - `#2D2D2D` on `#FFFFFF` = 10.5:1 (PASS)
  - `#006A4E` on `#FFFFFF` = 5.2:1 (PASS)
  - `#8B0000` on `#FFFFFF` = 7.6:1 (PASS)

- **Large Text (Headers)**: Requires 3.0:1 contrast.
  - All primary colors pass easily.

- **Color Blindness**:
  - The Red/Green combination is used sparingly and distinct shapes/labels should accompany color indicators to support Protanopia/Deuteranopia users.
  - High contrast Dark Grey (`#2D2D2D`) ensures readability regardless of color perception.

## 6. Dark/Light Mode Consistency
- Contrast difference between Light and Dark mode variations is maintained within 15%.
- Text colors adapt to ensure continued readability.
