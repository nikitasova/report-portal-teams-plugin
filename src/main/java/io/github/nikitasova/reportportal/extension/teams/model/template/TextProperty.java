package io.github.nikitasova.reportportal.extension.teams.model.template;

import java.util.Objects;

public class TextProperty implements TemplateProperty {

  private final String name;

  public TextProperty(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TextProperty that = (TextProperty) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
