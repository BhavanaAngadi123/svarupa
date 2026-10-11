package com.svarupa.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.Map;

/** Student progress: totals, streak, trend, per-reference improvement. */
@Route(value = "progress", layout = MainLayout.class)
@PageTitle("Svarupa — My progress")
public class ProgressView extends Div {

  @SuppressWarnings("unchecked")
  public ProgressView(SessionContext session, ApiClient api) {
    MainLayout.pageStyle(this);

    if (!session.isLoggedIn()) {
      var prompt = MainLayout.card();
      prompt.add(new H2("Sign in to see your progress"));
      var btn = new com.vaadin.flow.component.button.Button("Sign in",
          e -> UI.getCurrent().navigate(LoginView.class));
      MainLayout.stylePrimary(btn);
      prompt.add(btn);
      add(prompt);
      return;
    }

    add(new H2("Your journey"));

    try {
      var summary = api.get("/api/progress/summary", Map.class);
      var stats = new HorizontalLayout(
          stat(String.valueOf(summary.get("totalSessions")), "sessions"),
          stat(summary.get("avgMatchPct") == null ? "—" : summary.get("avgMatchPct") + "%", "avg match"),
          stat(formatDuration(summary.get("totalPracticeSeconds")), "practice time"),
          stat(String.valueOf(summary.get("streakDays")), "day streak"));
      stats.setWidthFull();
      stats.getStyle().set("gap", "1rem").set("margin", "1rem 0 2rem");
      add(stats);

      var byRef = api.get("/api/progress/by-reference", List.class);
      var h = new H2("Per-step progress");
      h.getStyle().set("font-size", "1.25rem");
      add(h);
      if (byRef.isEmpty()) {
        var p = new Paragraph("Practice a step to start your trend line.");
        p.getStyle().set("color", MainLayout.MUTED);
        add(p);
      }
      for (var o : byRef) {
        var r = (Map<String, Object>) o;
        var card = MainLayout.card();
        card.getStyle().set("margin-bottom", "0.75rem");
        var title = new Span((String) r.get("title"));
        title.getStyle().set("font-weight", "600");
        var detail = new Paragraph(
            r.get("sessions") + " sessions · avg " + r.get("avgMatchPct") + "% · "
                + "first " + r.get("firstMatchPct") + "% → best " + r.get("bestMatchPct") + "%");
        detail.getStyle().set("color", MainLayout.MUTED).set("margin", "0.25rem 0 0");
        card.add(title, detail);
        add(card);
      }
    } catch (Exception ex) {
      var p = new Paragraph("Could not load progress right now.");
      p.getStyle().set("color", MainLayout.MUTED);
      add(p);
    }
  }

  private Div stat(String value, String label) {
    var card = MainLayout.card();
    card.setWidthFull();
    card.getStyle().set("text-align", "center");
    var v = new Span(value);
    v.getStyle().set("font-size", "1.8rem").set("font-weight", "700").set("color", MainLayout.GOLD);
    var l = new Paragraph(label);
    l.getStyle().set("color", MainLayout.MUTED).set("margin", "0.25rem 0 0").set("font-size", "0.85rem");
    card.add(v, l);
    return card;
  }

  private String formatDuration(Object secondsObj) {
    long s = secondsObj instanceof Number n ? n.longValue() : 0;
    long h = s / 3600, m = (s % 3600) / 60;
    return h > 0 ? h + "h " + m + "m" : m + "m";
  }
}
