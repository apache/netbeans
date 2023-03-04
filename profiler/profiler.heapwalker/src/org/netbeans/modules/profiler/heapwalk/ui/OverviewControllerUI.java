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

package org.netbeans.modules.profiler.heapwalk.ui;

import java.awt.BorderLayout;
import java.net.URL;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.ui.UIConstants;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.lib.profiler.ui.components.JTitledPanel;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.heapwalk.OverviewController;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
@NbBundle.Messages({
    "OverviewControllerUI_ViewTitle=Overview",
    "OverviewControllerUI_ViewDescr=Overview",
    "OverviewControllerUI_InProgressMsg=In progress..."
})
public class OverviewControllerUI extends JTitledPanel {
    
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    
    private static Icon ICON_INFO = Icons.getIcon(GeneralIcons.INFO);
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    
    private AbstractButton presenter;
    private HTMLTextArea dataArea;
    
    // --- UI definition ---------------------------------------------------------
    private OverviewController overviewController;
    
    // --- Private implementation ------------------------------------------------
    private Instance instanceToSelect;
    private boolean showSysprops = false;
    private boolean showThreads = false;
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------
    
    // --- Constructors ----------------------------------------------------------
    public OverviewControllerUI(OverviewController controller) {
        super(Bundle.OverviewControllerUI_ViewTitle(),ICON_INFO,true);
        overviewController = controller;
        initComponents();
        refreshSummary();
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    // --- Public interface ------------------------------------------------------

    public void showInThreads(Instance instance) {
        if (!showThreads) {
            showThreads = true;
            instanceToSelect = instance;
            refreshSummary();
            return;
        }
        String referenceId = String.valueOf(instance.getInstanceId());
        
        dataArea.scrollToReference(referenceId);
        Document d = dataArea.getDocument();
        HTMLDocument doc = (HTMLDocument) d;
        HTMLDocument.Iterator iter = doc.getIterator(HTML.Tag.A);
        for (; iter.isValid(); iter.next()) {
            AttributeSet a = iter.getAttributes();
            String nm = (String) a.getAttribute(HTML.Attribute.NAME);
            if ((nm != null) && nm.equals(referenceId)) {
                dataArea.select(iter.getStartOffset(),iter.getEndOffset());
                dataArea.requestFocusInWindow();
            }
        }
    }

    private void refreshSummary() {
        if (!showSysprops && !showThreads) {
            dataArea.setText(Bundle.OverviewControllerUI_InProgressMsg());
        }
        
        BrowserUtils.performTask(new Runnable() {
            public void run() {
                String summary = "<nobr>" + overviewController.computeSummary() + "</nobr>"; // NOI18N
                String environment = "<nobr>" + overviewController.computeEnvironment() + "</nobr>"; // NOI18N
                String properties = "<nobr>" + overviewController.computeSystemProperties(showSysprops) + "</nobr>"; // NOI18N
                String threads = "<nobr>" + overviewController.computeThreads(showThreads) + "</nobr>"; // NOI18N
                final String dataAreaText = summary + "<br><br>" // NOI18N
                        + environment + "<br><br>" // NOI18N
                        + properties + "<br><br>" // NOI18N
                        + threads;
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        dataArea.setText(dataAreaText);
                        if (instanceToSelect != null) {
                            showInThreads(instanceToSelect);
                            instanceToSelect = null;
                        } else {
                            dataArea.setCaretPosition(0);
                        }
                    }
                });
            }
        });
    }
        
    private void initComponents() {
        // dataArea
        dataArea = new HTMLTextArea() {
            protected void showURL(URL url) {
                if (url == null) return;
                String urls = url.toString();
                if (urls.equals(OverviewController.SHOW_SYSPROPS_URL)) {
                    showSysprops = true;
                    refreshSummary();
                } else if (urls.equals(OverviewController.SHOW_THREADS_URL)) {
                    showThreads = true;
                    refreshSummary();
                } else if (urls.equals(OverviewController.SHOW_NEXT_SEGMENT_URL)) {
                    overviewController.showNextSegment();
                } else {
                    overviewController.showURL(urls);
                }
            }
        };
        dataArea.setEditorKit(new CustomHtmlEditorKit());
        dataArea.setSelectionColor(UIConstants.TABLE_SELECTION_BACKGROUND_COLOR);
        JScrollPane dataAreaScrollPane = new JScrollPane(dataArea,
                                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dataAreaScrollPane.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5,
                                        UIUtils.getProfilerResultsBackground()));
        dataAreaScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        dataAreaScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        dataAreaScrollPane.getHorizontalScrollBar().setUnitIncrement(10);

        JPanel contentsPanel = new JPanel();
        contentsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, getTitleBorderColor()));
        contentsPanel.setLayout(new BorderLayout());
        contentsPanel.setOpaque(true);
        contentsPanel.setBackground(dataArea.getBackground());
        contentsPanel.add(dataAreaScrollPane, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(contentsPanel, BorderLayout.CENTER);
        
        // UI tweaks
        setBackground(dataArea.getBackground());
    }

    private class CustomHtmlEditorKit extends HTMLEditorKit {

        @Override
        public Document createDefaultDocument() {
            StyleSheet styles = getStyleSheet();
            StyleSheet ss = new StyleSheet();

            ss.addStyleSheet(styles);

            HTMLDocument doc = new CustomHTMLDocument(ss);
            doc.setParser(getParser());
            doc.setAsynchronousLoadPriority(4);
            doc.setTokenThreshold(100);
            return doc;
        }
    }
    
    private class CustomHTMLDocument extends HTMLDocument {
        private static final int CACHE_BOUNDARY = 1000;
        private char[] segArray;
        private int segOffset;
        private int segCount;
        private boolean segPartialReturn;
        private int lastOffset;
        private int lastLength;
        
        private CustomHTMLDocument(StyleSheet ss) {
            super(ss);
            lastOffset = -1;
            lastLength = -1;
            putProperty("multiByte", Boolean.TRUE);      // NOI18N
        }

        @Override
        public void getText(int offset, int length, Segment txt) throws BadLocationException {
            if (lastOffset == offset && lastLength == length) {
                txt.array = segArray;
                txt.offset = segOffset;
                txt.count = segCount;
                txt.setPartialReturn(segPartialReturn);
                return;
            }
            super.getText(offset, length, txt);
            if (length > CACHE_BOUNDARY || lastLength <= CACHE_BOUNDARY) {
                segArray = txt.array;
                segOffset = txt.offset;
                segCount = txt.count;
                segPartialReturn = txt.isPartialReturn();
                lastOffset = offset;
                lastLength = length;
            }
        }
    }

}
