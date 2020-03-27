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

package feign.form.feign.spring;

import static feign.form.util.CharsetUtil.UTF_8;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

import java.util.HashMap;
import java.util.List;

import lombok.val;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Tomasz Juchniewicz <tjuchniewicz@gmail.com>
 * @since 22.08.2016
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = DEFINED_PORT,
    classes = Server.class,
    properties = {
      "server.port=8080",
      "feign.hystrix.enabled=false",
      "logging.level.feign.form.feign.spring.Client=DEBUG"
    }
)
public class SpringFormEncoderTest {

  @Autowired
  private Client client;

  @Test
  public void upload1Test () throws Exception {
    val folder = "test_folder";
    val file = new MockMultipartFile("file", "test".getBytes(UTF_8));
    val message = "message test";

    val response = client.upload1(folder, file, message);

    Assert.assertEquals(new String(file.getBytes()) + ':' + message + ':' + folder, response);
  }

  @Test
  public void upload2Test () throws Exception {
    val folder = "test_folder";
    val file = new MockMultipartFile("file", "test".getBytes(UTF_8));
    val message = "message test";

    String response = client.upload2(file, folder, message);

    Assert.assertEquals(new String(file.getBytes()) + ':' + message + ':' + folder, response);
  }

  @Test
  public void uploadFileNameAndContentTypeTest () throws Exception {
    val folder = "test_folder";
    val file = new MockMultipartFile(
        "file",
        "hello.dat",
        "application/octet-stream",
        "test".getBytes(UTF_8)
    );
    val message = "message test";

    val response = client.upload3(file, folder, message);

    Assert.assertEquals(file.getOriginalFilename() + ':' + file.getContentType() + ':' + folder, response);
  }

  @Test
  public void upload4Test () throws Exception {
    val map = new HashMap<Object, Object>();
    map.put("one", 1);
    map.put("two", 2);

    val userName = "popa";
    val id = "42";

    val response = client.upload4(id, map, userName);

    Assert.assertEquals(userName + ':' + id + ':' + map.size(), response);
  }

  @Test
  public void upload5Test () throws Exception {
    val file = new MockMultipartFile("popa.txt", "Hello world".getBytes(UTF_8));
    val dto = new Dto("field 1 value", 42, file);

    val response = client.upload5(dto);
    assertEquals(200, response.status());
  }

  @Test
  public void upload6ArrayTest () throws Exception {
    val file1 = new MockMultipartFile("popa1", "popa1", null, "Hello".getBytes(UTF_8));
    val file2 = new MockMultipartFile("popa2", "popa2", null, " world".getBytes(UTF_8));

    val response = client.upload6Array(new MultipartFile[] { file1, file2 });
    Assert.assertEquals("Hello world", response);
  }

  @Test
  public void upload6CollectionTest () throws Exception {
    List<MultipartFile> list = asList(
        (MultipartFile) new MockMultipartFile("popa1", "popa1", null, "Hello".getBytes(UTF_8)),
        (MultipartFile) new MockMultipartFile("popa2", "popa2", null, " world".getBytes(UTF_8))
    );

    val response = client.upload6Collection(list);
    Assert.assertEquals("Hello world", response);
  }

  @Test
  public void upload7Test () throws Exception {
    Pojo pojo = new Pojo("Hello", " world", 1);

    val response = client.upload7(pojo);
    Assert.assertEquals("Hello world1", response);
  }

  @Test
  public void upload8Test () throws Exception {
    Pojo pojo = new Pojo("Hello", " world", 1);

    List<MultipartFile> list = asList(
        (MultipartFile) new MockMultipartFile("files", "popa1", null, "Hello".getBytes(UTF_8)),
        (MultipartFile) new MockMultipartFile("files", "popa2", null, " world".getBytes(UTF_8))
    );

    val response = client.upload8(pojo, list);
    Assert.assertEquals("Hello world1Hello world", response);
  }
}
