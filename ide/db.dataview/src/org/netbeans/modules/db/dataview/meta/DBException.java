/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.db.dataview.meta;

import java.sql.SQLException;

/**
 * @author Ahimanikya Satapathy
 */
public final class DBException extends Exception {

    public DBException(String message) {
        super(message);
    }

    public DBException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        StringBuffer buf = new StringBuffer();

        Throwable t = this;
        //we are getting only the first exception which is wrapped,
        //should we get messages from all the exceptions in the chain?
        if (t.getCause() != null) {
            t = t.getCause();
        }

        if (t != this) {
            if (t instanceof SQLException) {
                SQLException e = (SQLException) t;
                buf.append("Error code ").append(e.getErrorCode());
                buf.append(", SQL state ").append(e.getSQLState());
                buf.append("\n");
            }
            buf.append(super.getMessage() + " " + t.getMessage());
        } else {
            buf.append(super.getMessage());
        }

        return buf.toString();
    }
}
