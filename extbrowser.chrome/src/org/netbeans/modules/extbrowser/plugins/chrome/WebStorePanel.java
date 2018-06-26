/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.extbrowser.plugins.chrome;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import javax.swing.text.html.HTMLDocument;

import org.openide.util.NbBundle;

/**
 *
 * @author ads
 */
class WebStorePanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 6387325958428583652L;
    static final Logger LOGGER = Logger.getLogger(WebStorePanel.class.getName());
    
    static final String EXTENSION_HELP= "chrome.extension";     // NOI18N
    
    WebStorePanel(Runnable runnable) {
        init();
        warningTextLbl.setText( NbBundle.getMessage(WebStorePanel.class, "LBL_UpdateRequired"));
        description.setText(NbBundle.getMessage(WebStorePanel.class, "TXT_WebStoreUpdate"));
        webStoreButton.setText(NbBundle.getMessage(WebStorePanel.class, "LBL_RerunButton"));
        remove(notConnectedLink);
        attachActions(runnable);
    }


    WebStorePanel(boolean rerun, String link, Runnable runnable, final Runnable linkAction) {
        init();
        notConnectedLink.setBackground(getBackground());
        if ( rerun ){
            warningTextLbl.setText( NbBundle.getMessage(WebStorePanel.class, "LBL_Rerun"));
            //remove(description);
            description.setText("");
            description.setPreferredSize(new Dimension(0,0));
            webStoreButton.setText(NbBundle.getMessage(WebStorePanel.class, "LBL_RerunButton"));
            notConnectedLink.setText(NbBundle.getMessage(WebStorePanel.class, "LBL_UnableInstall", link));
            //remove(jScrollPane1);
        }
        else {
          notConnectedLink.setText(NbBundle.getMessage(WebStorePanel.class, "LBL_NotConnected", link));
        }
        notConnectedLink.addHyperlinkListener( new HyperlinkListener() {
            
            @Override
            public void hyperlinkUpdate( HyperlinkEvent event ) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    linkAction.run();
                    /*HelpCtx ctx = new HelpCtx(EXTENSION_HELP);
                    ctx.display();*/
                }
            }
        });
        if ( runnable!= null ){
            attachActions(runnable);
        }
    }
    
    private Dimension getDescriptionSize(){
        return new Dimension(350, (int)getAdjustedHeight());
    }
    
    private void init(){
        initComponents();
        // ui tweaks
        Font labelFont = new JLabel().getFont();
        description.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        description.setFont(labelFont);
        notConnectedLink.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        notConnectedLink.setFont(labelFont);
        
        Font font = description.getFont();
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)description.getDocument()).getStyleSheet().addRule(bodyRule);
        
        
        addHierarchyListener(new HierarchyListener() {
            
            @Override
            public void hierarchyChanged( HierarchyEvent event ) {
                if ((HierarchyEvent.SHOWING_CHANGED & event.getChangeFlags()) !=0 
                        && isShowing()) 
                {
//                    FontMetrics fontMetrics = description.getFontMetrics(
//                            description.getFont());
//                    float lineHeight = fontMetrics.getLineMetrics(description.getText(), 
//                            description.getGraphics()).getHeight();
                    Dimension size = description.getPreferredSize();
                    size.setSize(size.getWidth(), getAdjustedHeight());
                    description.setPreferredSize( size);
                    description.setMaximumSize(size);
                    description.setMinimumSize(size);
                    Window window = SwingUtilities.getWindowAncestor(WebStorePanel.this);
                    window.pack();
                }
            }
        });
    }
    
    private double getAdjustedHeight(){
        JEditorPane fakePane = new JEditorPane();
        fakePane.setEditable(false);
        fakePane.setBorder(null);
        fakePane.setContentType("text/html"); // NOI18N
        fakePane.setFont(description.getFont());
        Dimension size = description.getPreferredSize();
        size.setSize( size.getWidth(), Short.MAX_VALUE);
        fakePane.setSize( size);
        fakePane.setText(description.getText());
        Font font = description.getFont();
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)fakePane.getDocument()).getStyleSheet().addRule(bodyRule);
        return fakePane.getPreferredSize().getHeight();
    }
    
    private int getRows() {
        int count = 0;
        try {
            int offs=description.getCaretPosition();
            while( offs>0) {
                offs=Utilities.getRowStart(description, offs)-1;
                count++;
            }
        } 
        catch (BadLocationException e) {
            assert false;
        }
        return count+1;
    }
    
    private void attachActions(final Runnable runnable){
        webStoreButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed( ActionEvent arg0 ) {
                runnable.run();
            }
        });
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        warningLbl = new javax.swing.JLabel();
        warningTextLbl = new javax.swing.JLabel();
        webStoreButton = new javax.swing.JButton();
        description = new javax.swing.JEditorPane();
        notConnectedLink = new javax.swing.JEditorPane();

        setLayout(new java.awt.GridBagLayout());

        warningLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/extbrowser/chrome/resources/warning.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 15, 20);
        add(warningLbl, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(warningTextLbl, org.openide.util.NbBundle.getMessage(WebStorePanel.class, "LBL_ConnectorExtenstion")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 20);
        add(warningTextLbl, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(webStoreButton, org.openide.util.NbBundle.getMessage(WebStorePanel.class, "LBL_WebStore")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        add(webStoreButton, gridBagConstraints);

        description.setEditable(false);
        description.setBorder(null);
        description.setContentType("text/html"); // NOI18N
        description.setText(org.openide.util.NbBundle.getMessage(WebStorePanel.class, "WebStorePanel.description.text")); // NOI18N
        description.setMinimumSize(new java.awt.Dimension(350, 60));
        description.setOpaque(false);
        description.setPreferredSize(new java.awt.Dimension(350, 100));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 20);
        add(description, gridBagConstraints);

        notConnectedLink.setEditable(false);
        notConnectedLink.setBorder(null);
        notConnectedLink.setContentType("text/html"); // NOI18N
        notConnectedLink.setText(org.openide.util.NbBundle.getMessage(WebStorePanel.class, "LBL_NotConnected")); // NOI18N
        notConnectedLink.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(notConnectedLink, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane description;
    private javax.swing.JEditorPane notConnectedLink;
    private javax.swing.JLabel warningLbl;
    private javax.swing.JLabel warningTextLbl;
    private javax.swing.JButton webStoreButton;
    // End of variables declaration//GEN-END:variables
}
