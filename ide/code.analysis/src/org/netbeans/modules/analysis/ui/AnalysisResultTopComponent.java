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
package org.netbeans.modules.analysis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.api.project.Project;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.analysis.AnalysisResult;
import org.netbeans.modules.analysis.DescriptionReader;
import org.netbeans.modules.analysis.RunAnalysis;
import org.netbeans.modules.analysis.RunAnalysisPanel.DialogState;
import org.netbeans.modules.analysis.RunAnalysisPanel.FutureWarnings;
import org.netbeans.modules.analysis.spi.Analyzer.AnalyzerFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.awt.ActionID;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.netbeans.modules.analysis.ui//AnalysisResult//EN",
autostore = false)
@TopComponent.Description(preferredID = AnalysisResultTopComponent.PREFERRED_ID,
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false, position=12000)
@ActionID(category = "Window", id = "org.netbeans.modules.analysis.ui.AnalysisResultTopComponent")
@TopComponent.OpenActionRegistration(displayName = "#CTL_AnalysisResultAction",
preferredID = "AnalysisResultTopComponent")
@Messages({
    "CTL_AnalysisResultAction=Inspector",
    "CTL_AnalysisResultTopComponent=Inspector",
    "HINT_AnalysisResultTopComponent=This is an Inspector Window"
})
public final class AnalysisResultTopComponent extends TopComponent implements ExplorerManager.Provider {

    static final String PREFERRED_ID = "AnalysisResultTopComponent";
    private final ExplorerManager manager = new ExplorerManager();

    private Lookup context;
    private DialogState dialogState;
    private BeanTreeView btv;
    
    public AnalysisResultTopComponent() {
        initComponents();
        setName(Bundle.CTL_AnalysisResultTopComponent());
        setToolTipText(Bundle.HINT_AnalysisResultTopComponent());

        btv = new BeanTreeView();

        btvHolder.setLayout(new BorderLayout());
        btvHolder.add(btv, BorderLayout.CENTER);

        btv.setRootVisible(false);

        prevAction = new PreviousError(this);
        nextAction = new NextError(this);

        PCLImpl l = new PCLImpl();

        prevAction.addPropertyChangeListener(l);
        nextAction.addPropertyChangeListener(l);

        previousError.setEnabled(prevAction.isEnabled());
        nextError.setEnabled(nextAction.isEnabled());
        
        setData(Lookup.EMPTY, null, new AnalysisResult(Collections.<AnalyzerFactory, List<ErrorDescription>>emptyMap(), Collections.<ErrorDescription, Project>emptyMap(), new FutureWarnings(), Collections.<Node>emptyList()));

        getActionMap().put("jumpNext", nextAction);
        getActionMap().put("jumpPrev", prevAction);

        HTMLEditorKit hek = new HTMLEditorKit();
        StyleSheet styleSheet = (hek).getStyleSheet();

        //same as in netbeans.css:
        styleSheet.addRule("body { font-family: Verdana, \"Verdana CE\",  Arial, \"Arial CE\", \"Lucida Grande CE\", lucida, \"Helvetica CE\", sans-serif; }");
        styleSheet.addRule("h1 { font-weight: bold; font-size: 100% }");
        hek.setStyleSheet(styleSheet);
        descriptionPanel.setEditorKit(hek);
        descriptionPanel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        manager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                Node[] selectedNodes = manager.getSelectedNodes();

                if (selectedNodes.length == 1) {
                    DescriptionReader rd = selectedNodes[0].getLookup().lookup(DescriptionReader.class);
                    CharSequence description = rd != null ? rd.getDescription() : null;
                    descriptionPanel.setText(description != null ? description.toString() : null);
                }

                selectOnEnable = false;
            }
        });

        descriptionPanel.addHyperlinkListener(new HyperlinkListener() {
            @Override public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == EventType.ACTIVATED && e.getURL() != null) {
                    //if ("file".equals(e.getURL().getProtocol())||"rfs".equals(e.getURL().getProtocol())) {
                        if (e.getURL().getRef() == null) {
                            FileObject file = URLMapper.findFileObject(e.getURL());

                            if (file != null) {
                                EditCookie ec = file.getLookup().lookup(EditCookie.class);

                                if (ec != null) {
                                    ec.edit();
                                    return ;
                                }

                                OpenCookie oc = file.getLookup().lookup(OpenCookie.class);

                                if (oc != null) {
                                    oc.open();
                                    return ;
                                }
                            }
                        } else {
                            try {
                                int line;
                                if (e.getURL().getRef().startsWith("line")) {
                                    line = Integer.parseInt(e.getURL().getRef().substring(4));
                                } else {
                                    line = Integer.parseInt(e.getURL().getRef());
                                }
                                String s = e.getURL().toExternalForm();
                                URL url;
                                if (s.indexOf("#")>0) {
                                    s = s.substring(0, s.indexOf("#"));
                                    url = new URL(s);
                                } else {
                                    url=e.getURL();
                                }
                                FileObject file = URLMapper.findFileObject(url);
                                if (file != null) {
                                    DataObject dobj = DataObject.find(file);
                                    if (dobj != null) {
                                        NbDocument.openDocument(dobj, line-1, 0, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                                        return;
                                    }
                                }
                            } catch (NumberFormatException ex) {
                            } catch (MalformedURLException ex) {
                            } catch (DataObjectNotFoundException ex) {
                            }
                        }
                    //}
                    URLDisplayer.getDefault().showURL(e.getURL());
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        btvHolder = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionPanel = new javax.swing.JEditorPane();
        jToolBar1 = new javax.swing.JToolBar();
        refreshButton = new javax.swing.JButton();
        previousError = new javax.swing.JButton();
        nextError = new javax.swing.JButton();
        byCategory = new javax.swing.JToggleButton();

        jSplitPane1.setBorder(null);
        jSplitPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jSplitPane1ComponentResized(evt);
            }
        });

        javax.swing.GroupLayout btvHolderLayout = new javax.swing.GroupLayout(btvHolder);
        btvHolder.setLayout(btvHolderLayout);
        btvHolderLayout.setHorizontalGroup(
            btvHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 369, Short.MAX_VALUE)
        );
        btvHolderLayout.setVerticalGroup(
            btvHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(btvHolder);

        jScrollPane1.setBorder(null);

        descriptionPanel.setEditable(false);
        descriptionPanel.setContentType(org.openide.util.NbBundle.getMessage(AnalysisResultTopComponent.class, "AnalysisResultTopComponent.descriptionPanel.contentType")); // NOI18N
        descriptionPanel.putClientProperty(javax.swing.JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        jScrollPane1.setViewportView(descriptionPanel);

        jSplitPane1.setRightComponent(jScrollPane1);

        jToolBar1.setBorder(new VariableRightBorder());
        jToolBar1.setFloatable(false);
        jToolBar1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jToolBar1.setRollover(true);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/analysis/ui/resources/refresh.png"))); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getBundle(AnalysisResultTopComponent.class).getString("AnalysisResultTopComponent.refreshButton.toolTipText")); // NOI18N
        refreshButton.setBorderPainted(false);
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setMaximumSize(new java.awt.Dimension(24, 24));
        refreshButton.setMinimumSize(new java.awt.Dimension(24, 24));
        refreshButton.setPreferredSize(new java.awt.Dimension(24, 24));
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(refreshButton);

        previousError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/analysis/ui/resources/prevmatch.png"))); // NOI18N
        previousError.setToolTipText(org.openide.util.NbBundle.getBundle(AnalysisResultTopComponent.class).getString("AnalysisResultTopComponent.previousError.toolTipText")); // NOI18N
        previousError.setBorderPainted(false);
        previousError.setFocusable(false);
        previousError.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        previousError.setMaximumSize(new java.awt.Dimension(24, 24));
        previousError.setMinimumSize(new java.awt.Dimension(24, 24));
        previousError.setPreferredSize(new java.awt.Dimension(24, 24));
        previousError.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        previousError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousErrorActionPerformed(evt);
            }
        });
        jToolBar1.add(previousError);

        nextError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/analysis/ui/resources/nextmatch.png"))); // NOI18N
        nextError.setToolTipText(org.openide.util.NbBundle.getBundle(AnalysisResultTopComponent.class).getString("AnalysisResultTopComponent.nextError.toolTipText")); // NOI18N
        nextError.setBorderPainted(false);
        nextError.setFocusable(false);
        nextError.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextError.setMaximumSize(new java.awt.Dimension(24, 24));
        nextError.setMinimumSize(new java.awt.Dimension(24, 24));
        nextError.setPreferredSize(new java.awt.Dimension(24, 24));
        nextError.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        nextError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextErrorActionPerformed(evt);
            }
        });
        jToolBar1.add(nextError);

        byCategory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/analysis/ui/resources/categorize.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(byCategory, org.openide.util.NbBundle.getMessage(AnalysisResultTopComponent.class, "AnalysisResultTopComponent.byCategory.text")); // NOI18N
        byCategory.setToolTipText(org.openide.util.NbBundle.getMessage(AnalysisResultTopComponent.class, "BTN_Categorize")); // NOI18N
        byCategory.setBorderPainted(false);
        byCategory.setFocusable(false);
        byCategory.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        byCategory.setMaximumSize(new java.awt.Dimension(24, 24));
        byCategory.setMinimumSize(new java.awt.Dimension(24, 24));
        byCategory.setPreferredSize(new java.awt.Dimension(24, 24));
        byCategory.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        byCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                byCategoryActionPerformed(evt);
            }
        });
        jToolBar1.add(byCategory);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        RunAnalysis.showDialogAndRunAnalysis(context, dialogState);
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void nextErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextErrorActionPerformed
        nextAction.actionPerformed(null);
    }//GEN-LAST:event_nextErrorActionPerformed

    private void previousErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousErrorActionPerformed
        prevAction.actionPerformed(null);
    }//GEN-LAST:event_previousErrorActionPerformed

    private void byCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_byCategoryActionPerformed
        manager.setRootContext(Nodes.constructSemiLogicalView(analysisResult, byCategory.isSelected()));
        updatePrevNextButtonsForNewRootContext();
    }//GEN-LAST:event_byCategoryActionPerformed

    private void jSplitPane1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jSplitPane1ComponentResized
        jSplitPane1.setDividerLocation(0.5);
    }//GEN-LAST:event_jSplitPane1ComponentResized

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel btvHolder;
    private javax.swing.JToggleButton byCategory;
    private javax.swing.JEditorPane descriptionPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton nextError;
    private javax.swing.JButton previousError;
    private javax.swing.JButton refreshButton;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    boolean selectOnEnable;
    final PreviousError prevAction;
    final NextError nextAction;

    AnalysisResult analysisResult;

    public void setData(Lookup context, DialogState dialogState, AnalysisResult analysisResult) {
        this.context = context;
        this.dialogState = dialogState;
        this.analysisResult = analysisResult;
        manager.setRootContext(Nodes.constructSemiLogicalView(analysisResult, byCategory.isSelected()));
        if (btv != null) {
            btv.expandAll();
        }
        refreshButton.setEnabled(context != Lookup.EMPTY);
        updatePrevNextButtonsForNewRootContext();
    }
    
    private void updatePrevNextButtonsForNewRootContext() {
        descriptionPanel.setText(null);
        selectOnEnable = !analysisResult.provider2Hints.isEmpty() && !byCategory.isSelected();
    }

    public static synchronized AnalysisResultTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win instanceof AnalysisResultTopComponent) {
            return (AnalysisResultTopComponent) win;
        }
        if (win == null) {
            Logger.getLogger(AnalysisResultTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
        } else {
            Logger.getLogger(AnalysisResultTopComponent.class.getName()).warning(
                    "There seem to be multiple components with the '" + PREFERRED_ID +
                    "' ID. That is a potential source of errors and unexpected behavior.");
        }
        
        AnalysisResultTopComponent result = new AnalysisResultTopComponent();
        Mode outputMode = WindowManager.getDefault().findMode("output");
        
        if (outputMode != null) {
            outputMode.dockInto(result);
        }
        return result;
    }

    private class VariableRightBorder implements Border {

        public VariableRightBorder() {
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color old = g.getColor();
            g.setColor(getColor());
            g.drawLine(x + width - 1, y, x + width - 1, y + height);
            g.setColor(old);
        }

        public Color getColor() {
            if (Utilities.isMac()) {
                Color c1 = UIManager.getColor("controlShadow");
                Color c2 = UIManager.getColor("control");
                return new Color((c1.getRed() + c2.getRed()) / 2,
                        (c1.getGreen() + c2.getGreen()) / 2,
                        (c1.getBlue() + c2.getBlue()) / 2);
            } else {
                return UIManager.getColor("controlShadow");
            }
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 2);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    }
    
    private class PCLImpl implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (name == null || "enabled".equals(name)) {
                previousError.setEnabled(prevAction.isEnabled());
                nextError.setEnabled(nextAction.isEnabled());

                if (selectOnEnable && evt.getSource() == nextAction && nextAction.isEnabled()) {
                    selectOnEnable = false;
                    nextAction.actionPerformed(null);
                }

                selectOnEnable = false;
            }
        }

    }
}
