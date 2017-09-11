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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.subversion.ui.history;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import org.openide.awt.Mnemonics;
import org.tigris.subversion.svnclientadapter.*;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.NbBundle;
import javax.swing.*;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;

/**
 * Packages search criteria in Search History panel.
 *
 * @author Maros Sandor
 */
class SearchCriteriaPanel extends javax.swing.JPanel {
    
    private final File[] roots;
    private final SVNUrl url;
    
    /** Creates new form SearchCriteriaPanel */
    public SearchCriteriaPanel(File [] roots) {
        this.roots = roots;
        this.url = null;
        initComponents();
    }

    public SearchCriteriaPanel(SVNUrl url) {
        this.url = url;
        this.roots = null;
        initComponents();
    }
    
    public SVNRevision getFrom() {
        String s = tfFrom.getText().trim();
        if(s.length() == 0) {
            return new SVNRevision.Number(1);
        }
        return toRevision(s);
    }

    public SVNRevision getTo() {
        String s = tfTo.getText().trim();
        if(s.length() == 0) {
            return SVNRevision.HEAD;
        }
        return toRevision(s);
    }
    
    private Date parseDate(String s) {
        if (s == null) return null;
        for (int i = 0; i < SearchExecutor.dateFormats.length; i++) {
            DateFormat dateformat = SearchExecutor.dateFormats[i];
            try {
                Date date = dateformat.parse(s);
                if (s.equals(dateformat.format(date))) {
                    return date;
                }
            } catch (ParseException e) {
                // try the next one
            }
        }
        return null;
    }

    private SVNRevision toRevision(String s) {
        Date date = parseDate(s);
        if (date != null) {
            return new SVNRevision.DateSpec(date);
        } else {
            if ("BASE".equals(s)) { // NOI18N
                return SVNRevision.BASE;
            } else if ("HEAD".equals(s)) { // NOI18N
                return SVNRevision.HEAD;
            } else {
                try {
                    return new SVNRevision.Number(Long.parseLong(s));
                } catch (NumberFormatException ex) {
                    // do nothing
                }
            }
        }
        return null;    
    }  
    
    public void setFrom(String from) {
        if (from == null) from = "";  // NOI18N
        tfFrom.setText(from);
    }

    public void setTo(String to) {
        if (to == null) to = "";  // NOI18N
        tfTo.setText(to);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tfFrom.requestFocusInWindow();
            }
        });
    }

    // <editor-fold desc="UI Layout Code" defaultstate="collapsed">
    private void initComponents() {

        JLabel jLabel3      = new JLabel();
        JLabel jLabel4      = new JLabel();
        JLabel jLabel5      = new JLabel();
        JLabel jLabel6      = new JLabel();
        JButton bBrowseFrom = new JButton();
        JButton bBrowseTo   = new JButton();

        jLabel3.setLabelFor(tfFrom);
        jLabel4.setLabelFor(tfTo);

        tfFrom.setColumns(20);
        tfTo.setColumns(20);

        ResourceBundle bundle = ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/history/Bundle"); // NOI18N

        Mnemonics.setLocalizedText(jLabel3, bundle.getString("CTL_UseFrom")); // NOI18N
        jLabel3.setToolTipText(bundle.getString("TT_From")); // NOI18N

        Mnemonics.setLocalizedText(jLabel4, bundle.getString("CTL_UseTo")); // NOI18N
        jLabel4.setToolTipText(bundle.getString("TT_To")); // NOI18N

        Mnemonics.setLocalizedText(jLabel5, bundle.getString("CTL_FromToHint")); // NOI18N

        Mnemonics.setLocalizedText(jLabel6, bundle.getString("CTL_FromToHint")); // NOI18N

        Mnemonics.setLocalizedText(bBrowseFrom, bundle.getString("CTL_BrowseFrom")); // NOI18N
        bBrowseFrom.setToolTipText(bundle.getString("TT_BrowseFrom")); // NOI18N

        Mnemonics.setLocalizedText(bBrowseTo, bundle.getString("CTL_BrowseTo")); // NOI18N
        bBrowseTo.setToolTipText(bundle.getString("TT_BrowseTo")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGap(12)
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(jLabel3)
                                .addComponent(jLabel4))
                        .addPreferredGap(RELATED)
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(tfFrom)
                                        .addPreferredGap(RELATED)
                                        .addComponent(jLabel5))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(tfTo)
                                        .addPreferredGap(RELATED)
                                        .addComponent(jLabel6)))
                        .addPreferredGap(RELATED)
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(bBrowseFrom)
                                .addComponent(bBrowseTo))
                        .addGap(11)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGap(8)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(tfFrom)
                                .addComponent(jLabel5)
                                .addComponent(bBrowseFrom))
                        .addPreferredGap(RELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(jLabel4)
                                .addComponent(tfTo)
                                .addComponent(jLabel6)
                                .addComponent(bBrowseTo))
                        //no gap at the bottom
        );

        bBrowseFrom.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                        onFromBrowse(evt);
                }
        });
        bBrowseTo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                        onToBrowse(evt);
                }
        });
    }// </editor-fold>

    private void onToBrowse(ActionEvent evt) {
        onBrowse(tfTo);
    }

    private void onFromBrowse(ActionEvent evt) {
        onBrowse(tfFrom);
    }

    private void onBrowse(final JTextField destination) {
        final SVNUrl repositoryUrl;
        try {            
            repositoryUrl = url != null ? url : SvnUtils.getRepositoryRootUrl(roots[0]); 
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }                

        String title = destination == tfFrom ? NbBundle.getMessage(SearchCriteriaPanel.class, "CTL_BrowseTag_StartTag") : NbBundle.getMessage(SearchCriteriaPanel.class, "CTL_BrowseTag_EndTag"); // NOI18N
        final Browser browser;
        RepositoryFile repoFile = new RepositoryFile(repositoryUrl, SVNRevision.HEAD);
        int browserMode;
        if(roots[0].isFile()) {
            browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY | Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FOLDERS_SELECTION_ONLY;                        
        } else {
            browserMode = Browser.BROWSER_SHOW_FILES;                        
        }        
        browser = new Browser(title, browserMode, repoFile, null, null, Browser.BROWSER_HELP_ID_SEARCH_HISTORY);        
        final RepositoryFile[] repositoryFiles = browser.getRepositoryFiles();
        if(repositoryFiles == null || repositoryFiles.length == 0) {
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                destination.setText(repositoryFiles[0].getRevision().toString());
            }
        });
        
    }
    
        // Variables declaration
        final JTextField tfFrom = new JTextField();
        final JTextField tfTo = new JTextField();
        // End of variables declaration
    
}
