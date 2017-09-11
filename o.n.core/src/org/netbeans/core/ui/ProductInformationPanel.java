/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.ui;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.core.actions.HTMLViewAction;
import static org.netbeans.core.ui.Bundle.*;
import org.openide.awt.CheckForUpdatesProvider;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

public class ProductInformationPanel extends JPanel implements HyperlinkListener {

    URL url = null;
    Icon about;
    
    private static final String CHECK_FOR_UPDATES_ACTION = "check-for-updates";

    private static final int FONT_SIZE = getFontSize();
    
    @Messages({
        "# {0} - product version",
        "# {1} - Java version",
        "# {2} - VM version",
        "# {3} - OS",
        "# {4} - encoding",
        "# {5} - locale",
        "# {6} - user dir",
        "# {7} - cache dir",
        "# {8} - updates",
        "# {9} - font size",
        "# {10} - Java runtime",
        "LBL_description=<div style=\"font-size: {9}pt; font-family: Verdana, 'Verdana CE',  Arial, 'Arial CE', 'Lucida Grande CE', lucida, 'Helvetica CE', sans-serif;\">"
            + "<p style=\"margin: 0\"><b>Product Version:</b> {0}</p>\n "
            + "{8}"
            + "<p style=\"margin: 0\"><b>Java:</b> {1}; {2}</p>\n "
            + "<p style=\"margin: 0\"><b>Runtime:</b> {10}</p>\n "
            + "<p style=\"margin: 0\"><b>System:</b> {3}; {4}; {5}</p>\n "
            + "<p style=\"margin: 0\"><b>User directory:</b> {6}</p>\n "
            + "<p style=\"margin: 0\"><b>Cache directory:</b> {7}</p></div>",
        "# {0} - content description",
        "updates_not_found=<p style=\"margin: 0\"><b>Updates:</b> NetBeans IDE is updated to version {0}</p>\n ",
        "# {0} - content description",
        "updates_found=<p style=\"margin: 0\"><b>Updates:</b> <a href=\"" + CHECK_FOR_UPDATES_ACTION + "\">Updates available</a> {0}</p>\n ",
        "check_for_updates=Check for Updates",
        "to_version=to version {0}"
    })
    public ProductInformationPanel() {
        initComponents();
        imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        description.setText(LBL_description(getProductVersionValue(), getJavaValue(), getVMValue(), getOperatingSystemValue(), getEncodingValue(), getSystemLocaleValue(), getUserDirValue(), Places.getCacheDirectory().getAbsolutePath(), "", FONT_SIZE, getJavaRuntime()));
        description.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        description.putClientProperty( JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                final String updates = getUpdates();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        description.setText(LBL_description(getProductVersionValue(), getJavaValue(), getVMValue(), getOperatingSystemValue(), getEncodingValue(), getSystemLocaleValue(), getUserDirValue(), Places.getCacheDirectory().getAbsolutePath(), updates, FONT_SIZE, getJavaRuntime()));
                        description.setCursor(null);
                        description.revalidate();
                    }
                });
            }
        });
        description.setCaretPosition(0); // so that text is not scrolled down
        description.addHyperlinkListener(this);
        copyright.addHyperlinkListener(this);
        copyright.setBackground(getBackground());
        copyright.putClientProperty( JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        about = new ImageIcon(org.netbeans.core.startup.Splash.loadContent(true));
        imageLabel.setIcon(about);

        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                try {
                    url = new URL(NbBundle.getMessage(ProductInformationPanel.class, "URL_ON_IMG")); // NOI18N
                    showUrl();
                } catch (MalformedURLException ex) {
                    //ignore
                }
            }
        });
        
        description.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(HyperlinkEvent.EventType.ENTERED == e.getEventType()) {
                    if (CHECK_FOR_UPDATES_ACTION.equals(e.getDescription())) {
                        description.setToolTipText(check_for_updates());
                    } else if (e.getURL() != null) {
                        description.setToolTipText(e.getURL().toExternalForm());
                    }
                } else if (HyperlinkEvent.EventType.EXITED == e.getEventType()) {
                    description.setToolTipText(null);
                } else if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                    if (CHECK_FOR_UPDATES_ACTION.equals(e.getDescription())) {
                        checkForUpdates();
                    } else {
                        URLDisplayer.getDefault().showURL(e.getURL());
                    }
                }
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        copyright = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        description = new javax.swing.JTextPane();
        imagePanel = new javax.swing.JPanel();
        imageLabel = new javax.swing.JLabel();

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jButton2.setMnemonic(NbBundle.getMessage(ProductInformationPanel.class, "MNE_Close").charAt(0));
        jButton2.setText(NbBundle.getMessage(ProductInformationPanel.class, "LBL_Close")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jButton2, gridBagConstraints);

        jScrollPane3.setBorder(null);

        copyright.setBorder(null);
        copyright.setContentType("text/html");
        copyright.setEditable(false);
        copyright.setText(getCopyrightText());
        copyright.setCaretPosition(0); // so that text is not scrolled down
        copyright.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                copyrightMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(copyright);

        description.setContentType("text/html");
        description.setEditable(false);
        description.setText("<div style=\"font-size: 12pt; font-family: Verdana, 'Verdana CE',  Arial, 'Arial CE', 'Lucida Grande CE', lucida, 'Helvetica CE', sans-serif;\">\n    <b>Product Version:</b> {0}<br> <b>Java:</b> {1}; {2}<br> <b>System:</b> {3}; {4}; {5}<br><b>Userdir:</b> {6}</div>");
        jScrollPane2.setViewportView(description);

        imagePanel.setLayout(new java.awt.BorderLayout());

        imageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imagePanel.add(imageLabel, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(imagePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(imagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void copyrightMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_copyrightMouseClicked
    showUrl();
}//GEN-LAST:event_copyrightMouseClicked

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    closeDialog();    
}//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane copyright;
    private javax.swing.JTextPane description;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JPanel imagePanel;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables
    
    private void closeDialog() {
        Window w = SwingUtilities.getWindowAncestor(this);
        w.setVisible(false);
        w.dispose();
    }

    private void showUrl() {
        if (url != null) {
            org.openide.awt.StatusDisplayer.getDefault().setStatusText(
                NbBundle.getBundle(HTMLViewAction.class).getString("CTL_OpeningBrowser"));
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        }
    }
    
    public static String getProductVersionValue () {
        return MessageFormat.format(
            NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"),
            new Object[] {System.getProperty("netbeans.buildnumber")});
    }

    public static String getOperatingSystemValue () {
        return NbBundle.getMessage(ProductInformationPanel.class, "Format_OperatingSystem_Value",
            System.getProperty("os.name", "unknown"),
            System.getProperty("os.version", "unknown"),
            System.getProperty("os.arch", "unknown"));
    }

    public static String getJavaValue () {
        return System.getProperty("java.version", "unknown");
    }

    public static String getVMValue () {
        return System.getProperty("java.vm.name", "unknown") + " " + System.getProperty("java.vm.version", "");
    }
    
    public static String getJavaRuntime () {
        return System.getProperty("java.runtime.name", "unknown") + " " + System.getProperty("java.runtime.version", "");
    }

    public static String getSystemLocaleValue () {
        String branding;
        return Locale.getDefault().toString() + ((branding = NbBundle.getBranding()) == null ? "" : (" (" + branding + ")")); // NOI18N
    }

    private String getUserDirValue () {
        return System.getProperty("netbeans.user");
    }

    public static String getEncodingValue() {
        return System.getProperty("file.encoding", "unknown");
    }

    public void hyperlinkUpdate(HyperlinkEvent event) {
        if(HyperlinkEvent.EventType.ENTERED == event.getEventType()) {
            url = event.getURL();
        } else if (HyperlinkEvent.EventType.EXITED == event.getEventType()) {
            url = null;
        }
    }
     
    private static String getCopyrightText () {
        
        String copyrighttext = NbBundle.getMessage(ProductInformationPanel.class, "LBL_Copyright", FONT_SIZE); // NOI18N
        
        FileObject licenseFolder = FileUtil.getConfigFile("About/Licenses");   // NOI18N
        if (licenseFolder != null) {
            FileObject[] foArray = licenseFolder.getChildren();
            if (foArray.length > 0) {
                String curLicense;
                boolean isSomeLicense = false;
                StringWriter sw = new StringWriter();
                for (int i = 0; i < foArray.length; i++) {
                    curLicense = loadLicenseText(foArray[i]);
                    if (curLicense != null) {
                        sw.write("<br>" + MessageFormat.format( // NOI18N
                            NbBundle.getBundle(ProductInformationPanel.class).getString("LBL_AddOnCopyright"), // NOI18N
                            new Object[] { curLicense, FONT_SIZE }));
                        isSomeLicense = true;
                    }
                }
                if (isSomeLicense) {
                    copyrighttext += sw.toString();
                }
            }
        }
        
        return copyrighttext;
    }
    
    /** Tries to load text stored in given file object.
     *
     * @param fo File object to retrieve text from
     * @return String containing text from the file, or null if file can't be found
     * or some kind of I/O error appeared.
     */
    private static String loadLicenseText (FileObject fo) {
        
        InputStream is = null;
        try {
            is = fo.getInputStream();
        } catch (FileNotFoundException ex) {
            // license file not found
            return null;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringWriter result = new StringWriter();
        int curChar;
        try {
            // reading content of license file
            while ((curChar = in.read()) != -1) {
                result.write(curChar);
            }
        } catch (IOException ex) {
            // don't return anything if any problem during read
            return null;
        }

        return result.toString();
    }
    
    private static String getUpdates() {
        assert ! EventQueue.isDispatchThread() : "Don't call it from event dispatch thread.";
        CheckForUpdatesProvider checkForUpdatesProvider = Lookup.getDefault().lookup(CheckForUpdatesProvider.class);
        if (checkForUpdatesProvider == null) {
            return ""; // NOI18N
        }
        String desc = checkForUpdatesProvider.getContentDescription();
        desc = desc != null ? desc : ""; // NOI18N
        if (checkForUpdatesProvider.notifyAvailableUpdates(false)) {
            return updates_found(desc.isEmpty() ? desc : to_version(desc));
        } else {
            return desc.isEmpty() ? desc : updates_not_found(desc);
        }
    }
    
    private static void checkForUpdates() {
        assert EventQueue.isDispatchThread() : "Call it from event dispatch thread only.";
        CheckForUpdatesProvider checkForUpdatesProvider = Lookup.getDefault().lookup(CheckForUpdatesProvider.class);
        if (checkForUpdatesProvider != null) {
            checkForUpdatesProvider.openCheckForUpdatesWizard(true);
        }
    }

    private static int getFontSize() {
        Integer customFontSize = (Integer)UIManager.get("customFontSize"); // NOI18N
        if (customFontSize != null) {
            return customFontSize.intValue();
        }
        return 12;
    }
}
