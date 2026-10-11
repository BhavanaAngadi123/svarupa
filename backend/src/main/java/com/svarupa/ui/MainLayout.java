package com.svarupa.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;

/**
 * Dark luminous shell: deep indigo-black, gold accents, constellation motif.
 * Critical styling is inline so the aesthetic holds even without a
 * frontend build; the full theme lives in frontend/themes/svarupa.
 */
public class MainLayout extends AppLayout {

  public static final String BG = "#0b0b18";
  public static final String PANEL = "#141428";
  public static final String GOLD = "#e8b64c";
  public static final String TEXT = "#f2ecdf";
  public static final String MUTED = "#9a94b8";

  private final SessionContext session;

  public MainLayout(SessionContext session) {
    this.session = session;
    getStyle()
        .set("background", "radial-gradient(1200px 800px at 80% -10%, #1c1440 0%, " + BG + " 60%)")
        .set("color", TEXT)
        .set("min-height", "100vh");

    var brand = new H1("✦ Svarupa");
    brand.getStyle()
        .set("margin", "0")
        .set("font-size", "1.4rem")
        .set("color", GOLD)
        .set("letter-spacing", "0.08em")
        .set("font-weight", "600");
    var tagline = new Span("meet your true form");
    tagline.getStyle().set("color", MUTED).set("font-size", "0.75rem").set("margin-left", "0.5rem");

    var nav = new HorizontalLayout(
        link("Practice", PracticeView.class),
        link("Library", LibraryView.class),
        link("Progress", ProgressView.class));
    nav.setSpacing(true);

    var right = new HorizontalLayout();
    right.setAlignItems(FlexComponent.Alignment.CENTER);
    if (session.isLoggedIn()) {
      if (session.isTeacher()) {
        right.add(link("Teach", TeacherDashboardView.class));
      }
      var me = new Span(session.getDisplayName());
      me.getStyle().set("color", MUTED).set("font-size", "0.85rem");
      var logout = new Button("Sign out", e -> {
        session.logout();
        UI.getCurrent().getPage().executeJs("localStorage.removeItem('svarupa_jwt');");
        UI.getCurrent().navigate(HomeView.class);
      });
      styleGhost(logout);
      right.add(me, logout);
    } else {
      var login = new Button("Sign in", e -> UI.getCurrent().navigate(LoginView.class));
      stylePrimary(login);
      right.add(login);
    }

    var bar = new HorizontalLayout(brand, tagline, nav, right);
    bar.setWidthFull();
    bar.setAlignItems(FlexComponent.Alignment.CENTER);
    bar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    bar.getStyle()
        .set("padding", "0.75rem 1.5rem")
        .set("border-bottom", "1px solid #2a2450")
        .set("background", "rgba(11,11,24,0.85)")
        .set("backdrop-filter", "blur(8px)");
    nav.getStyle().set("margin-left", "2rem");

    addToNavbar(bar);
  }

  private RouterLink link(String text, Class<? extends com.vaadin.flow.component.Component> target) {
    var link = new RouterLink(text, target);
    link.getStyle()
        .set("color", TEXT)
        .set("text-decoration", "none")
        .set("font-size", "0.9rem")
        .set("padding", "0.35rem 0.75rem")
        .set("border-radius", "999px");
    return link;
  }

  public static void stylePrimary(Button b) {
    b.getStyle()
        .set("background", "linear-gradient(135deg, #e8b64c, #c98a2b)")
        .set("color", "#1a1206")
        .set("border", "none")
        .set("border-radius", "999px")
        .set("padding", "0.5rem 1.25rem")
        .set("font-weight", "600")
        .set("cursor", "pointer");
  }

  public static void styleGhost(Button b) {
    b.getStyle()
        .set("background", "transparent")
        .set("color", TEXT)
        .set("border", "1px solid #3a3468")
        .set("border-radius", "999px")
        .set("padding", "0.45rem 1.1rem")
        .set("cursor", "pointer");
  }

  public static Div card() {
    var card = new Div();
    card.getStyle()
        .set("background", PANEL)
        .set("border", "1px solid #2a2450")
        .set("border-radius", "16px")
        .set("padding", "1.25rem")
        .set("box-shadow", "0 8px 32px rgba(0,0,0,0.45)");
    return card;
  }

  public static void pageStyle(Div page) {
    page.getStyle()
        .set("max-width", "1100px")
        .set("margin", "0 auto")
        .set("padding", "2rem 1.5rem 4rem")
        .set("color", TEXT);
  }
}
