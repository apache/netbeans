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
package org.netbeans.modules.cnd.debugger.common2.ui.processlist;

import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 *
 */
public final class ProcessFilter {

    private String filter = ""; // NOI18N
    private final ChangeSupport cs = new ChangeSupport(this);

    public ProcessFilter() {
    }

    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    public synchronized void set(String filter) {
        if (filter == null) {
            filter = ""; // NOI18N
        }

        if (filter.equals(this.filter)) {
            return;
        }

        this.filter = filter;
        cs.fireChange();
    }

    /**
     *
     * @return NOT NULL filter
     */
    public synchronized String get() {
        return filter;
    }
}
