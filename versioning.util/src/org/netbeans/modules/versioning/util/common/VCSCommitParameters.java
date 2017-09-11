/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.versioning.util.common;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.TemplateSelector;
import org.netbeans.modules.versioning.util.UndoRedoSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VerticallyNonResizingPanel;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;

/**
 *
 * @author Tomas Stupka
 */
public abstract class VCSCommitParameters {

    private static final String PARAMETERS_CHANGED_PROPERTY  = "VCSCommitParameters.changed";   // NOI18N
    
    private static final String RECENT_COMMIT_MESSAGES  = "recentCommitMessage";                // NOI18N
    private static final String LAST_COMMIT_MESSAGE     = "lastCommitMessage";                  // NOI18N
            
    private JLabel recentLink;
    private JLabel templateLink;
    private Preferences preferences;

    private ChangeSupport changeSupport = new ChangeSupport(this);
    
    public VCSCommitParameters(Preferences preferences) {
        this.preferences = preferences;
    }        
    
    public abstract JPanel getPanel();
    public abstract boolean isCommitable();
    public abstract String getErrorMessage();

    protected Preferences getPreferences() {
        return preferences;
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    protected void fireChange() {
        changeSupport.fireChange();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public static JLabel createRecentMessagesLink(final JTextArea text, final Preferences preferences) {
        final JLabel recentLink = new JLabel();
        recentLink.setIcon(new ImageIcon(VCSCommitParameters.class.getResource("/org/netbeans/modules/versioning/util/resources/recent_messages.png"))); // NOI18N
        recentLink.setToolTipText(getMessage("CTL_CommitForm_RecentMessages")); // NOI18N            

        recentLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final AbstractAction openRecentAction = new AbstractAction() {
            @Override
            public void actionPerformed (ActionEvent e) {
                onBrowseRecentMessages(text, preferences);
            }
        };
        recentLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRecentAction.actionPerformed(new ActionEvent(recentLink, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        recentLink.putClientProperty("openAction", openRecentAction);
        return recentLink;
    }
    
    protected JLabel getRecentMessagesLink(final JTextArea text) {
        if(recentLink == null) {
            recentLink = createRecentMessagesLink(text, preferences);
        }
        return recentLink;
    }
    
    protected static JLabel createMessagesTemplateLink(final JTextArea text, final Preferences preferences, final String helpCtxId) {
        final JLabel templateLink = new JLabel();
        templateLink.setIcon(new ImageIcon(VCSCommitParameters.class.getResource("/org/netbeans/modules/versioning/util/resources/load_template.png"))); // NOI18N
        templateLink.setToolTipText(getMessage("CTL_CommitForm_LoadTemplate")); // NOI18N            

        templateLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final AbstractAction openTemplatesAction = new AbstractAction() {
            @Override
            public void actionPerformed (ActionEvent e) {
                onTemplate(text, preferences, helpCtxId);
            }
        };
        templateLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openTemplatesAction.actionPerformed(new ActionEvent(templateLink, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        templateLink.putClientProperty("openAction", openTemplatesAction);
        return templateLink;
    }
    
    protected JLabel getMessagesTemplateLink(final JTextArea text, String helpCtxId) {
        if(templateLink == null) {
            templateLink = createMessagesTemplateLink(text, preferences, helpCtxId);
        }
        return templateLink;
    }
    
    private static String getMessage(String msgKey) {
        return NbBundle.getMessage(VCSCommitParameters.class, msgKey);
    } 
    
    private static void onBrowseRecentMessages(JTextArea text, Preferences preferences) {
        StringSelector.RecentMessageSelector selector = new StringSelector.RecentMessageSelector(preferences);    
        String message = selector.getRecentMessage(getMessage("CTL_CommitForm_RecentTitle"),  // NOI18N
                                               getMessage("CTL_CommitForm_RecentPrompt"),  // NOI18N
            getRecentCommitMessages(preferences));
        if (message != null) {
            text.replaceSelection(message);
        }
    }

    private static void onTemplate(JTextArea text, Preferences preferences, String helpCtxId) {
        TemplateSelector ts = new TemplateSelector(preferences);
        if(ts.show(helpCtxId)) {
            text.setText(ts.getTemplate());
        }
    }    
    
    protected String getLastCanceledCommitMessage() {
        return preferences.get(LAST_COMMIT_MESSAGE, "");
    }
        
    protected static List<String> getRecentCommitMessages(Preferences preferences) {
        return Utils.getStringList(preferences, RECENT_COMMIT_MESSAGES);
    }  
    
    protected Component makeVerticalStrut(JComponent compA,
                                        JComponent compB,
                                        ComponentPlacement relatedUnrelated, 
                                        JPanel parent) {
        return VCSCommitPanel.makeVerticalStrut(compA, compB, relatedUnrelated, parent);
    }

    protected static Component makeHorizontalStrut(JComponent compA,
                                              JComponent compB,
                                              ComponentPlacement relatedUnrelated,
                                              JPanel parent) {
        return VCSCommitPanel.makeHorizontalStrut(compA, compB, relatedUnrelated, parent);
    }
    
    public static class DefaultCommitParameters extends VCSCommitParameters {
        private JPanel panel;
        private String commitMessage;

        public DefaultCommitParameters(Preferences preferences, String commitMessage) {
            super(preferences);
            this.commitMessage = commitMessage;
        }
        
        public DefaultCommitParameters(Preferences preferences) {
            super(preferences);
        }

        @Override
        public JPanel getPanel() {
            if(panel == null) {
                panel = createPanel();                
            }
            return panel;
        }

        protected JPanel createPanel() {
            return new ParametersPanel();
        }
        
        public String getCommitMessage() {
            return ((ParametersPanel) getPanel()).messageTextArea.getText();
        }

        @Override
        public boolean isCommitable() {
            return true;
        }

        @Override
        public String getErrorMessage() {
            return "";                                                          // NOI18N
        }
        
        public void storeCommitMessage() {
            Utils.insert(getPreferences(), RECENT_COMMIT_MESSAGES, getCommitMessage().trim(), 20);
        }

        private class ParametersPanel extends JPanel {
            private JScrollPane scrollpane = new JScrollPane();
            private final JLabel messageLabel = new JLabel();        
            private final JTextArea messageTextArea = new JTextArea();
            private UndoRedoSupport um;                

            public ParametersPanel() {
                messageLabel.setLabelFor(messageTextArea);
                Mnemonics.setLocalizedText(messageLabel, getMessage("CTL_CommitForm_Message")); // NOI18N

                JLabel templateLink = getMessagesTemplateLink(messageTextArea, "org.netbeans.modules.versioning.util.common.TemplatePanel"); //NOI18N
                JLabel recentLink = getRecentMessagesLink(messageTextArea);

                messageTextArea.setColumns(60);    //this determines the preferred width of the whole dialog
                messageTextArea.setLineWrap(true);
                messageTextArea.setRows(4);
                messageTextArea.setTabSize(4);
                messageTextArea.setWrapStyleWord(true);
                messageTextArea.setMinimumSize(new Dimension(100, 18));
                scrollpane.setViewportView(messageTextArea);

                messageTextArea.getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitForm_Message")); // NOI18N
                messageTextArea.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitForm_Message")); // NOI18N
                if(commitMessage != null) {
                    messageTextArea.setText(commitMessage);
                }
                
                JPanel topPanel = new VerticallyNonResizingPanel();
                topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
                topPanel.add(messageLabel);
                topPanel.add(Box.createHorizontalGlue());
                topPanel.add(recentLink);
                topPanel.add(makeHorizontalStrut(recentLink, templateLink, RELATED, this));
                topPanel.add(templateLink);            
                messageLabel.setAlignmentX(LEFT_ALIGNMENT);
                messageLabel.setAlignmentY(BOTTOM_ALIGNMENT); 
                recentLink.setAlignmentY(BOTTOM_ALIGNMENT);
                templateLink.setAlignmentY(BOTTOM_ALIGNMENT);        

                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                topPanel.setAlignmentY(BOTTOM_ALIGNMENT);        
                add(topPanel);
                add(makeVerticalStrut(messageLabel, scrollpane, RELATED, this));            
                add(scrollpane);
                
                Spellchecker.register (messageTextArea);    
            }

            @Override
            public void addNotify() {
                super.addNotify();

                // XXX why in notify?
                TemplateSelector ts = new TemplateSelector(getPreferences());
                if (ts.isAutofill()) {
                    messageTextArea.setText(ts.getTemplate());
                } else {
                    String lastCommitMessage = getLastCanceledCommitMessage();
                    if (lastCommitMessage.isEmpty() && new StringSelector.RecentMessageSelector(getPreferences()).isAutoFill()) {
                        List<String> messages = getRecentCommitMessages(getPreferences());
                        if (messages.size() > 0) {
                            lastCommitMessage = messages.get(0);
                        }
                    }
                    messageTextArea.setText(lastCommitMessage);
                }
                messageTextArea.selectAll();
                um = UndoRedoSupport.register(messageTextArea);          
            }

            @Override
            public void removeNotify() {
                super.removeNotify();
                if (um != null) {
                    um.unregister();
                    um = null;
                }            
            }

            private String getMessage(String msgKey) {
                return NbBundle.getMessage(ParametersPanel.class, msgKey);
            }                     
        }

    }

}
