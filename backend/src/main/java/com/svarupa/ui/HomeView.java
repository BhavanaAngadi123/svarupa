package com.svarupa.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Landing: the promise of Svarupa. */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Svarupa — Meet your true form")
public class HomeView extends Div {

  public HomeView(SessionContext session) {
    MainLayout.pageStyle(this);

    var hero = new Div();
    hero.getStyle()
        .set("text-align", "center")
        .set("padding", "4rem 1rem 3rem");

    var kicker = new Paragraph("AI practice companion for dance");
    kicker.getStyle()
        .set("color", MainLayout.GOLD)
        .set("letter-spacing", "0.25em")
        .set("text-transform", "uppercase")
        .set("font-size", "0.75rem");

    var title = new H1("Meet your true form");
    title.getStyle()
        .set("font-size", "3rem")
        .set("margin", "0.5rem 0")
        .set("font-weight", "700")
        .set("background", "linear-gradient(135deg, #f2ecdf, #e8b64c)")
        .set("-webkit-background-clip", "text")
        .set("background-clip", "text")
        .set("-webkit-text-fill-color", "transparent");

    var sub = new Paragraph(
        "Your teacher sets the destination — the perfect form. "
            + "Svarupa watches you practice and reroutes you back to it, "
            + "joint by joint, rep by rep.");
    sub.getStyle().set("color", MainLayout.MUTED).set("max-width", "34rem").set("margin", "0 auto");

    var cta = new Button(
        session.isLoggedIn() ? "Start practicing" : "Begin your journey",
        e -> {
          if (session.isLoggedIn()) {
            UI.getCurrent().navigate(PracticeView.class);
          } else {
            UI.getCurrent().navigate(LoginView.class);
          }
        });
    MainLayout.stylePrimary(cta);
    cta.getStyle().set("margin-top", "1.5rem").set("font-size", "1.05rem")
        .set("padding", "0.75rem 2rem");

    hero.add(kicker, title, sub, cta);

    var steps = new HorizontalLayout(
        step("01", "Learn the reference", "Teachers record the perfect step — eight joint angles, captured precisely."),
        step("02", "Practice with light", "Your camera becomes a constellation of joints, scored live against the reference."),
        step("03", "Feel the reroute", "Gentle corrections guide each joint home. Watch your match % climb week after week."));
    steps.setWidthFull();
    steps.getStyle().set("gap", "1rem").set("margin-top", "2rem");
    steps.setFlexGrow(1, steps.getComponentAt(0), steps.getComponentAt(1), steps.getComponentAt(2));

    add(hero, steps);
  }

  private Div step(String num, String title, String body) {
    var card = MainLayout.card();
    card.setWidthFull();
    var n = new Paragraph(num);
    n.getStyle().set("color", MainLayout.GOLD).set("font-size", "0.8rem")
        .set("letter-spacing", "0.2em").set("margin", "0 0 0.5rem");
    var t = new Paragraph(title);
    t.getStyle().set("font-weight", "600").set("margin", "0 0 0.5rem");
    var b = new Paragraph(body);
    b.getStyle().set("color", MainLayout.MUTED).set("font-size", "0.9rem").set("margin", "0");
    card.add(n, t, b);
    return card;
  }
}
