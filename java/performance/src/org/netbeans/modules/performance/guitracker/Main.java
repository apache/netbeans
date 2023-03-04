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
package org.netbeans.modules.performance.guitracker;

/**
 * An entry point to start an application that initializes event logging and
 * passes logic to original main class that is specified as first parameter
 *
 * @author Radim Kubacki
 */
public class Main {

    public Main() {
    }

    public static void main(String[] args) {
        String clzName = System.getProperty("guitracker.mainclass");
        if (clzName == null) {
            throw new IllegalStateException("No main class defined. Use -Dguitracker.mainclass=<classname>");
        }
        // init tracker and EQ now
        ActionTracker tr;

        LoggingRepaintManager rm;

        LoggingEventQueue leq;

        // load our EQ and repaint manager
        tr = ActionTracker.getInstance();
        rm = new LoggingRepaintManager(tr);
        rm.setEnabled(true);
        leq = new LoggingEventQueue(tr);
        leq.setEnabled(true);
        tr.setInteractive(true);
        tr.connectToAWT(true);
        tr.startNewEventList("ad hoc");
        tr.startRecording();

        try {
            Class<?> clz = Class.forName(clzName);
            clz.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot pass control to " + clzName, ex);
        }
    }
}
