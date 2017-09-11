/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
