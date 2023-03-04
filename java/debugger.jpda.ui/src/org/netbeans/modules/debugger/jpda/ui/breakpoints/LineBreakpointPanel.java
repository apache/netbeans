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

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.spi.debugger.ui.Controller;

import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 * Panel for customizing line breakpoints.
 *
 * @author  Maros Sandor
 */
// <RAVE>
// Implement HelpCtx.Provider interface to provide help ids for help system
// public class LineBreakpointPanel extends JPanel implements Controller {
//
public class LineBreakpointPanel extends JPanel implements ControllerProvider, org.openide.util.HelpCtx.Provider {
// </RAVE>
    
    private static final Logger logger = Logger.getLogger(LineBreakpointPanel.class.getName());

    private static final String         HELP_ID = "NetbeansDebuggerBreakpointLineJPDA"; // NOI18N
    private ConditionsPanel             conditionsPanel;
    private ActionsPanel                actionsPanel; 
    private LineBreakpoint              breakpoint;
    private boolean                     createBreakpoint = false;
    private LBController                controller = new LBController();
    private final DocumentListener      validityDocumentListener = new ValidityDocumentListener();
    private URL                         fileURL;
    
    
    private static LineBreakpoint createBreakpoint () {
        LineBreakpoint lb = LineBreakpoint.create (
            EditorContextDispatcher.getDefault().getMostRecentURLAsString(),
            EditorContextDispatcher.getDefault().getMostRecentLineNumber()
        );
        lb.setPrintText (
            NbBundle.getBundle (LineBreakpointPanel.class).getString 
                ("CTL_Line_Breakpoint_Print_Text")
        );
        return lb;
    }
    
    
    public LineBreakpointPanel () {
        this (createBreakpoint (), true);
    }
    
    /** Creates new form LineBreakpointPanel */
    public LineBreakpointPanel (LineBreakpoint b) {
        this(b, false);
    }

    /** Creates new form LineBreakpointPanel */
    public LineBreakpointPanel (LineBreakpoint b, boolean createBreakpoint) {
        this.createBreakpoint= createBreakpoint;
        breakpoint = b;
        initComponents ();
        if (createBreakpoint) {
            tfFileName.setEditable(true);
        }

        String urlStr = b.getURL();
        logger.fine("LineBreakpointPanel("+urlStr+")");
        try {
            URL url = new URL(urlStr);
            String protocol = url.getProtocol();
            String s;
            if ("file".equalsIgnoreCase(protocol)) {    // NOI18N
                s = url.toURI().getPath();
                if (s.length() == 0) {
                    s = urlStr;
                } else {
                    fileURL = url;
                }
            } else {
                s = urlStr;
            }
            logger.fine("Path/URL = "+s);
            tfFileName.setText(s);
        } catch (Exception e) {
            tfFileName.setText(urlStr);
        }
        tfFileName.setPreferredSize(new Dimension(
            30*tfFileName.getFontMetrics(tfFileName.getFont()).charWidth('W'),
            tfFileName.getPreferredSize().height));

        tfLineNumber.setText(Integer.toString(b.getLineNumber()));
        conditionsPanel = new ConditionsPanel(HELP_ID);
        setupConditionPane();
        conditionsPanel.showClassFilter(false);
        conditionsPanel.setCondition(b.getCondition());
        conditionsPanel.setHitCountFilteringStyle(b.getHitCountFilteringStyle());
        conditionsPanel.setHitCount(b.getHitCountFilter());
        cPanel.add(conditionsPanel, "Center");  // NOI18N
        
        actionsPanel = new ActionsPanel (b);
        pActions.add (actionsPanel, "Center");  // NOI18N

        tfFileName.getDocument().addDocumentListener(validityDocumentListener);
        tfLineNumber.getDocument().addDocumentListener(validityDocumentListener);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controller.checkValid();
            }
        });
    }
    
    private static int findNumLines(FileObject file) {
        DataObject dataObject;
        try {
            dataObject = DataObject.find (file);
        } catch (DataObjectNotFoundException ex) {
            return 0;
        }
        EditorCookie ec = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        if (ec == null) return 0;
        ec.prepareDocument().waitFinished();
        Document d = ec.getDocument();
        if (!(d instanceof StyledDocument)) return 0;
        StyledDocument sd = (StyledDocument) d;
        return NbDocument.findLineNumber(sd, sd.getLength());
    }
    
    private void setupConditionPane() {
        conditionsPanel.setupConditionPaneContext(breakpoint.getURL(), breakpoint.getLineNumber());
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct helpID
    //
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerBreakpointLineJPDA"); // NOI18N
    }
    // </RAVE>
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pSettings = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        tfFileName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        tfLineNumber = new javax.swing.JTextField();
        cPanel = new javax.swing.JPanel();
        pActions = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle"); // NOI18N
        pSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("L_Line_Breakpoint_BorderTitle"))); // NOI18N
        pSettings.setLayout(new java.awt.GridBagLayout());

        jLabel3.setLabelFor(tfFileName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("L_Line_Breakpoint_File_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Line_Breakpoint_File_Name")); // NOI18N

        tfFileName.setEditable(false);
        tfFileName.setToolTipText(bundle.getString("TTT_TF_Line_Breakpoint_File_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(tfFileName, gridBagConstraints);
        tfFileName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Line_Breakpoint_File_Name")); // NOI18N

        jLabel1.setLabelFor(tfLineNumber);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("L_Line_Breakpoint_Line_Number")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Line_Breakpoint_Line_Number")); // NOI18N

        tfLineNumber.setToolTipText(bundle.getString("TTT_TF_Line_Breakpoint_Line_Number")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(tfLineNumber, gridBagConstraints);
        tfLineNumber.getAccessibleContext().setAccessibleName("Line number");
        tfLineNumber.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Line_Breakpoint_Line_Number")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pSettings, gridBagConstraints);

        cPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(cPanel, gridBagConstraints);

        pActions.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pActions, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LineBreakpointPanel.class, "ACSN_LineBreakpoint")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    
    // Controller implementation ...............................................
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pActions;
    private javax.swing.JPanel pSettings;
    private javax.swing.JTextField tfFileName;
    private javax.swing.JTextField tfLineNumber;
    // End of variables declaration//GEN-END:variables

    public Controller getController() {
        return controller;
    }

    private class LBController implements Controller {

        private boolean valid;
        private String errMsg = null;

        /**
         * Called when "Ok" button is pressed.
         *
         * @return whether customizer can be closed
         */
        public boolean ok () {
            if (!valid) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errMsg));
                return false;
            }
            actionsPanel.ok ();
            String path = tfFileName.getText().trim();
            logger.fine("O.K.: path = '"+path+"'");
            URL url = getURL(path);
            logger.fine("      => URL = '"+url+"'");
            breakpoint.setURL((url != null) ? url.toString() : path);
            breakpoint.setLineNumber(Integer.parseInt(tfLineNumber.getText().trim()));
            breakpoint.setCondition (conditionsPanel.getCondition());
            breakpoint.setHitCountFilter(conditionsPanel.getHitCount(),
                    conditionsPanel.getHitCountFilteringStyle());

            if (createBreakpoint)
                DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
            return true;
        }

        /**
         * Called when "Cancel" button is pressed.
         *
         * @return whether customizer can be closed
         */
        public boolean cancel () {
            return true;
        }

        private void checkValid() {
            String path = tfFileName.getText().trim();
            logger.fine("checkValid: path = '"+path+"'");
            if (path.length() == 0) {
                setErrorMessage(NbBundle.getMessage(LineBreakpointPanel.class, "MSG_No_File_Spec"));
                setValid(false);
                return ;
            }
            URL url = getURL(path);
            logger.fine("  url = '"+url+"'");
            FileObject fo = null;
            if (url != null) {
                fo = URLMapper.findFileObject (url);
            }
            logger.fine("  => FileObject = '"+fo+"'");
            if (fo == null) {
                setErrorMessage(NbBundle.getMessage(LineBreakpointPanel.class, "MSG_NonExistent_File_Spec"));
                setValid(false);
                return ;
            } else if (!"text/x-java".equals(fo.getMIMEType())) {
                setErrorMessage(NbBundle.getMessage(LineBreakpointPanel.class, "MSG_NonJava_File_Spec"));
                setValid(false);
                return ;
            }
            int line;
            try {
                line = Integer.parseInt(tfLineNumber.getText().trim());
            } catch (NumberFormatException e) {
                setErrorMessage(NbBundle.getMessage(LineBreakpointPanel.class, "MSG_No_Line_Number_Spec"));
                setValid(false);
                return ;
            }
            if (line <= 0) {
                setErrorMessage(NbBundle.getMessage(LineBreakpointPanel.class, "MSG_NonPositive_Line_Number_Spec"));
                setValid(false);
                return ;
            }
            int maxLine = findNumLines(fo);
            logger.fine("  => maxLine = '"+maxLine+"'");
            if (maxLine == 0) { // Not found
                maxLine = Integer.MAX_VALUE - 1; // Not to bother the user when we did not find it
            }
            if (line > maxLine + 1) {
                setErrorMessage(NbBundle.getMessage(LineBreakpointPanel.class, "MSG_TooBig_Line_Number_Spec",
                                Integer.toString(line), Integer.toString(maxLine + 1)));
                setValid(false);
                return ;
            }
            setErrorMessage(null);
            setValid(true);
        }

        private URL getURL(String path) {
            URL url = null;
            if (fileURL != null) {
                String q = fileURL.getQuery();
                try {
                    URI uri = new URI(fileURL.getProtocol(), null, fileURL.getHost(), fileURL.getPort(), path, q, null);
                    try {
                        url = uri.toURL();
                    } catch (MalformedURLException ex) {
                        logger.log(Level.INFO, "Malformed url protocol '"+fileURL.getProtocol()+"', from "+fileURL+", path = '"+path+"', uri = "+uri, ex);
                    }
                } catch (URISyntaxException ex) {
                    logger.log(Level.INFO, "Malformed URI: scheme '"+fileURL.getProtocol()+"', from "+fileURL+", path = '"+path+"'", ex);
                }
            }
            if (url == null) {
                try {
                    url = new URL(path);
                } catch (MalformedURLException ex) {
                    logger.log(Level.INFO, "Malformed url '"+path+"'", ex);
                }
            }
            return url;
        }

        private void setValid(boolean valid) {
            this.valid = valid;
            firePropertyChange(PROP_VALID, !valid, valid);
        }

        public boolean isValid() {
            return valid;
        }

        private void setErrorMessage(String msg) {
            errMsg = msg;
            firePropertyChange(NotifyDescriptor.PROP_ERROR_NOTIFICATION, null, msg);
        }

        private void setInformationMessage(String msg) {
            firePropertyChange(NotifyDescriptor.PROP_INFO_NOTIFICATION, null, msg);
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            LineBreakpointPanel.this.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            LineBreakpointPanel.this.removePropertyChangeListener(l);
        }
        
    }

    private class ValidityDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            controller.checkValid();
        }

        public void removeUpdate(DocumentEvent e) {
            controller.checkValid();
        }

        public void changedUpdate(DocumentEvent e) {
            controller.checkValid();
        }
        
    }
    
}
