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

package org.netbeans.modules.editor.impl.crlf;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.StatusLineElementProvider;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@NbBundle.Messages({
    "LBL_CR=Mac OS 9 (CR)",
    "LBL_CRLF=Windows (CRLF)",
    "LBL_LF=Unix (LF)",
    "LBL_Unknown=Unknown"
})
public class CRLFStatus {

    private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);
    private static final JLabel GLOBAL_CRLF = new JLabel("    ");
    private static final Map<String, String> LINE_ENDINGS_DN = new HashMap<String, String>();
    private static final String UNKNOWN = Bundle.LBL_Unknown();

    static {
        LINE_ENDINGS_DN.put(BaseDocument.LS_CR, Bundle.LBL_CR());
        LINE_ENDINGS_DN.put(BaseDocument.LS_CRLF, Bundle.LBL_CRLF());
        LINE_ENDINGS_DN.put(BaseDocument.LS_LF, Bundle.LBL_LF());

        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                updateCRLFComponent();
            }
        });

        GLOBAL_CRLF.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                final JTextComponent comp = EditorRegistry.focusedComponent();

                if (comp == null) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                final JList l = new JList();
                DefaultListModel model = new DefaultListModel();

                for (String k : LINE_ENDINGS_DN.keySet()) {
                    model.addElement(k);
                }

                l.setModel(model);
                l.setSelectedValue(comp.getDocument().getProperty(BaseDocument.READ_LINE_SEPARATOR_PROP), true);
                l.setCellRenderer(new DefaultListCellRenderer() {
                    @Override @SuppressWarnings("element-type-mismatch")
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        return super.getListCellRendererComponent(list, LINE_ENDINGS_DN.get(value), index, isSelected, cellHasFocus);
                    }
                });
                l.setBorder(new LineBorder(Color.GRAY, 1));

                Point labelStart = GLOBAL_CRLF.getLocationOnScreen();
                int x = Math.min(labelStart.x, labelStart.x + GLOBAL_CRLF.getSize().width - l.getPreferredSize().width);
                int y = labelStart.y - l.getPreferredSize().height;

                final Popup popup = PopupFactory.getSharedInstance().getPopup(GLOBAL_CRLF, l, x, y);
                final AWTEventListener multicastListener = new AWTEventListener() {
                     @Override public void eventDispatched(AWTEvent event) {
                         if (event instanceof MouseEvent && ((MouseEvent) event).getClickCount() > 0) {
                             popup.hide();
                             Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                         }
                     }
                 };

                Toolkit.getDefaultToolkit().addAWTEventListener(multicastListener, AWTEvent.MOUSE_EVENT_MASK);

                l.addListSelectionListener(new ListSelectionListener() {
                    @Override public void valueChanged(ListSelectionEvent e) {
                        comp.getDocument().putProperty(BaseDocument.READ_LINE_SEPARATOR_PROP, l.getSelectedValue());
                        DataObject dataObject = NbEditorUtilities.getDataObject(comp.getDocument());

                        if (dataObject != null) {
                            try {
                                EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);
                                Method notifyModified = CloneableEditorSupport.class.getDeclaredMethod("callNotifyModified");

                                notifyModified.setAccessible(true);
                                notifyModified.invoke(ec);
                            } catch (ReflectiveOperationException | SecurityException ex) {
                                Logger.getLogger(CRLFStatus.class.getName()).log(Level.FINE, null, ex);
                            }
                        }
                        showLE(comp.getDocument(), GLOBAL_CRLF);
                    }
                });

                popup.show();
            }
        });

        Collection<String> dimensions = new ArrayList<String>(LINE_ENDINGS_DN.values());

        dimensions.add(UNKNOWN);

        initMinDimension(dimensions);
    }

    private static void updateCRLFComponent() {
        final JTextComponent comp = EditorRegistry.focusedComponent();

        if (comp != null) {
            showLE(comp.getDocument(), GLOBAL_CRLF);
        } else {
            GLOBAL_CRLF.setText("    ");
        }
    }

    private static void showLE(Document doc, JLabel l) {
        @SuppressWarnings("element-type-mismatch")
        String dn = LINE_ENDINGS_DN.get(doc.getProperty(BaseDocument.READ_LINE_SEPARATOR_PROP));

        if (dn == null) {
            dn = UNKNOWN;
        }

        l.setText(dn);
    }

    private static void initMinDimension(Iterable<? extends String> maxStrings) {
        FontMetrics fm = GLOBAL_CRLF.getFontMetrics(GLOBAL_CRLF.getFont());
        int minWidth = 0;
        for (String s : maxStrings) {
            minWidth = Math.max(minWidth, fm.stringWidth(s));
        }
        Border b = GLOBAL_CRLF.getBorder();
        Insets ins = (b != null) ? b.getBorderInsets(GLOBAL_CRLF) : NULL_INSETS;
        minWidth += ins.left + ins.right;
        int minHeight = fm.getHeight() + ins.top + ins.bottom;
        GLOBAL_CRLF.setMinimumSize(new Dimension(minWidth, minHeight));
        GLOBAL_CRLF.setPreferredSize(new Dimension(minWidth, minHeight));
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
    public static final class StatusLineElementProviderImpl implements StatusLineElementProvider {
        @Override public Component getStatusLineElement() {
            return panelWithSeparator(GLOBAL_CRLF);
        }
    }
}
