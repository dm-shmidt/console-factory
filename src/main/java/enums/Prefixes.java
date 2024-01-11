package enums;

public enum Prefixes {

  SHORT("-"), LONG("--");
  private final String value;

  Prefixes(String s) {
    this.value = s;
  }

  @Override
  public String toString() {
    return value;
  }
}
