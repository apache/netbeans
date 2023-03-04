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

package org.netbeans.modules.uihandler;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
@org.openide.util.lookup.ServiceProviders({@org.openide.util.lookup.ServiceProvider(service=java.util.logging.Handler.class), @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.uihandler.EarlyHandler.class)})
public final class EarlyHandler extends Handler {
    
    final Queue<LogRecord> earlyRecords = new ArrayDeque<>();
    private volatile boolean isOn = true;
    
    public EarlyHandler() {
        setLevel(Level.ALL);
    }
    
    public static void disable() {
        EarlyHandler eh = Lookup.getDefault().lookup(EarlyHandler.class);
        eh.setLevel(Level.OFF);
        eh.isOn = false;
    }

    @Override
    public void publish(LogRecord record) {
        if (isOn && record.getLoggerName() != null) {
            synchronized (earlyRecords) {
                earlyRecords.add(record);
            }
        }
    }
    
    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
    
}
