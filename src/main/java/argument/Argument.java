package argument;

import java.util.Set;

public interface Argument {

  Argument copy();

  String getName();

  String getHelpInfo();

  boolean isMandatory();

  Object getValue();

  Object getDefaultValue();

  void setValue(Object value);

  Class<?> getType();

  Set<String> getAliases();

  boolean isUnique();

  boolean hasDefaultValue();
}
