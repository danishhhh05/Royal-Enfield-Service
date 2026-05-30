package com.royalenfield.service;

import java.awt.Color;

/**
 * Central color palette — Royal Enfield inspired dark theme.
 * Use these constants everywhere for consistent styling.
 */
public final class AppColors {

    private AppColors() {
    }

    // ── Base surfaces ─────────────────────────────────────────────
    /** Matte black — main window background */
    public static final Color BACKGROUND = new Color(14, 14, 16);
    /** Charcoal — cards, content panels */
    public static final Color SURFACE = new Color(28, 28, 32);
    /** Elevated panels — sidebar, navbar */
    public static final Color SURFACE_ELEVATED = new Color(36, 36, 42);
    /** Sidebar background */
    public static final Color SIDEBAR = new Color(22, 22, 26);
    /** Slightly lighter surface for hover rows */
    public static final Color SURFACE_LIGHT = new Color(48, 48, 54);

    // ── Brand accents ─────────────────────────────────────────────
    /** Primary accent — Royal Enfield red */
    public static final Color ACCENT_RED = new Color(196, 30, 58);
    public static final Color ACCENT_RED_DARK = new Color(150, 22, 44);
    public static final Color ACCENT_RED_BRIGHT = new Color(220, 50, 75);
    /** Subtle orange highlight */
    public static final Color ACCENT_ORANGE = new Color(232, 126, 54);
    public static final Color ACCENT_ORANGE_DIM = new Color(180, 95, 40);

    // ── Typography ──────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY = new Color(245, 245, 247);
    public static final Color TEXT_SECONDARY = new Color(158, 158, 168);
    public static final Color TEXT_MUTED = new Color(110, 110, 118);
    public static final Color TEXT_ON_ACCENT = Color.WHITE;

    // ── UI chrome ───────────────────────────────────────────────────
    public static final Color BORDER = new Color(55, 55, 62);
    public static final Color BORDER_FOCUS = new Color(196, 30, 58);
    public static final Color HOVER_OVERLAY = new Color(255, 255, 255, 18);
    public static final Color SELECTION = new Color(196, 30, 58, 60);

    // ── Semantic ────────────────────────────────────────────────────
    public static final Color SUCCESS = new Color(46, 160, 90);
    public static final Color WARNING = new Color(232, 126, 54);
    public static final Color INFO = new Color(72, 149, 239);

    // ── Table ───────────────────────────────────────────────────────
    public static final Color TABLE_HEADER = new Color(42, 42, 48);
    public static final Color TABLE_ROW = new Color(32, 32, 38);
    public static final Color TABLE_ROW_ALT = new Color(28, 28, 34);
    public static final Color TABLE_ROW_HOVER = new Color(50, 50, 58);
    public static final Color TABLE_GRID = new Color(45, 45, 52);

    // ── Stat card accents ───────────────────────────────────────────
    public static final Color CARD_CUSTOMERS = new Color(72, 149, 239);
    public static final Color CARD_BIKES = new Color(232, 126, 54);
    public static final Color CARD_SERVICES = new Color(196, 30, 58);
    public static final Color CARD_REVENUE = new Color(46, 160, 90);

    // ── Fonts (family names) ────────────────────────────────────────
    public static final String FONT_FAMILY = "Segoe UI";
    public static final String FONT_FALLBACK = "SansSerif";

    public static int sizeHeading() {
        return 22;
    }

    public static int sizeSubheading() {
        return 16;
    }

    public static int sizeBody() {
        return 13;
    }

    public static int sizeSmall() {
        return 11;
    }

    public static int sizeStatValue() {
        return 28;
    }
}
