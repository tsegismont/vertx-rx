/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.reactivex.test;

import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.file.AsyncFile;
import io.vertx.reactivex.core.http.HttpClientResponse;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.impl.WriteStreamSubscriber;
import io.vertx.test.core.Repeat;
import io.vertx.test.core.RepeatRule;
import io.vertx.test.core.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class WriteStreamSubscriberTest {

  @Rule
  public RepeatRule repeat = new RepeatRule();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Vertx vertx;
  private byte[] content;

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
    Path testFile = temporaryFolder.newFile().toPath();
    content = TestUtils.randomByteArray(1024 * 1024); // big enough not to fit into stream queues
    Files.write(testFile, content);
  }

  @After
  public void tearDown() {
    vertx.close();
  }

  @Test
  @Repeat(times = 100)
  public void httpRequestToFile() throws Exception {
    File destFile = temporaryFolder.newFile();
    // Create the AsyncFile and HttpServer from different contexts to make sure WriteStreamSubscriber is thread-safe
    AsyncFile asyncFile = vertx.fileSystem().openBlocking(destFile.getPath(), new OpenOptions());
    HttpServer httpServer = vertx.createHttpServer().requestHandler(req -> {
      req.toFlowable()
        .subscribe(new WriteStreamSubscriber<>(asyncFile, v -> req.response().end(), t -> {
          t.printStackTrace();
          req.response().setStatusCode(500);
        }));
    }).rxListen(8080).blockingGet();

    CompletableFuture<HttpClientResponse> response = new CompletableFuture<>();
    vertx.createHttpClient()
      .request(HttpMethod.GET, 8080, "127.0.0.1", "/", response::complete)
      .end(Buffer.newInstance(io.vertx.core.buffer.Buffer.buffer(content)));

    assertEquals(200, response.get().statusCode());
    assertArrayEquals("Content differs", content, Files.readAllBytes(destFile.toPath()));
  }
}
