package feign.form.feign.spring;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = PRIVATE)
@ToString
public class SubDto extends Dto {

  MultipartFile subFile;

  SubEnumeration someEnum;

  Boolean subBool;

  public enum SubEnumeration {
    ONE, TWO, THREE
  }
}
