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

package org.netbeans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

/**
 * XXX replace with MockPropertyChangeListener
 */
public final class LoggedPCListener implements PropertyChangeListener {

    private final Set<PropertyChangeEvent> changes = new HashSet<PropertyChangeEvent>(100);

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        changes.add(evt);
        notify();
    }

    public synchronized void waitForChanges() throws InterruptedException {
        wait(5000);
    }

    public synchronized boolean hasChange(Object source, String prop) {
        for (PropertyChangeEvent ev : changes) {
            if (source == ev.getSource()) {
                if (prop.equals(ev.getPropertyName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean waitForChange(Object source, String prop) throws InterruptedException {
        while (!hasChange(source, prop)) {
            long start = System.currentTimeMillis();
            waitForChanges();
            if (System.currentTimeMillis() - start > 4000) {
                //System.err.println("changes=" + changes);
                return false;
            }
        }
        return true;
    }

}
