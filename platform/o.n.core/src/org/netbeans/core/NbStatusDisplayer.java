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

package org.netbeans.core;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default status displayer implementation; GUI is in StatusLine.
 */
@ServiceProvider(service=StatusDisplayer.class)
public final class NbStatusDisplayer extends StatusDisplayer {

    private final ChangeSupport cs = new ChangeSupport(this);
    //list of status messages sorted by their importance in descending order
    private List<WeakReference<MessageImpl>> messages = new ArrayList<WeakReference<MessageImpl>>(30);
    private static int SURVIVING_TIME = Integer.getInteger("org.openide.awt.StatusDisplayer.DISPLAY_TIME", 5000);

    private static final RequestProcessor RP = new RequestProcessor("NbStatusDisplayer"); //NOI18N

    public void setStatusText(String text) {
        //unimportant message are cleared automatically after some time
        add(text, 0).clear(SURVIVING_TIME);
    }

    @Override
    public Message setStatusText(String text, int importance) {
        if (importance <= 0) {
            throw new IllegalArgumentException("Invalid importance value: " + importance);
        }
        return add(text, importance);
    }

    public synchronized String getStatusText() {
        String text = null;
        synchronized (this) {
            MessageImpl msg = getCurrent();
            text = null == msg ? "" : msg.text;
        }
        return text;
    }

    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    /**
     * @return The most important status message in the list or null
     */
    private MessageImpl getCurrent() {
        while (!messages.isEmpty()) {
            WeakReference<MessageImpl> ref = messages.get(0);
            MessageImpl impl = ref.get();
            if (null != impl) {
                return impl;
            } else {
                messages.remove(0);
            }
        }
        return null;
    }

    /**
     * Add new status message and fire change event. The message won't show
     * in the status line if there's already a message with higher importance.
     * @param text Status line message
     * @param importance Message importance
     * @return New status Message
     */
    private MessageImpl add(String text, int importance) {
        MessageImpl newMsg = new MessageImpl(text, importance);
        WeakReference<MessageImpl> newRef = new WeakReference<MessageImpl>(newMsg);
        synchronized (this) {
            boolean added = false;
            for (int i = 0; i < messages.size() && !added; i++) {
                WeakReference<MessageImpl> ref = messages.get(0);
                MessageImpl impl = ref.get();
                if (impl == null) {
                    continue;
                }
                if (impl.importance == importance) {
                    messages.set(i, newRef);
                    added = true;
                } else if (impl.importance < importance) {
                    messages.add(i, newRef);
                    added = true;
                }
            }
            if (!added) {
                messages.add(newRef);
            }
        }
        cs.fireChange();
        Logger.getLogger(NbStatusDisplayer.class.getName()).log(Level.FINE,
                "Status text updated: {0}, importance: {1}", new Object[] {text, importance});
        return newMsg;
    }

    /**
     * Remove given message and fire change event. If there's a less important
     * message in the list, it will show in status line.
     * @param toRemove
     */
    private void remove(MessageImpl toRemove) {
        boolean removed = false;
        synchronized (this) {
            WeakReference<MessageImpl> refToRemove = null;
            for (WeakReference<MessageImpl> ref : messages) {
                if (toRemove == ref.get()) {
                    refToRemove = ref;
                    break;
                }
            }
            if (null != refToRemove) {
                removed = messages.remove(refToRemove);
            }
        }
        if (removed) {
            cs.fireChange();
        }
    }

    /**
     * Status line message which clears itself when garbage collected.
     */
    private class MessageImpl implements StatusDisplayer.Message, Runnable {

        private final String text;
        private final int importance;

        public MessageImpl(String text, int importance) {
            this.text = text;
            this.importance = importance;
        }

        public void clear(int timeInMillis) {
            RP.post(this, timeInMillis);
        }

        @Override
        protected void finalize() throws Throwable {
            run();
        }

        public void run() {
            remove(this);
        }
    }
}
