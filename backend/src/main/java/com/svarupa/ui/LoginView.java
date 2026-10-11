package com.svarupa.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.Map;
import org.springframework.web.client.HttpClientErrorException;

/** Sign in / register. Mirrors the JWT into localStorage for the tracker. */
@Route(value = "login", layout = MainLayout.class)
@PageTitle("Svarupa — Sign in")
public class LoginView extends Div {

  public LoginView(SessionContext session, ApiClient api) {
    MainLayout.pageStyle(this);

    var card = MainLayout.card();
    card.getStyle().set("max-width", "26rem").set("margin", "3rem auto");

    var title = new H2("Welcome back");
    title.getStyle().set("margin-top", "0");

    var email = new TextField("Email");
    email.setWidthFull();
    var password = new PasswordField("Password");
    password.setWidthFull();

    var signIn = new Button("Sign in", e -> {
      try {
        @SuppressWarnings("unchecked")
        var res = api.post("/api/auth/login",
            Map.of("email", email.getValue(), "password", password.getValue()), Map.class);
        completeLogin(session, res);
      } catch (HttpClientErrorException ex) {
        Notification.show("Sign in failed — check your email and password.");
      }
    });
    MainLayout.stylePrimary(signIn);
    signIn.setWidthFull();

    var name = new TextField("Display name");
    name.setWidthFull();
    var role = new com.vaadin.flow.component.select.Select<String>();
    role.setLabel("I am a");
    role.setItems("Student", "Teacher");
    role.setValue("Student");
    role.setWidthFull();

    var register = new Button("Create account", e -> {
      try {
        @SuppressWarnings("unchecked")
        var res = api.post("/api/auth/register",
            Map.of("email", email.getValue(), "password", password.getValue(),
                "displayName", name.getValue().isBlank() ? email.getValue() : name.getValue(),
                "role", "Teacher".equals(role.getValue()) ? "TEACHER" : "STUDENT"),
            Map.class);
        completeLogin(session, res);
      } catch (HttpClientErrorException ex) {
        Notification.show("Registration failed — " + ex.getStatusCode());
      }
    });
    MainLayout.styleGhost(register);
    register.setWidthFull();

    var divider = new Paragraph("— or —");
    divider.getStyle().set("text-align", "center").set("color", MainLayout.MUTED);

    var form = new VerticalLayout(title, email, password, signIn, divider, name, role, register);
    form.setSpacing(true);
    form.setPadding(false);
    card.add(form);
    add(card);
  }

  @SuppressWarnings("unchecked")
  private void completeLogin(SessionContext session, Map<String, Object> res) {
    session.login(
        (String) res.get("accessToken"),
        (String) res.get("refreshToken"),
        java.util.UUID.fromString((String) res.get("userId")),
        (String) res.get("email"),
        (String) res.get("displayName"),
        com.svarupa.common.Role.valueOf((String) res.get("role")));
    // Mirror the JWT for the embedded practice tracker (same origin).
    UI.getCurrent().getPage().executeJs(
        "localStorage.setItem('svarupa_jwt', $0); localStorage.setItem('svarupa_user', $1);",
        res.get("accessToken"), res.get("displayName"));
    UI.getCurrent().navigate(PracticeView.class);
    UI.getCurrent().getPage().reload();
  }
}
