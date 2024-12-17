/*
 * Copyright (c) 2011-2016 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.it.discovery.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HelloServiceImpl implements HelloService {

  private final String msg;
  private MessageConsumer<JsonObject> service;
  private ServiceBinder binder;

  public HelloServiceImpl() {
    this("Hello");
  }

  public HelloServiceImpl(String message) {
    this.msg = message;
  }

  public void start(Vertx vertx, String address) {
    binder = new ServiceBinder(vertx).setAddress(address);
    binder.register(HelloService.class, this);
  }

  public void stop() {
    binder.unregister(service);
  }

  @Override
  public Future<String> hello(JsonObject name) {
    return Future.succeededFuture(msg + " " + name.getString("name"));
  }
}
