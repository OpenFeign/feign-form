package feign.form.multipart;

import java.util.Collection;

import feign.codec.EncodeException;

/**
 *
 * @author Darren Foong
 */
public class EmptyCollectionWriter implements Writer {

  @Override
  public boolean isApplicable(Object value) {
    return value instanceof Collection && ((Collection) value).isEmpty();
  }

  @Override
  public void write(Output output, String boundary, String key, Object value) throws EncodeException {}
}
