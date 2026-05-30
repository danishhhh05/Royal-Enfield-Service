package com.royalenfield.service;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Main application shell — sidebar, header, and dashboard home view.
 */
public class Dashboard extends JFrame {

    private static final int CORNER_RADIUS = 16;
    private static final int BTN_RADIUS = 12;
    private static final int SIDEBAR_WIDTH = 240;

    private final JPanel contentHost = new JPanel(new BorderLayout());
    private final JPanel dashboardView = buildDashboardView();
    private final JLabel clockLabel = new JLabel();
    private final JLabel dbStatusLabel = new JLabel();
    private final JLabel pageTitleLabel = new JLabel("Dashboard");

    private final Runnable refreshAll = this::refreshAllData;
    private final CustomerPanel customerPanel = new CustomerPanel(refreshAll);
    private final ServicePanel servicePanel = new ServicePanel(refreshAll);
    private final ReportsPanel reportsPanel = new ReportsPanel();
    private final SettingsPanel settingsPanel = new SettingsPanel(refreshAll);

    private NavButton activeNav;
    private NavButton dashNav;
    private NavButton customersNav;
    private NavButton servicesNav;
    private NavButton reportsNav;
    private NavButton settingsNav;

    public Dashboard() {
        super("Royal Enfield — Service Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppColors.BACKGROUND);
        getContentPane().setLayout(new BorderLayout(0, 0));

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainArea(), BorderLayout.CENTER);

        showView(dashboardView);
        startClockTimer();
        refreshStatistics();
    }

    // ── Layout builders ─────────────────────────────────────────────

    private JPanel buildSidebar() {
        RoundedPanel sidebar = new RoundedPanel(AppColors.SIDEBAR, 0);
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        sidebar.add(buildBrandPanel(), BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(28, 0, 0, 0));

        dashNav = createNav("Dashboard", "\u25A3", dashboardView, "Dashboard", true);
        customersNav = createNav("Customers", "\u263A", customerPanel, "Customers", false);
        servicesNav = createNav("Services", "\u2699", servicePanel, "Service Records", false);
        reportsNav = createNav("Reports", "\u2637", reportsPanel, "Reports", false);
        settingsNav = createNav("Settings", "\u2692", settingsPanel, "Settings", false);

        nav.add(dashNav);
        nav.add(Box.createVerticalStrut(6));
        nav.add(customersNav);
        nav.add(Box.createVerticalStrut(6));
        nav.add(servicesNav);
        nav.add(Box.createVerticalStrut(6));
        nav.add(reportsNav);
        nav.add(Box.createVerticalStrut(6));
        nav.add(settingsNav);

        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(buildSidebarFooter(), BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildBrandPanel() {
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel logoMark = label("RE", boldFont(26), AppColors.ACCENT_RED);
        logoMark.setAlignmentX(0.5f);
        logoMark.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.ACCENT_RED, 2, true),
                new EmptyBorder(4, 10, 4, 10)));

        JLabel title = label("ROYAL ENFIELD", boldFont(14), AppColors.TEXT_PRIMARY);
        title.setAlignmentX(0.5f);
        JLabel subtitle = label("Service Center", plainFont(AppColors.sizeSmall()), AppColors.TEXT_SECONDARY);
        subtitle.setAlignmentX(0.5f);

        brand.add(logoMark);
        brand.add(Box.createVerticalStrut(10));
        brand.add(title);
        brand.add(Box.createVerticalStrut(2));
        brand.add(subtitle);
        return brand;
    }

    private JPanel buildSidebarFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 0, 0, 0));

        dbStatusLabel.setFont(plainFont(AppColors.sizeSmall()));
        updateDbStatus();
        footer.add(dbStatusLabel, BorderLayout.CENTER);
        return footer;
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(16, 20, 20, 20));
        main.add(buildTopBar(), BorderLayout.NORTH);
        main.add(contentHost, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildTopBar() {
        RoundedPanel bar = new RoundedPanel(AppColors.SURFACE_ELEVATED, CORNER_RADIUS);
        bar.setLayout(new BorderLayout(16, 0));
        bar.setBorder(new EmptyBorder(14, 20, 14, 20));

        pageTitleLabel.setFont(boldFont(AppColors.sizeHeading()));
        pageTitleLabel.setForeground(AppColors.TEXT_PRIMARY);
        bar.add(pageTitleLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        clockLabel.setFont(plainFont(AppColors.sizeBody()));
        clockLabel.setForeground(AppColors.TEXT_SECONDARY);
        updateClock();
        right.add(clockLabel);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildDashboardView() {
        JPanel view = new JPanel(new BorderLayout(0, 20));
        view.setOpaque(false);

        view.add(buildWelcomeSection(), BorderLayout.NORTH);
        view.add(buildStatsGrid(), BorderLayout.CENTER);
        view.add(buildQuickActions(), BorderLayout.SOUTH);
        return view;
    }

    private JPanel buildWelcomeSection() {
        RoundedPanel welcome = new RoundedPanel(AppColors.SURFACE, CORNER_RADIUS);
        welcome.setLayout(new BorderLayout());
        welcome.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel greeting = label("Welcome back, Administrator", boldFont(AppColors.sizeSubheading()),
                AppColors.TEXT_PRIMARY);
        JLabel desc = label(
                "Manage customers, track bike services, and monitor revenue — all in one place.",
                plainFont(AppColors.sizeBody()), AppColors.TEXT_SECONDARY);
        desc.setBorder(new EmptyBorder(6, 0, 0, 0));

        text.add(greeting);
        text.add(desc);
        welcome.add(text, BorderLayout.CENTER);

        JPanel accent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.ACCENT_RED);
                g2.fillRoundRect(0, 0, 6, getHeight(), 6, 6);
                g2.setColor(AppColors.ACCENT_ORANGE);
                g2.fillRoundRect(10, 0, 4, getHeight() / 2, 4, 4);
                g2.dispose();
            }
        };
        accent.setPreferredSize(new Dimension(20, 0));
        accent.setOpaque(false);
        welcome.add(accent, BorderLayout.WEST);

        return welcome;
    }

    private JPanel buildStatsGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 18));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(4, 0, 0, 0));

        grid.add(new StatCard("Total Customers", "0", "\u263A", AppColors.CARD_CUSTOMERS));
        grid.add(new StatCard("Bikes Serviced", "0", "\u2699", AppColors.CARD_BIKES));
        grid.add(new StatCard("Active Services", "0", "\u26A1", AppColors.CARD_SERVICES));
        grid.add(new StatCard("Revenue", "\u20B9 0", "\u20B9", AppColors.CARD_REVENUE));

        return grid;
    }

    private JPanel buildQuickActions() {
        RoundedPanel panel = new RoundedPanel(AppColors.SURFACE, CORNER_RADIUS);
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 16));
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        panel.add(sectionLabel("Quick Actions"));
        JButton newCust = styledButton("New Customer", AppColors.ACCENT_RED, AppColors.ACCENT_RED_BRIGHT);
        JButton newSvc = styledButton("New Service", AppColors.SURFACE_LIGHT, AppColors.ACCENT_ORANGE);
        JButton refresh = styledButton("Refresh Stats", AppColors.SURFACE_LIGHT, AppColors.SURFACE_ELEVATED);
        newCust.addActionListener(e -> openCustomersNew());
        newSvc.addActionListener(e -> openServicesNew());
        refresh.addActionListener(e -> refreshAllData());
        panel.add(newCust);
        panel.add(newSvc);
        panel.add(refresh);

        return panel;
    }

    public void refreshAllData() {
        refreshStatistics();
        customerPanel.loadTablePublic();
        servicePanel.reload();
        reportsPanel.loadReport();
        updateDbStatus();
    }

    private void openCustomersNew() {
        activateNav(customersNav, customerPanel, "Customers");
        customerPanel.focusNewCustomer();
    }

    private void openServicesNew() {
        activateNav(servicesNav, servicePanel, "Service Records");
        servicePanel.focusNewService();
    }

    private void activateNav(NavButton nav, JPanel panel, String title) {
        setActiveNav(nav);
        pageTitleLabel.setText(title);
        showView(panel);
    }

    // ── Navigation ──────────────────────────────────────────────────

    private NavButton createNav(String text, String icon, JPanel target, String pageTitle, boolean selected) {
        NavButton btn = new NavButton(text, icon, target);
        btn.addActionListener(e -> {
            setActiveNav(btn);
            pageTitleLabel.setText(pageTitle);
            showView(target);
            if (target == customerPanel) {
                customerPanel.loadTablePublic();
            } else if (target == servicePanel) {
                servicePanel.reload();
            } else if (target == reportsPanel) {
                reportsPanel.loadReport();
            }
        });
        if (selected) {
            setActiveNav(btn);
        }
        return btn;
    }

    private void setActiveNav(NavButton btn) {
        if (activeNav != null) {
            activeNav.setNavSelected(false);
        }
        activeNav = btn;
        btn.setNavSelected(true);
    }

    private void showView(JPanel view) {
        contentHost.removeAll();
        contentHost.add(view, BorderLayout.CENTER);
        contentHost.revalidate();
        contentHost.repaint();
        fadeIn(contentHost);
    }

    private void fadeIn(JPanel panel) {
        Timer timer = new Timer(16, null);
        final float[] alpha = {0.4f};
        timer.addActionListener(e -> {
            alpha[0] = Math.min(1f, alpha[0] + 0.12f);
            panel.setOpaque(false);
            if (alpha[0] >= 1f) {
                timer.stop();
            }
            panel.repaint();
        });
        timer.start();
    }

    // ── Data refresh ────────────────────────────────────────────────

    public void refreshStatistics() {
        JPanel statsGrid = findStatsGrid();
        if (statsGrid == null) {
            return;
        }
        int customers = DBConnection.countCustomers();
        int bikes = DBConnection.countBikesServiced();
        int active = DBConnection.countActiveServices();
        double revenue = DBConnection.totalRevenue();

        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        setStatValue(statsGrid, 0, String.valueOf(customers));
        setStatValue(statsGrid, 1, String.valueOf(bikes));
        setStatValue(statsGrid, 2, String.valueOf(active));
        setStatValue(statsGrid, 3, currency.format(revenue));
        updateDbStatus();
    }

    private JPanel findStatsGrid() {
        if (dashboardView.getComponentCount() < 2) {
            return null;
        }
        return (JPanel) dashboardView.getComponent(1);
    }

    private void setStatValue(JPanel grid, int index, String value) {
        if (index >= 0 && index < grid.getComponentCount()) {
            ((StatCard) grid.getComponent(index)).setValue(value);
        }
    }

    private void updateDbStatus() {
        boolean ok = DBConnection.isConnected();
        dbStatusLabel.setText(ok ? "\u25CF  Database connected" : "\u25CF  Database offline");
        dbStatusLabel.setForeground(ok ? AppColors.SUCCESS : AppColors.ACCENT_ORANGE);
    }

    private void startClockTimer() {
        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();
    }

    private void updateClock() {
        String formatted = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  |  HH:mm:ss"));
        clockLabel.setText(formatted);
    }

    // ── Styling helpers ─────────────────────────────────────────────

    static Font plainFont(int size) {
        Font f = new Font(AppColors.FONT_FAMILY, Font.PLAIN, size);
        if (!AppColors.FONT_FAMILY.equals(f.getFamily())) {
            f = new Font(AppColors.FONT_FALLBACK, Font.PLAIN, size);
        }
        return f;
    }

    static Font boldFont(int size) {
        Font f = new Font(AppColors.FONT_FAMILY, Font.BOLD, size);
        if (!AppColors.FONT_FAMILY.equals(f.getFamily())) {
            f = new Font(AppColors.FONT_FALLBACK, Font.BOLD, size);
        }
        return f;
    }

    static JLabel label(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    static JLabel sectionLabel(String text) {
        JLabel lbl = label(text, boldFont(AppColors.sizeBody()), AppColors.TEXT_SECONDARY);
        lbl.setBorder(new EmptyBorder(0, 8, 0, 12));
        return lbl;
    }

    static JButton styledButton(String text, Color bg, Color hover) {
        return new RoundedButton(text, bg, hover, BTN_RADIUS);
    }

    // ── Custom components ─────────────────────────────────────────────

    static class RoundedPanel extends JPanel {
        private final Color fill;
        private final int radius;

        RoundedPanel(Color fill, int radius) {
            this.fill = fill;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            if (radius > 0) {
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            } else {
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundedButton extends JButton {
        private Color normalBg;
        private Color hoverBg;
        private final int radius;
        private Color currentBg;
        private float hoverProgress;

        RoundedButton(String text, Color normalBg, Color hoverBg, int radius) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.radius = radius;
            this.currentBg = normalBg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(AppColors.TEXT_ON_ACCENT);
            setFont(boldFont(AppColors.sizeBody()));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 22, 10, 22));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    animateHover(1f);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    animateHover(0f);
                }
            });
        }

        private void animateHover(float target) {
            Timer anim = new Timer(12, null);
            anim.addActionListener(e -> {
                float step = (target - hoverProgress) * 0.35f;
                if (Math.abs(step) < 0.02f) {
                    hoverProgress = target;
                    anim.stop();
                } else {
                    hoverProgress += step;
                }
                currentBg = blend(normalBg, hoverBg, hoverProgress);
                repaint();
            });
            anim.start();
        }

        private static Color blend(Color a, Color b, float t) {
            int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
            int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
            int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
            return new Color(r, g, bl);
        }

        void setBackgroundColors(Color normal, Color hover) {
            this.normalBg = normal;
            this.hoverBg = hover;
            this.currentBg = normal;
            this.hoverProgress = 0f;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class StatCard extends RoundedPanel {
        private final JLabel valueLabel;

        StatCard(String title, String value, String icon, Color accent) {
            super(AppColors.SURFACE, CORNER_RADIUS);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(22, 24, 22, 24));

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);

            JLabel iconLbl = label(icon, boldFont(22), accent);
            JLabel titleLbl = label(title, plainFont(AppColors.sizeSmall()), AppColors.TEXT_SECONDARY);
            titleLbl.setHorizontalAlignment(SwingConstants.RIGHT);

            top.add(iconLbl, BorderLayout.WEST);
            top.add(titleLbl, BorderLayout.EAST);

            valueLabel = label(value, boldFont(AppColors.sizeStatValue()), AppColors.TEXT_PRIMARY);
            valueLabel.setBorder(new EmptyBorder(14, 0, 0, 0));

            JPanel accentBar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(accent);
                    g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(0, getHeight() - 4, getWidth(), getHeight() - 4);
                    g2.dispose();
                }
            };
            accentBar.setOpaque(false);
            accentBar.setPreferredSize(new Dimension(0, 8));

            add(top, BorderLayout.NORTH);
            add(valueLabel, BorderLayout.CENTER);
            add(accentBar, BorderLayout.SOUTH);
        }

        void setValue(String value) {
            valueLabel.setText(value);
        }
    }

    static class NavButton extends RoundedButton {
        private final JPanel target;
        private boolean selected;

        NavButton(String text, String icon, JPanel target) {
            super("  " + icon + "   " + text, AppColors.SIDEBAR, AppColors.SURFACE_LIGHT, BTN_RADIUS);
            this.target = target;
            setHorizontalAlignment(SwingConstants.LEFT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setForeground(AppColors.TEXT_SECONDARY);
            setOpaque(false);
        }

        void setNavSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setBackgroundColors(AppColors.ACCENT_RED, AppColors.ACCENT_RED_BRIGHT);
                setForeground(AppColors.TEXT_ON_ACCENT);
            } else {
                setBackgroundColors(AppColors.SIDEBAR, AppColors.SURFACE_LIGHT);
                setForeground(AppColors.TEXT_SECONDARY);
            }
            repaint();
        }

        JPanel getTarget() {
            return target;
        }
    }
}
