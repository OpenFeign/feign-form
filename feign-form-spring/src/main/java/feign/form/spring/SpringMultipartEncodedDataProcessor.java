/*
 * Copyright 2017 Artem Labazin <xxlabaza@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package feign.form.spring;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import feign.codec.EncodeException;
import feign.form.MultipartEncodedDataProcessor;

/**
 * Adds support for {@link MultipartFile} type to {@link MultipartEncodedDataProcessor}.
 *
 * @author Tomasz Juchniewicz <tjuchniewicz@gmail.com>
 * @since 14.09.2016
 */
public class SpringMultipartEncodedDataProcessor extends MultipartEncodedDataProcessor {

  private static final List<HttpMessageConverter<?>>
      converters =
      new RestTemplate().getMessageConverters();
  private static final HttpHeaders jsonHeaders = new HttpHeaders();

  static {
    jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
  }

  @Override
  protected boolean isPayload(Object value) {
    return super.isPayload(value) || isMultipart(value);
  }

  private boolean isMultipart(Object value) {
    if (value instanceof MultipartFile) {
      return true;
    }
    if (value.getClass().isArray()) {
      return isMultipartArray(value);
    }
    // TODO add collections/map also?
    return false;
  }

  private boolean isMultipartArray(Object value) {
    return value instanceof MultipartFile[];
  }

  @Override
  protected void writeByteOrFile(OutputStream output, PrintWriter writer, String name,
                                 Object value) {
    if (isMultipart(value)) {
      MultipartFile[] multipartFiles;
      if (!isMultipartArray(value)) { // TODO add collections/map also?
        multipartFiles = new MultipartFile[]{(MultipartFile) value};
      } else {
        multipartFiles = (MultipartFile[]) value;
      }
      try {
        for (MultipartFile mpf : multipartFiles) {
          writeByteArray(output, writer, name, mpf.getOriginalFilename(), mpf.getContentType(),
              mpf.getBytes());
        }
      } catch (IOException e) {
        throw new EncodeException("Can't encode MultipartFile", e);
      }
      return;
    }

    super.writeByteOrFile(output, writer, name, value);
  }

  @Override
  protected void writeParameter(PrintWriter writer, String name, Object value) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    HttpOutputMessage dummyRequest = new HttpOutputMessageImpl(outputStream, jsonHeaders);
    Class<?> requestType = value.getClass();
    MediaType requestContentType = MediaType.APPLICATION_JSON_UTF8;
    for (HttpMessageConverter<?> messageConverter : converters) {
      if (messageConverter.canWrite(requestType, requestContentType)) {
        try {
          ((HttpMessageConverter<Object>) messageConverter)
              .write(value, requestContentType, dummyRequest);
          writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(CRLF);
          writer.append("Content-Type: application/json; charset=UTF-8").append(CRLF);
          String test = ((ByteArrayOutputStream) dummyRequest.getBody()).toString("UTF-8");
          writer.append(CRLF).append(test);
          return;
        } catch (IOException e) {

        }
        return;
      }
    }
    super.writeParameter(writer, name, value);
  }

  /**
   * Minimal implementation of {@link org.springframework.http.HttpOutputMessage}. It's needed to
   * provide the request body output stream to
   * {@link org.springframework.http.converter.HttpMessageConverter}s
   */
  private class HttpOutputMessageImpl implements HttpOutputMessage {

    private final OutputStream body;
    private final HttpHeaders headers;

    public HttpOutputMessageImpl(OutputStream body, HttpHeaders headers) {
      this.body = body;
      this.headers = headers;
    }

    @Override
    public OutputStream getBody() throws IOException {
      return body;
    }

    @Override
    public HttpHeaders getHeaders() {
      return headers;
    }

  }
}
