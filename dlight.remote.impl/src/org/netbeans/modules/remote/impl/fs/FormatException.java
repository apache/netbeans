/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.remote.impl.fs;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.modules.remote.impl.RemoteLogger;

/**
 *
 */
public class FormatException extends Exception {

    private final boolean expected;
    private static final int REPORT_THRESHOLD = 10;
    private static AtomicInteger count = new AtomicInteger();

    public FormatException(String text, boolean expected) {
        super(text);
        this.expected = expected;
    }

    public FormatException(String string, Throwable thrwbl) {
        super(string, thrwbl);
        expected = false;
    }

    public boolean isExpected() {
        return expected;
    }

    public static void reportIfNeeded(FormatException e) {
        Level level;
        if (!e.isExpected() && count.incrementAndGet() == REPORT_THRESHOLD) {
            level = Level.WARNING;
        } else {
            level = Level.FINE;
        }
        RemoteLogger.getInstance().log(level, "Error reading directory cache", e); // NOI18N
    }
}
