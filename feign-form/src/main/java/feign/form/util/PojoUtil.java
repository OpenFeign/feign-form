/*
 * Copyright 2019 the original author or authors.
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

import feign.codec.EncodeException;
import feign.form.FormProperty;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.rmi.UnexpectedException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import lombok.var;

/**
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
  @Deprecated
  public static Map<String, Object> toMap (@NonNull Object object) {
    val result = new HashMap<String, Object>();
    val type = object.getClass();
    val setAccessibleAction = new SetAccessibleAction();
    for (val field : type.getDeclaredFields()) {
      val modifiers = field.getModifiers();
      if (isFinal(modifiers) || isStatic(modifiers)) {
        continue;
      }
      setAccessibleAction.setField(field);
      AccessController.doPrivileged(setAccessibleAction);

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

  public static Map<String, Object> toMap(
      final @NonNull Object object,
      final boolean processTransient,
      final boolean processFinal) {
    final var result = new HashMap<String, Object>();
    var clazz = object.getClass();
    val setAccessibleAction = new SetAccessibleAction();
    while (clazz != null) {
      final var fieldResult = Arrays.stream(clazz.getDeclaredFields())
          .filter(field ->
              (processFinal || !Modifier.isFinal(field.getModifiers())) &&
                  (processTransient || !Modifier.isTransient(field.getModifiers())) &&
                  !Modifier.isStatic(field.getModifiers()))
          .map(field -> toMapDoOnEach(setAccessibleAction, field, object))
          .filter(entry -> entry.getValue() != null)
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              Map.Entry::getValue,
              (oldObj, newObj) -> newObj,
              HashMap::new));
      result.putAll(fieldResult);
      clazz = clazz.getSuperclass();
    }
    return result;
  }

  private static void setFieldAccessible(
      final SetAccessibleAction setAccessibleAction,
      final Field field) {
    setAccessibleAction.setField(field);
    AccessController.doPrivileged(setAccessibleAction);
  }

  private static Map.Entry<String, Object> toMapDoOnEach(
      final SetAccessibleAction setAccessibleAction,
      final Field field,
      final Object object) throws EncodeException {

    setFieldAccessible(setAccessibleAction, field);
    try {
      var fieldValue = field.get(object);
      if (fieldValue != null && fieldValue.getClass().isEnum()) {
        fieldValue = ((Enum<?>) fieldValue).name();
      }
      final var propertyKey = field.isAnnotationPresent(FormProperty.class)
          ? field.getAnnotation(FormProperty.class).value()
          : field.getName();
      return new AbstractMap.SimpleEntry<>(propertyKey, fieldValue);
    } catch (Exception err) {
      throw new EncodeException(err.getMessage(), err);
    }
  }

  private PojoUtil() throws UnexpectedException {
    throw new UnexpectedException("It is not allowed to instantiate this class");
  }

  @Setter
  @NoArgsConstructor
  @FieldDefaults(level = PRIVATE)
  private static class SetAccessibleAction implements PrivilegedAction<Object> {

    @Nullable
    Field field;

    @Override
    public Object run () {
      field.setAccessible(true);
      return null;
    }
  }
}
