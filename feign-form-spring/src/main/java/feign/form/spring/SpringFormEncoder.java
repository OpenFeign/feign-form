package feign.form.spring;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

/**
 * Adds support for {@link MultipartFile} type to {@link FormEncoder}.
 *
 * @author Tomasz Juchniewicz <tjuchniewicz@gmail.com>
 * @since 14.09.2016
 */
public class SpringFormEncoder extends FormEncoder {

	private final Encoder delegate;

	public SpringFormEncoder() {
		this(new Encoder.Default());
	}

	public SpringFormEncoder(Encoder delegate) {
		this.delegate = delegate;
	}

	@Override
	public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {

		if (isFormRequest(bodyType) || MultipartFile.class.equals(bodyType)) {
			if (Map.class.isAssignableFrom(object.getClass())) {
				new SpringMultipartEncodedDataProcessor().process((Map<String, Object>) object, template);
			} else {
				MultipartFile file = (MultipartFile) object;
				Map<String, Object> data = Collections.singletonMap(file.getName(), object);
				new SpringMultipartEncodedDataProcessor().process(data, template);
			}
		} else {
			delegate.encode(object, bodyType, template);
		}

	}

	static boolean isFormRequest(Type type) {
		return MAP_STRING_WILDCARD.equals(type);
	}

}
