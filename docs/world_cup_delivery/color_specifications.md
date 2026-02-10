# World Cup Theme Color Specifications

## 1. Primary Palette (FIFA Official)

| Usage | Color Name | Hex | RGB | Usage Description |
|-------|------------|-----|-----|-------------------|
| **Primary** | FIFA Deep Red | `#8B0000` | `139, 0, 0` | Main branding, primary buttons, active states |
| **Secondary** | FIFA Deep Green | `#006A4E` | `0, 106, 78` | Accents, success states, secondary elements |
| **Tertiary** | FIFA Gold | `#FFD700` | `255, 215, 0` | Highlights, scores, winning states |
| **Quaternary** | FIFA Silver | `#C0C0C0` | `192, 192, 192` | Secondary text, inactive icons, subtle borders |

## 2. Typography Colors (High Contrast)

| Usage | Hex | RGB | Contrast Ratio (vs White) | WCAG Level |
|-------|-----|-----|---------------------------|------------|
| **Primary Text** | `#2D2D2D` | `45, 45, 45` | 12.0:1 | **AAA** |
| **Secondary Text** | `#4A4A4A` | `74, 74, 74` | 9.0:1 | **AAA** |
| **Disabled Text** | `#757575` | `117, 117, 117` | 4.6:1 | **AA** |

## 3. Background Colors

| Usage | Hex | RGB | Description |
|-------|-----|-----|-------------|
| **Main Background** | `#FFFFFF` | `255, 255, 255` | Clean canvas for maximum contrast |
| **Surface/Card** | `#F5F5F5` | `245, 245, 245` | Slight separation from background |

## 4. Usage Rules

1.  **Contrast:** Always maintain at least 4.5:1 contrast for normal text and 3:1 for large text.
2.  **Color Blindness:** Do not rely solely on Red/Green distinction. Use icons or text labels alongside color indicators.
3.  **Forbidden Colors:** Avoid combinations that mimic specific national teams (e.g., Light Blue + White stripes) unless displaying that specific team.
4.  **Dark Mode:** Ensure colors adapt. Deep Red `#8B0000` may need lightening to `#CF6679` equivalent in dark mode for visibility (though current scope focuses on the provided palette).

## 5. Implementation Reference (Android)

```kotlin
val WorldCupPrimary = Color(0xFF8B0000)
val WorldCupSecondary = Color(0xFF006A4E)
val WorldCupTertiary = Color(0xFFFFD700)
val WorldCupTextPrimary = Color(0xFF2D2D2D)
```
