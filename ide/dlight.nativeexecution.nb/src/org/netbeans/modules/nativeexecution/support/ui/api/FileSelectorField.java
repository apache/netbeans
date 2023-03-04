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
package org.netbeans.modules.nativeexecution.support.ui.api;

import org.netbeans.modules.nativeexecution.ui.Completable;
import org.netbeans.modules.nativeexecution.ui.CompletionPopup;
import java.awt.AWTKeyStroke;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ak119685
 */
public final class FileSelectorField extends JTextField
        implements PopupMenuListener {

    private static final RequestProcessor rp = new RequestProcessor("FileSelectorField", 3); // NOI18N
    private final AtomicReference<CompletionTask> currentTask;
    private boolean listenersInactive = false;
    private AutocompletionProvider provider;
    private CompletionPopup popup;
    private final Completable completable = new CompletableImpl();

    public FileSelectorField() {
        this(null);
    }

    public FileSelectorField(AutocompletionProvider provider) {
        super();
        this.provider = provider;

        currentTask = new AtomicReference<>();

        getDocument().addDocumentListener(new DocumentListener() {

            private void doUpdate() {
                if (!listenersInactive) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            updateCompletions();
                        }
                    });
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                doUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent evt) {
                int keyCode = evt.getKeyCode();
                if ((keyCode == KeyEvent.VK_RIGHT && isAtLastPosition())
                        || keyCode == KeyEvent.VK_TAB) {
                    if (popup == null || !popup.isShowing()) {
                        updateCompletions();
                    }
                }
            }
        });
    }

    private void updateCompletions() {
        if (provider == null) {
            return;
        }

        if (!isFocusOwner()) {
            return;
        }

        if (popup == null) {
            popup = new CompletionPopup(completable);
            popup.addPopupMenuListener(this);
        }

        String text = getText();
        int pos = getCaretPosition();
        final String textToComplete = text.substring(0, Math.min(text.length(), pos));
        final CompletionTask newTask = new CompletionTask(textToComplete);

        CompletionTask old = currentTask.getAndSet(newTask);

        if (old != null) {
            old.cancel();
        }

        rp.post(newTask);
        popup.setWaiting();

        if (!popup.isShowing()) {
            popup.showPopup(this, 0, getHeight() - 2);
        }
    }

    private boolean isAtLastPosition() {
        return getCaretPosition() >= (getDocument().getLength() - 1);
    }

    public void setAutocompletionProvider(AutocompletionProvider provider) {
        this.provider = provider;
    }

    private final class CompletableImpl implements Completable {

        @Override
        public boolean completeWith(final String completion) {
            String orig = getText().substring(0, getCaretPosition());
            String newValue;
            if (orig.startsWith("/") || orig.startsWith(".") || orig.startsWith("~")) { // NOI18N
                newValue = orig.substring(0, orig.lastIndexOf('/') + 1) + completion;
            } else {
                newValue = completion;
            }

            boolean updateCompletions = newValue.endsWith("/"); // NOI18N
            setText(newValue, updateCompletions);
            return updateCompletions;
        }

        @Override
        public void requestFocus() {
            FileSelectorField.this.requestFocus();
        }

        @Override
        public void addKeyListener(KeyListener listener) {
            FileSelectorField.this.addKeyListener(listener);
        }
    }

    public void setText(String text, boolean updateCompletions) {
        if (updateCompletions) {
            super.setText(text);
        } else {
            listenersInactive = true;
            super.setText(text);
            listenersInactive = false;
        }
    }

    private void setTabTraversalEnabled(boolean enabled) {
        Set<AWTKeyStroke> tKeys = getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newTKeys = new HashSet<>(tKeys);
        if (enabled) {
            newTKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
        } else {
            newTKeys.remove(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
        }
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newTKeys);
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        setTabTraversalEnabled(false);
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        setTabTraversalEnabled(true);
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    private class CompletionTask implements Runnable {

        private volatile boolean cancelled;
        private final String textToComplete;

        public CompletionTask(String textToComplete) {
            this.textToComplete = textToComplete;
        }

        void cancel() {
            cancelled = true;
        }

        @Override
        public void run() {
            final List<String> result = provider.autocomplete(textToComplete);
            if (!cancelled) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        popup.setOptionsList(result);
                    }
                });

            }
        }
    }
}
