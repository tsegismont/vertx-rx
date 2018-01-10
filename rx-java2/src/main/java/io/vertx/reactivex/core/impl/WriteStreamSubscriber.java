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

package io.vertx.reactivex.core.impl;

import io.vertx.core.Handler;
import io.vertx.reactivex.core.streams.WriteStream;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Thomas Segismont
 */
public final class WriteStreamSubscriber<R> implements Subscriber<R> {

  private final ReadStreamSubscriber<R, R> readStreamSubscriber;
  private final Handler<R> dataHandler;

  public WriteStreamSubscriber(WriteStream<R> writeStream, Handler<Void> onComplete, Handler<Throwable> onError) {
    Objects.requireNonNull(writeStream, "writeStream");
    Objects.requireNonNull(onError, "onError");
    Objects.requireNonNull(onComplete, "onComplete");
    readStreamSubscriber = new ReadStreamSubscriber<>(Function.identity());
    dataHandler = data -> {
      writeStream.write(data);
      if (writeStream.writeQueueFull()) {
        readStreamSubscriber.pause();
        writeStream.drainHandler(v -> readStreamSubscriber.resume());
      }
    };
    readStreamSubscriber.endHandler(onComplete).exceptionHandler(onError);
  }

  @Override
  public void onSubscribe(Subscription s) {
    readStreamSubscriber.onSubscribe(s);
    readStreamSubscriber.handler(dataHandler);
  }

  @Override
  public void onComplete() {
    readStreamSubscriber.onComplete();
  }

  @Override
  public void onError(Throwable e) {
    readStreamSubscriber.onError(e);
  }

  @Override
  public void onNext(R item) {
    readStreamSubscriber.onNext(item);
  }
}
