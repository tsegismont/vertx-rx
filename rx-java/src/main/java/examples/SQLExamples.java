/*
 * Copyright (c) 2011-2018 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package examples;

import io.vertx.ext.sql.ResultSet;
import io.vertx.rxjava.ext.sql.InTransactionCompletable;
import io.vertx.rxjava.ext.sql.SQLClient;
import io.vertx.rxjava.ext.sql.SQLClientHelper;
import io.vertx.rxjava.ext.sql.SQLConnection;

/**
 * @author Thomas Segismont
 */
public class SQLExamples {

  public void inTransactionTransformer(SQLConnection conn) {
    // tag::inTransactionTransformer[]
    conn.rxExecute("... insert into album ...").toCompletable()
      .andThen(conn.rxExecute("... insert into tracks ...").toCompletable())
      .compose(new InTransactionCompletable(conn)) // <1>
      .andThen(conn.rxQuery("... select from album, tracks ...").map(ResultSet::getResults))
      .subscribe(rows -> {
        // send to client
      }, throwable -> {
        // handle error
      });
    // end::inTransactionTransformer[]
  }

  public void inTransactionSingle(SQLClient sqlClient) {
    // tag::inTransactionSingle[]
    SQLClientHelper.inTransactionSingle(sqlClient, conn -> {
      return conn.rxExecute("... insert into album ...").toCompletable()
        .andThen(conn.rxExecute("... insert into tracks ...").toCompletable())
        .andThen(conn.rxQuery("... select from album, tracks ...").map(ResultSet::getResults)); // <1>
    }).subscribe(rows -> {
      // send to client
    }, throwable -> {
      // handle error
    });
    // end::inTransactionSingle[]
  }

  private SQLExamples() {
    // Examples
  }
}
