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

package org.netbeans.modules.web.jspparser;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsps.parserapi.TldChangeEvent;
import org.netbeans.modules.web.jsps.parserapi.TldChangeListener;
import org.openide.util.Exceptions;

/**
 * Heavily inspired by {@link org.openide.util.ChangeSupport}.
 * @author Tomas Mysik
 */
public class TldChangeSupport {
    private final List<TldChangeListener> listeners = new CopyOnWriteArrayList<TldChangeListener>();
    private final Object source;

    public TldChangeSupport(Object source) {
        this.source = source;
    }

    public void addTldChangeListener(TldChangeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    public void removeTldChangeListener(TldChangeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    public void fireChange(WebModule webModule) {
        if (listeners.isEmpty()) {
            return;
        }
        fireChange(new TldChangeEvent(source, webModule));
    }

    private void fireChange(TldChangeEvent event) {
        assert event != null;
        for (TldChangeListener listener : listeners) {
            try {
                listener.tldChange(event);
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

    public boolean hasListeners() {
        return !listeners.isEmpty();
    }
}
