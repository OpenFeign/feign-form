/*
 * Copyright 2020 the original author or authors.
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

package feign.form.spring;

import static feign.form.ContentProcessor.CRLF;
import static feign.form.util.PojoUtil.isUserPojo;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import feign.codec.EncodeException;
import feign.form.multipart.AbstractWriter;
import feign.form.multipart.Output;

import lombok.val;

import java.io.IOException;

/**
 *
 * @author Darren Foong
 */
public abstract class PojoSerializationWriter extends AbstractWriter {
  @Override
  public boolean isApplicable(Object object) {
    return !(object instanceof MultipartFile) && !(object instanceof MultipartFile[]) && isUserPojo(object);
  }

  @Override
  public void write (Output output, String key, Object object) throws EncodeException {
    try {
      val string = new StringBuilder()
          .append("Content-Disposition: form-data; name=\"").append(key).append('"')
          .append(CRLF)
          .append("Content-Type: ").append(getContentType())
          .append("; charset=").append(output.getCharset().name())
          .append(CRLF)
          .append(CRLF)
          .append(serialize(object))
          .toString();

      output.write(string);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  protected abstract MediaType getContentType();

  protected abstract String serialize(Object object) throws IOException;
}
