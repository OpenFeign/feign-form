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

package feign.form.multipart;

import static lombok.AccessLevel.PRIVATE;

import feign.codec.EncodeException;

import feign.form.FormData;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Sébastien Etronnier
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ManyFormDatasWriter extends AbstractWriter {

  SingleFormDataWriter formDataWriter = new SingleFormDataWriter();

  @Override
  public boolean isApplicable (Object value) {
    if (value instanceof FormData[]) {
      return true;
    }
    if (!(value instanceof Iterable)) {
      return false;
    }
    val iterable = (Iterable<?>) value;
    val iterator = iterable.iterator();
    return iterator.hasNext() && iterator.next() instanceof FormData;
  }

  @Override
  public void write (Output output, String boundary, String key, Object value) throws EncodeException {
    if (value instanceof FormData[]) {
      val formDatas = (FormData[]) value;
      for (val formData : formDatas) {
        formDataWriter.write(output, boundary, key, formData);
      }
    } else if (value instanceof Iterable) {
      val iterable = (Iterable<?>) value;
      for (val formData : iterable) {
        formDataWriter.write(output, boundary, key, formData);
      }
    } else {
      throw new IllegalArgumentException();
    }
  }
}
