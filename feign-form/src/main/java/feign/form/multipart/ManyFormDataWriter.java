package feign.form.multipart;

import feign.codec.EncodeException;
import feign.form.FormData;
import lombok.val;

public class ManyFormDataWriter implements Writer {
    private final FormDataWriter formDataWriter = new FormDataWriter();
    @Override
    public void write(Output output, String boundary, String key, Object value) throws EncodeException {
        if (value instanceof FormData[]) {
            for (FormData formData : (FormData[]) value) {
                formDataWriter.write(output, boundary, key, formData);
            }
        } else if (value instanceof Iterable) {
            val fields = (Iterable)value;
            for (Object formData : fields) {
                formDataWriter.write(output, boundary, key, formData);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean isApplicable(Object value) {
        if (value instanceof FormData[]) {
            return true;
        }
        if (value instanceof Iterable) {
            val iterable = (Iterable) value;
            val iterator = iterable.iterator();
            return iterator.hasNext() && iterator.next() instanceof FormData;
        }
        return false;
    }
}
