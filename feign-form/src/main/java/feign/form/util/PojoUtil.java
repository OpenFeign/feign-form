/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package feign.form.util;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;
import static lombok.AccessLevel.PRIVATE;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.rmi.UnexpectedException;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;

import feign.form.FormProperty;

/**
 * An utility class to work with POJOs.
 *
 * @author Artem Labazin
 */
public final class PojoUtil {

  public static boolean isUserPojo (@NonNull Object object) {
    val type = object.getClass();
    val packageName = type.getPackage().getName();
    return !packageName.startsWith("java.");
  }

  public static boolean isUserPojo (@NonNull Type type) {
    val typeName = type.toString();
    return !typeName.startsWith("class java.");
  }

  @SneakyThrows
  @SuppressFBWarnings("DP_DO_INSIDE_DO_PRIVILEGED")
  public static Map<String, Object> toMap (@NonNull Object object) {
    val result = new HashMap<String, Object>();
    val type = object.getClass();
    for (val field : type.getDeclaredFields()) {
      val modifiers = field.getModifiers();
      if (isFinal(modifiers) || isStatic(modifiers)) {
        continue;
      }
      field.setAccessible(true);

      val fieldValue = field.get(object);
      if (fieldValue == null) {
        continue;
      }

      val propertyKey = field.isAnnotationPresent(FormProperty.class)
                        ? field.getAnnotation(FormProperty.class).value()
                        : field.getName();

      result.put(propertyKey, fieldValue);
    }
    return result;
  }

  private PojoUtil () throws UnexpectedException {
    throw new UnexpectedException("It is not allowed to instantiate this class");
  }

  @Setter
  @NoArgsConstructor
  @FieldDefaults(level = PRIVATE)
  private static final class SetAccessibleAction implements PrivilegedAction<Object> {

    @Nullable
    Field field;

    @Override
    public Object run () {
      field.setAccessible(true);
      return null;
    }
  }
}
