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
package org.netbeans.modules.php.project.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.api.PhpLanguageProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.awt.StatusLineElementProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Show a PHP Version of project properties on the status line.
 *
 * Available version numbers are shown when a version number is clicked. We
 * click a specific version number of the version list, we can change the PHP
 * Version to it without opening a project properties dialog.
 */
@ServiceProvider(service = StatusLineElementProvider.class)
public class PhpVersionStatusLineElementProvider implements StatusLineElementProvider {

    @Override
    public Component getStatusLineElement() {
        return panelWithSeparator(PhpVersionLabel.getInstance());
    }

    private static Component panelWithSeparator(JLabel cell) {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL) {
            private static final long serialVersionUID = 4857471448025818174L;

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

    //~ Inner classes
    private static final class PhpVersionLabel extends JLabel {

        private static final RequestProcessor RP = new RequestProcessor(PhpVersionLabel.class);
        private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);
        private static final PhpVersionLabel INSTANCE = new PhpVersionLabel();
        private static final long serialVersionUID = -3407578574821051662L;

        static {
            EditorRegistry.addPropertyChangeListener(propertyChangeEvent -> getInstance().updatePhpVersion());
        }

        private PhpVersionLabel() {
            // show the version list when the label is clicked
            addMouseListener(new MouseAdapterImpl());
            initMinDimension(getPhpVersionStrings());
        }

        public static PhpVersionLabel getInstance() {
            return INSTANCE;
        }

        @Override
        public Point getToolTipLocation(MouseEvent event) {
            // show the tooltip left side of the label
            String toolTipText = getToolTipText(event);
            JToolTip jToolTip = new JToolTip();
            jToolTip.setTipText(toolTipText);
            return new Point(-5 - jToolTip.getPreferredSize().width, 0);
        }

        private Collection<String> getPhpVersionStrings() {
            Collection<String> phpVersionStrings = new ArrayList<>();
            for (PhpVersion phpVersion : PhpVersion.values()) {
                phpVersionStrings.add(phpVersion.getDisplayName());
            }
            return phpVersionStrings;
        }

        private void initMinDimension(Iterable<? extends String> maxStrings) {
            FontMetrics fontMetrics = getFontMetrics(getFont());
            int minWidth = 0;
            for (String string : maxStrings) {
                minWidth = Math.max(minWidth, fontMetrics.stringWidth(string));
            }
            Border border = getBorder();
            Insets insets = (border != null) ? border.getBorderInsets(this) : NULL_INSETS;
            minWidth += insets.left + insets.right;
            int minHeight = fontMetrics.getHeight() + insets.top + insets.bottom;
            setMinimumSize(new Dimension(minWidth, minHeight));
            setPreferredSize(new Dimension(minWidth, minHeight));
        }

        private void updatePhpVersion() {
            assert EventQueue.isDispatchThread();
            final JTextComponent comp = EditorRegistry.focusedComponent();
            if (comp != null) {
                showPhpVersion(comp.getDocument());
            } else {
                clear();
            }
        }

        @NbBundle.Messages("PhpVersionLabel.loading.message=Loading...")
        private void showPhpVersion(Document document) {
            setText(Bundle.PhpVersionLabel_loading_message());
            RP.post(() -> {
                FileObject fileObject = NbEditorUtilities.getFileObject(document);
                PhpVersion phpVersion = null;
                String projectName = null;
                if (fileObject != null) {
                    PhpProject phpProject = PhpProjectUtils.getPhpProject(fileObject);
                    if (phpProject != null) {
                        projectName = phpProject.getName();
                        phpVersion = PhpLanguageProperties.forFileObject(fileObject).getPhpVersion();
                    }
                }
                showPhpVersionOnDispatchThread(projectName, phpVersion);
            });
        }

        private void showPhpVersionOnDispatchThread(String projectName, PhpVersion phpVersion) {
            if (EventQueue.isDispatchThread()) {
                showPhpVersion(projectName, phpVersion);
            } else {
                SwingUtilities.invokeLater(() -> showPhpVersion(projectName, phpVersion));
            }
        }

        private void showPhpVersion(String projectName, PhpVersion phpVersion) {
            assert EventQueue.isDispatchThread();
            if (projectName != null && phpVersion != null) {
                setToolTipText(projectName);
                setText(phpVersion.getDisplayName());
                setVisible(true);
            } else {
                clear();
            }
        }

        private void clear() {
            assert EventQueue.isDispatchThread();
            setToolTipText(null);
            setText(""); // NOI18N
            setVisible(false);
        }

        private JList<PhpVersion> createPhpVersionList(JTextComponent component, FileObject fileObject) {
            JList<PhpVersion> phpVersionList = new JList<>();
            DefaultListModel<PhpVersion> model = new DefaultListModel<>();

            for (PhpVersion phpVersion : PhpVersion.values()) {
                model.addElement(phpVersion);
            }

            phpVersionList.setModel(model);
            PhpVersion phpVersion = PhpLanguageProperties.forFileObject(fileObject).getPhpVersion();
            phpVersionList.setSelectedValue(phpVersion, true);
            phpVersionList.setBorder(new LineBorder(Color.GRAY, 1));

            phpVersionList.addListSelectionListener((ListSelectionEvent e) -> {
                savePhpVersion(component, phpVersionList.getSelectedValue());
            });
            return phpVersionList;
        }

        void showPhpVersionList() {
            final JTextComponent component = EditorRegistry.focusedComponent();
            if (component == null) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            FileObject fileObject = NbEditorUtilities.getFileObject(component.getDocument());
            if (fileObject == null) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            final JList<PhpVersion> phpVersionList = createPhpVersionList(component, fileObject);
            final Popup popup = getPopup(phpVersionList);
            popup.show();
        }

        private Popup getPopup(JList<PhpVersion> phpVersionList) {
            Point labelStart = getLocationOnScreen();
            int x = Math.min(labelStart.x, labelStart.x + getSize().width - phpVersionList.getPreferredSize().width);
            int y = labelStart.y - phpVersionList.getPreferredSize().height;

            final Popup popup = PopupFactory.getSharedInstance().getPopup(this, phpVersionList, x, y);
            final AWTEventListener multicastListener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (event instanceof MouseEvent && ((MouseEvent) event).getClickCount() > 0) {
                        popup.hide();
                        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                    }
                }
            };
            Toolkit.getDefaultToolkit().addAWTEventListener(multicastListener, AWTEvent.MOUSE_EVENT_MASK);
            return popup;
        }

        private void savePhpVersion(JTextComponent component, PhpVersion phpVersion) {
            RP.post(() -> {
                FileObject fileObject = NbEditorUtilities.getFileObject(component.getDocument());
                if (fileObject != null) {
                    PhpProject phpProject = PhpProjectUtils.getPhpProject(fileObject);
                    if (phpProject != null) {
                        PhpProjectProperties projectProperties = new PhpProjectProperties(phpProject);
                        projectProperties.setPhpVersion(phpVersion.name());
                        projectProperties.save();

                        SwingUtilities.invokeLater(() -> showPhpVersion(component.getDocument()));
                    }
                }
            });
        }
    }

    private static class MouseAdapterImpl extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            PhpVersionLabel.getInstance().showPhpVersionList();
        }

    }
}
