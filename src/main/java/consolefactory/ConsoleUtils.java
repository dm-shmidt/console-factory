package consolefactory;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.ClassUtils;

public class ConsoleUtils {

  protected static boolean isPrimitiveType(TypeReference<?> typeReference) {
    try {
      return ClassUtils.isPrimitiveOrWrapper(getClass(typeReference));
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  protected static boolean isEnum(TypeReference<?> typeReference) {
    try {
      return getClass(typeReference).isEnum();
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  protected static Class<?> getClass(TypeReference<?> typeReference)
      throws ClassNotFoundException {
    return Class.forName(typeReference.getType().getTypeName());
  }

}
