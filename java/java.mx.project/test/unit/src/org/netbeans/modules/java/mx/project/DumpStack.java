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
package org.netbeans.modules.java.mx.project;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

final class DumpStack extends TimerTask {
    private static final Timer TIMER = new Timer("Dump Stack Watchdog");
    private final long created = System.currentTimeMillis();
    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();
        long after = (System.currentTimeMillis() - created) / 1000;
        sb.append("Thread dump after ").append(after).append(" s from start:\n");
        for (Map.Entry<Thread, StackTraceElement[]> info : Thread.getAllStackTraces().entrySet()) {
            sb.append(info.getKey().getName()).append("\n");
            for (StackTraceElement e : info.getValue()) {
                sb.append("    ").append(e.getClassName()).append(".").
                   append(e.getMethodName()).append("(").append(e.getFileName()).
                   append(":").append(e.getLineNumber()).append(")\n");
            }
        }
        Logger.getLogger(DumpStack.class.getName()).warning(sb.toString());
    }
    
    public static void start() {
        final int tenMinutes = 1000 * 60 * 10;
        final int tenSeconds = 10000;
        TIMER.schedule(new DumpStack(), tenMinutes, tenSeconds);
    }
}
