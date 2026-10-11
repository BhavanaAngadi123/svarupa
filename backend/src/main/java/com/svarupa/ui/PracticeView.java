package com.svarupa.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Live practice: embeds the client-side pose tracker (MediaPipe runs in the
 * browser; the backend scores and stores the session). Requires sign-in —
 * the tracker reads the JWT from localStorage (same origin).
 */
@Route(value = "practice", layout = MainLayout.class)
@PageTitle("Svarupa — Practice")
public class PracticeView extends Div {

  public PracticeView(SessionContext session) {
    MainLayout.pageStyle(this);

    if (!session.isLoggedIn()) {
      var prompt = MainLayout.card();
      prompt.add(new H2("Sign in to practice"));
      var p = new Paragraph("Your practice sessions, scores, and progress live in your account.");
      p.getStyle().set("color", MainLayout.MUTED);
      var btn = new com.vaadin.flow.component.button.Button("Sign in",
          e -> UI.getCurrent().navigate(LoginView.class));
      MainLayout.stylePrimary(btn);
      prompt.add(p, btn);
      add(prompt);
      return;
    }

    var head = new H2("Live practice");
    head.getStyle().set("margin", "0 0 0.25rem");
    var sub = new Paragraph(
        "Choose a reference step in the tracker, allow camera access, and dance. "
            + "Your skeleton is drawn live; the session is scored when you finish.");
    sub.getStyle().set("color", MainLayout.MUTED).set("margin-top", "0");

    var frame = new IFrame("/svarupa-tracker/tracker.html");
    frame.setWidthFull();
    frame.setHeight("78vh");
    frame.getStyle()
        .set("border", "1px solid #2a2450")
        .set("border-radius", "16px")
        .set("background", "#000");
    frame.getElement().setAttribute("allow", "camera; microphone");

    add(head, sub, frame);
  }
}
