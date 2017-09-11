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

import java.util.Map;
import org.openide.util.NbBundle;
import java.util.Collection;
import java.util.Collections;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.netbeans.modules.versioning.hooks.VCSHook;
import org.netbeans.modules.versioning.hooks.VCSHookContext;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.border.Border;
import org.openide.awt.Mnemonics;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.BorderFactory.createEmptyBorder;

/**
 *
 * @author Tomas Stupka
 */
abstract class CollapsiblePanel extends JPanel {
    protected final SectionButton sectionButton;
    protected final JPanel sectionPanel;
    protected final VCSCommitPanel master;

    public CollapsiblePanel(VCSCommitPanel master, String sectionButtonText, boolean defaultSectionDisplayed) {
        this.master = master;
        ActionListener al = new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               if (sectionPanel.isVisible()) {
                   hideSection();
               } else {
                   displaySection();
               }
           }
        };

        this.sectionButton = new SectionButton(al);
        this.sectionPanel = new JPanel();

        this.sectionButton.setSelected(defaultSectionDisplayed);

        setLayout(new BoxLayout(this, Y_AXIS));
        Mnemonics.setLocalizedText(sectionButton, sectionButtonText);
        sectionButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, sectionButton.getMaximumSize().height));
        add(sectionButton);
        add(master.makeVerticalStrut(sectionButton, sectionPanel, RELATED, master));
        add(sectionPanel);  

        setAlignmentX(LEFT_ALIGNMENT);
        sectionPanel.setLayout(new BoxLayout(sectionPanel, Y_AXIS));
        sectionPanel.setAlignmentX(LEFT_ALIGNMENT);            
        sectionButton.setAlignmentX(LEFT_ALIGNMENT);

        Icon i = sectionButton.getIcon();
        Border b = sectionButton.getBorder();
        int left = (b != null ? b.getBorderInsets(sectionButton).left : 0) + (i != null ? i.getIconWidth() : 16) + sectionButton.getIconTextGap();
        int bottom = master.getContainerGap(SOUTH);
        sectionPanel.setBorder(createEmptyBorder(0,     // top
                                left,                   // left
                                bottom,                 // bottom
                                0));                    // right

        if(defaultSectionDisplayed) {
            displaySection();
        } else {
            hideSection();
        }
    }

    protected final void displaySection() {
        sectionPanel.setVisible(true);
        master.enlargeVerticallyAsNecessary();
    }

    protected final void hideSection() {
        sectionPanel.setVisible(false);            
    }        
    
    private static String getMessage(String msgKey) {
        return NbBundle.getMessage(VCSCommitPanel.class, msgKey);
    }
    
    static class FilesPanel extends CollapsiblePanel implements ActionListener {
        public static final String TOOLBAR_FILTER = "toolbar.filter";           // NOI18N
        final JLabel filesLabel = new JLabel();        
        private static final boolean DEFAULT_DISPLAY_FILES = true;
        private final JToolBar toolbar;
        private final Map<String, VCSCommitFilter> filters;
        public FilesPanel(VCSCommitPanel master, Map<String, VCSCommitFilter> filters, int preferedHeight)  {
            super(master, master.getModifier().getMessage(VCSCommitPanelModifier.BundleMessage.FILE_PANEL_TITLE), DEFAULT_DISPLAY_FILES);
            this.filters = filters;
            
            master.getCommitTable().labelFor(filesLabel);
            
            JComponent table = master.getCommitTable().getComponent();
            
            Mnemonics.setLocalizedText(filesLabel, getMessage("CTL_CommitForm_FilesToCommit"));         // NOI18N
            filesLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, filesLabel.getMaximumSize().height));
            
            table.setPreferredSize(new Dimension(0, preferedHeight));
            
            ButtonGroup bg = new ButtonGroup();
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            
            for (VCSCommitFilter filter : filters.values()) {
                
                JToggleButton tgb = new JToggleButton();
                tgb.setIcon(filter.getIcon()); 
                tgb.setToolTipText(filter.getTooltip()); 
                tgb.setFocusable(false);
                tgb.setSelected(filter.isSelected());
                tgb.addActionListener(this);                
                tgb.putClientProperty(TOOLBAR_FILTER, filter);
                bg.add(tgb);
                toolbar.add(tgb);
                
            }
            toolbar.setAlignmentX(LEFT_ALIGNMENT);        
            
            sectionPanel.add(toolbar);
            sectionPanel.add(table);
            sectionPanel.add(VCSCommitPanel.makeVerticalStrut(filesLabel, table, RELATED, sectionPanel));
            sectionPanel.add(filesLabel);
            
            sectionPanel.setAlignmentX(LEFT_ALIGNMENT);
            filesLabel.setAlignmentX(LEFT_ALIGNMENT);
            table.setAlignmentX(LEFT_ALIGNMENT);
        }        

        @Override
        public void actionPerformed(ActionEvent e) {
            Object s = e.getSource();            
            if(s instanceof JToggleButton) {
                JToggleButton tgb = (JToggleButton)s;
                Object o = tgb.getClientProperty(TOOLBAR_FILTER);
                assert o != null;
                if(o != null) {
                    VCSCommitFilter f = (VCSCommitFilter) o;
                    boolean selected = tgb.isSelected();
                    if(selected != f.isSelected()) {
                        f.setSelected(selected);
                        String id = f.getID();                        
                        for (VCSCommitFilter filter : filters.values()) {
                            if(!filter.getID().equals(id)) {
                                filter.setSelected(!selected);
                            }
                        }
                        master.computeNodes();
                    } else {
                        return;
                    }
                } 
            }
        }
    }
    
    static class HookPanel extends CollapsiblePanel {
            
        private Collection<? extends VCSHook> hooks = Collections.emptyList();
        private VCSHookContext hookContext;    
        private static final boolean DEFAULT_DISPLAY_HOOKS = false;
        
        public HookPanel(VCSCommitPanel master, Collection<? extends VCSHook> hooks, VCSHookContext hookContext) {            
            super(master, (hooks.size() == 1)
                    ? hooks.iterator().next().getDisplayName()
                    : getMessage("LBL_Advanced"), //NOI18N
                    DEFAULT_DISPLAY_HOOKS);
            this.hooks = hooks;
            this.hookContext = hookContext;
            
            // need this to happen in addNotify() - depends on how 
            // repositoryComboSupport in hook.createComponents works for bugzilla|jira
            if (hooks.size() == 1) {                
                JPanel p = hooks.iterator().next().createComponent(hookContext);
                if (Boolean.TRUE.equals(p.getClientProperty("prop.requestOpened"))) { //NOI18N - some hook panels may want to be opened (hg queue hook with previously configured setts)
                    super.displaySection();
                }
                sectionPanel.add(p);
            } else {
                JTabbedPane hooksTabbedPane = new JTabbedPane();
                for (VCSHook hook : hooks) {
                    hooksTabbedPane.add(hook.createComponent(hookContext), hook.getDisplayName().replaceAll("\\&", ""));
                }
                sectionPanel.add(hooksTabbedPane);
            }
        }
            
        int getPreferedWidth() {
            return sectionPanel.getPreferredSize().width;
        }
    }    
    
}
