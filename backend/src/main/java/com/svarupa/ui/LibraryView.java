package com.svarupa.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.Map;

/** Browse the teacher reference library. */
@Route(value = "library", layout = MainLayout.class)
@PageTitle("Svarupa — Reference library")
public class LibraryView extends Div {

  @SuppressWarnings("unchecked")
  public LibraryView(ApiClient api) {
    MainLayout.pageStyle(this);
    add(new H2("Reference library"));

    var form = new ComboBox<String>("Dance form");
    form.setItems("Kuchipudi", "Bharatanatyam", "Kathak", "Odissi", "Yoga", "Fitness");
    form.setClearButtonVisible(true);
    var q = new TextField("Search");
    q.setPlaceholder("e.g. araimandi");
    q.setClearButtonVisible(true);
    var search = new Button("Search", e -> {});
    MainLayout.stylePrimary(search);

    var filters = new HorizontalLayout(form, q, search);
    filters.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);

    var grid = new Div();
    grid.getStyle()
        .set("display", "grid")
        .set("grid-template-columns", "repeat(auto-fill, minmax(260px, 1fr))")
        .set("gap", "1rem")
        .set("margin-top", "1.5rem");

    Runnable load = () -> {
      grid.removeAll();
      try {
        var page = api.get("/api/references?size=24"
            + (form.getValue() == null ? "" : "&form=" + form.getValue())
            + (q.getValue().isBlank() ? "" : "&q=" + q.getValue().trim()), Map.class);
        var content = (List<Map<String, Object>>) page.get("content");
        if (content == null || content.isEmpty()) {
          var empty = new Paragraph("No reference steps yet.");
          empty.getStyle().set("color", MainLayout.MUTED);
          grid.add(empty);
          return;
        }
        for (var r : content) {
          grid.add(card(r));
        }
      } catch (Exception ex) {
        var err = new Paragraph("Could not load the library.");
        err.getStyle().set("color", MainLayout.MUTED);
        grid.add(err);
      }
    };
    search.addClickListener(e -> load.run());
    load.run();

    add(filters, grid);
  }

  private Div card(Map<String, Object> r) {
    var card = MainLayout.card();
    var title = new Span((String) r.get("title"));
    title.getStyle().set("font-weight", "600").set("display", "block");
    var meta = new Span(r.get("danceForm") + " · " + r.get("difficulty"));
    meta.getStyle().set("color", MainLayout.GOLD).set("font-size", "0.8rem").set("display", "block");
    var teacher = new Span("by " + r.get("teacherName"));
    teacher.getStyle().set("color", MainLayout.MUTED).set("font-size", "0.85rem").set("display", "block");
    var desc = new Paragraph((String) r.getOrDefault("description", ""));
    desc.getStyle().set("color", MainLayout.MUTED).set("font-size", "0.85rem");
    var practice = new Button("Practice this",
        e -> UI.getCurrent().navigate(PracticeView.class));
    MainLayout.styleGhost(practice);
    practice.getStyle().set("margin-top", "0.75rem");
    card.add(title, meta, teacher, desc, practice);
    return card;
  }
}
