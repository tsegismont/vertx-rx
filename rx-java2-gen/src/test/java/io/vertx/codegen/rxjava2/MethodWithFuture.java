package io.vertx.codegen.rxjava2;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface MethodWithFuture {

  static <T> boolean isSucceeded(Future<T> future) {
    return future.succeeded();
  }

  static <T> boolean isFailed(Future<T> future) {
    return future.failed();
  }

  static <T> boolean isComplete(Future<T> future) {
    return future.isComplete();
  }

  static <T> T getResult(Future<T> future) {
    return future.result();
  }

  static <T> Throwable getCause(Future<T> future) {
    return future.cause();
  }

  static Future<MethodWithFuture> withVertxGen(Future<MethodWithFuture> future) {
    return future;
  }
}
