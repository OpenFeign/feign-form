/*
 * Copyright 2018 Artem Labazin
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

package feign.form.feign.spring.issue34;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author alabazin
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = NONE,
    classes = WireMockTest.WireMockConfiguration.class
)
public class WireMockTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8796);

  @Autowired
  StorageClient storageClient;

  @Test
  public void savesMultipartFile() {
    String imagePngContentType = "image/png";
    String fileContent = "nonsensecontent";
    String location = "location";

    stubFor(any(urlPathMatching("/storage-service/\\\\?.*"))
        .withQueryParam("type", equalTo("IMG"))
        .withMultipartRequestBody(aMultipart()
            .withName("file")
            .withHeader("Content-Type", containing(imagePngContentType))
            .withBody(binaryEqualTo(fileContent.getBytes()))
        )
        .willReturn(aResponse()
            .withStatus(OK_200)
            .withHeader("Location", location)
        )
    );

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "image.png",
        imagePngContentType,
        fileContent.getBytes()
    );

    ResponseEntity<Void> response = storageClient.save("IMG", file);

    assertThat(response.getStatusCode().value()).isEqualTo(OK_200);
    assertThat(response.getHeaders().get("Location")).containsOnly(location);
  }


  @EnableFeignClients
  @SpringBootApplication
  public static class WireMockConfiguration {

  }
}
