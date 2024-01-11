package argument;

public interface Argument {

  String getName();

  String getHelpInfo();

  boolean isMandatory();

  Object getValue();

  Object getDefaultValue();

}
