package com.svarupa.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.Map;

/** Teacher dashboard: followers with their progress summaries. */
@Route(value = "teacher/dashboard", layout = MainLayout.class)
@PageTitle("Svarupa — Teacher dashboard")
public class TeacherDashboardView extends Div {

  @SuppressWarnings("unchecked")
  public TeacherDashboardView(SessionContext session, ApiClient api) {
    MainLayout.pageStyle(this);

    if (!session.isLoggedIn() || !session.isTeacher()) {
      var prompt = MainLayout.card();
      prompt.add(new H2("Teachers only"));
      var p = new Paragraph("This space belongs to teachers watching their students grow.");
      p.getStyle().set("color", MainLayout.MUTED);
      prompt.add(p);
      if (!session.isLoggedIn()) {
        var btn = new com.vaadin.flow.component.button.Button("Sign in",
            e -> UI.getCurrent().navigate(LoginView.class));
        MainLayout.stylePrimary(btn);
        prompt.add(btn);
      }
      add(prompt);
      return;
    }

    add(new H2("Your students"));

    try {
      var dash = api.get("/api/teachers/dashboard", Map.class);
      var count = new Paragraph("Following you: " + dash.get("followerCount"));
      count.getStyle().set("color", MainLayout.MUTED);
      add(count);

      var students = (List<Map<String, Object>>) dash.get("students");
      if (students == null || students.isEmpty()) {
        var p = new Paragraph("No students follow you yet — share your profile and they will find you.");
        p.getStyle().set("color", MainLayout.MUTED);
        add(p);
        return;
      }
      for (var o : students) {
        var card = MainLayout.card();
        card.getStyle().set("margin-bottom", "0.75rem");
        var name = new Span((String) o.get("displayName"));
        name.getStyle().set("font-weight", "600");
        var detail = new Paragraph(
            o.get("totalSessions") + " sessions · avg "
                + (o.get("avgMatchPct") == null ? "—" : o.get("avgMatchPct") + "%")
                + " · " + o.get("streakDays") + "-day streak");
        detail.getStyle().set("color", MainLayout.MUTED).set("margin", "0.25rem 0 0");
        card.add(name, detail);
        add(card);
      }
    } catch (Exception ex) {
      var p = new Paragraph("Could not load the dashboard right now.");
      p.getStyle().set("color", MainLayout.MUTED);
      add(p);
    }
  }
}
