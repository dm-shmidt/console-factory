package consolefactory;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.ClassUtils;

public class Utils {

  protected static boolean isPrimitiveType(TypeReference<?> typeReference) {
    try {
      return ClassUtils.isPrimitiveOrWrapper(getClassName(typeReference));
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  protected static boolean isEnum(TypeReference<?> typeReference) {
    try {
      return getClassName(typeReference).isEnum();
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private static Class<?> getClassName(TypeReference<?> typeReference)
      throws ClassNotFoundException {
    return Class.forName(typeReference.getType().getTypeName());
  }

}
