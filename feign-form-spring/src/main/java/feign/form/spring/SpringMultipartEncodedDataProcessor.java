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

import feign.codec.EncodeException;
import feign.form.MultipartEncodedDataProcessor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Adds support for {@link MultipartFile} type to {@link MultipartEncodedDataProcessor}.
 *
 * @author Tomasz Juchniewicz <tjuchniewicz@gmail.com>
 * @since 14.09.2016
 */
public class SpringMultipartEncodedDataProcessor extends MultipartEncodedDataProcessor {

	@Override
	protected boolean isPayload(Object value) {
		return super.isPayload(value) || isMultipart(value);
	}

	private boolean isMultipart(Object value) {
		if (value instanceof MultipartFile)
			return true;
		if (value.getClass().isArray())
			return isMultipartArray(value);
		// TODO add collections/map also?
		return false;
	}

	private boolean isMultipartArray(Object value) {
		return value instanceof MultipartFile[];
	}

	@Override
	protected void writeByteOrFile(OutputStream output, PrintWriter writer, String name, Object value) {
		if (isMultipart(value)) {
			MultipartFile[] multipartFiles;
			if (!isMultipartArray(value)) { // TODO add collections/map also?
				multipartFiles = new MultipartFile[] { (MultipartFile) value };
			} else {
				multipartFiles = (MultipartFile[]) value;
			}
			try {
				for (MultipartFile mpf : multipartFiles) {
					writeByteArray(output, writer, name, mpf.getOriginalFilename(), mpf.getContentType(), mpf.getBytes());
				}
			} catch (IOException e) {
				throw new EncodeException("Can't encode MultipartFile", e);
			}
			return;
		}

		super.writeByteOrFile(output, writer, name, value);
	}
}
