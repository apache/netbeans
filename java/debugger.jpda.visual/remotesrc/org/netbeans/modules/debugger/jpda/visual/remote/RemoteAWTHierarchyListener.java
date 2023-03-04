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
package org.netbeans.modules.debugger.jpda.visual.remote;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.WeakHashMap;

/**
 * Listens on changes in component hierarchy.
 * 
 * @author Martin Entlicher
 */
class RemoteAWTHierarchyListener implements AWTEventListener {
    
    private final WeakHashMap components = new WeakHashMap();

    public void eventDispatched(AWTEvent event) {
        HierarchyEvent e = (HierarchyEvent) event;
        if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
            Component c = e.getChanged();
            Container p = c.getParent();
            if (p == null) {
                // Component was removed from the hierarchy
                components.remove(c);
            } else {
                Throwable t = new RuntimeException();
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                String stackLine = getComponentAddStackLine(stackTrace);
                components.put(c, stackLine);
            }
        }
    }
    
    String getStackFromComponent(Component c) {
        return (String) components.get(c);
    }
    
    private static String getComponentAddStackLine(String stack) {
        int pos = 0;
        int eol;
        String lineSeparator = System.getProperty("line.separator");
        for ( ; (eol = stack.indexOf(lineSeparator, pos)) > 0; pos = eol + 1) {
            String line = stack.substring(pos, eol).trim();
            if (line.startsWith("at ")) {
                if (line.indexOf(RemoteAWTHierarchyListener.class.getName()) > 0) {
                    continue;
                }
                if (line.indexOf(" java.awt.") > 0 ||
                    line.indexOf(" javax.swing.") > 0 ||
                    line.indexOf(" com.sun.") > 0) {
                    
                    continue;
                }
                return line;
            }
        }
        return stack;
    }

}
