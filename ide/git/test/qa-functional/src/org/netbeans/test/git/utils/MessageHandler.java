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
package org.netbeans.test.git.utils;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author pvcs
 */
public final class MessageHandler extends Handler {

    String message;
    boolean started = false;
    boolean finished = false;

    public MessageHandler(String message) {
        this.message = message;
    }

    @Override
    public void publish(LogRecord record) {
//        throw new UnsupportedOperationException("Not supported yet.");
        if (started == false) {
            if (record.getMessage().indexOf("Start - " + message) > -1) {
                started = true;
                finished = false;
            }
        }
        if (started) {
            if (record.getMessage().indexOf("End - " + message) > -1) {
                started = false;
                finished = true;
            }
        }
    }

    @Override
    public void flush() {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws SecurityException {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isFinished() {
        return finished;
    }
}
