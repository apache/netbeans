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
package org.netbeans.modules.editor.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.StatusBar;
import org.netbeans.editor.Utilities;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;


/**
 * Factories for status bar elements.
 *
 * @author Miloslav Metelka
 */
public final class StatusLineFactories {
    
    private static final RequestProcessor WORKER = new RequestProcessor(StatusLineFactories.class.getName(), 1, false, false);

    // -J-Dorg.netbeans.modules.editor.impl.StatusLineFactories.level=FINE
    private static final Logger LOG = Logger.getLogger(StatusLineFactories.class.getName());

    public static JLabel LINE_COLUMN_CELL = new StatusLineComponent(StatusLineComponent.Type.LINE_COLUMN);

    public static JLabel TYPING_MODE_CELL = new StatusLineComponent(StatusLineComponent.Type.TYPING_MODE);

    public static final JLabel MAIN_CELL = new JLabel();
    static {
        MAIN_CELL.addPropertyChangeListener(new PropertyChangeListener() {
            private final AtomicReference<Message> previous = new AtomicReference<Message>();
            public void propertyChange(PropertyChangeEvent evt) {
                if ("text".equals(evt.getPropertyName())) {
                    String text = MAIN_CELL.getText();
                    if ("".equals(text)) {
                        Message message = previous.getAndSet(null);
                        
                        if (message != null) {
                            message.clear(0);
                        }
                        return;
                    }
                    Integer importance = (Integer)MAIN_CELL.getClientProperty("importance");
                    final StatusDisplayer.Message msg = StatusDisplayer.getDefault().setStatusText(
                            text, importance);
                    previous.set(msg);
                    WORKER.post(new Runnable() {
                        @Override public void run() {
                            if (previous.compareAndSet(msg, null)) {
                                msg.clear(0);
                            }
                        }
                    }, 5000);
                }
            }
        });
        StatusBar.setGlobalCell(StatusBar.CELL_MAIN, StatusLineFactories.MAIN_CELL);
        StatusBar.setGlobalCell(StatusBar.CELL_POSITION, StatusLineFactories.LINE_COLUMN_CELL);
        StatusBar.setGlobalCell(StatusBar.CELL_TYPING_MODE, StatusLineFactories.TYPING_MODE_CELL);
        // Listen on EditorRegistry
        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                refreshStatusLine();
            }
        });

    }

    private static void clearStatusLine() {
        LINE_COLUMN_CELL.setText("");
        TYPING_MODE_CELL.setText("");
    }

    static void refreshStatusLine() {
        LOG.fine("StatusLineFactories.refreshStatusLine()\n");
        List<? extends JTextComponent> componentList = EditorRegistry.componentList();
        for (JTextComponent component : componentList) {
            boolean underMainWindow = (SwingUtilities.isDescendingFrom(component,
                    WindowManager.getDefault().getMainWindow()));
            EditorUI editorUI = Utilities.getEditorUI(component);
            if (LOG.isLoggable(Level.FINE)) {
                String componentDesc = component.toString();
                Document doc = component.getDocument();
                Object streamDesc;
                if (doc != null && ((streamDesc = doc.getProperty(Document.StreamDescriptionProperty)) != null)) {
                    componentDesc = streamDesc.toString();
                }
                LOG.fine("  underMainWindow=" + underMainWindow + // NOI18N
                        ", text-component: " + componentDesc + "\n");
            }
            if (editorUI != null) {
                StatusBar statusBar = editorUI.getStatusBar();
                statusBar.setVisible(!underMainWindow);
                boolean shouldUpdateGlobal = underMainWindow && component.isShowing();
                if (shouldUpdateGlobal) {
                    statusBar.updateGlobal();
                    LOG.fine("  end of refreshStatusLine() - found main window component\n\n"); // NOI18N
                    return; // First non-docked one found and updated -> quit
                }
            }
        }
        clearStatusLine();
        LOG.fine("  end of refreshStatusLine() - no components - status line cleared\n\n"); // NOI18N
    }

    static Component panelWithSeparator(JLabel cell) {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(3, 3); // Y-unimportant -> gridlayout will stretch it
            }
        };
        separator.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(separator, BorderLayout.WEST);
        panel.add(cell);
        return panel;
    }

    @ServiceProvider(service=StatusLineElementProvider.class)
    public static final class LineColumn implements StatusLineElementProvider {

        public Component getStatusLineElement() {
            return panelWithSeparator(LINE_COLUMN_CELL);
        }

    }

    @ServiceProvider(service=StatusLineElementProvider.class)
    public static final class TypingMode implements StatusLineElementProvider {

        public Component getStatusLineElement() {
            return panelWithSeparator(TYPING_MODE_CELL);
        }

    }

}
