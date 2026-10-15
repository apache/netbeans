/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.subversion.ui.copy;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.utils.SVNUrlUtils;

/**
 *
 * @author Tomas Stupka
 */
public abstract class CopyDialog {

    private DialogDescriptor dialogDescriptor;
    private JButton okButton, cancelButton;
    private JPanel panel;
    private static final Pattern TEMPLATE_PATTERN_BRANCH = Pattern.compile("^(.*/)*?((?:branches|tags)/(?:.+?))(/.*)+$"); //NOI18N
    private static final Pattern TEMPLATE_PATTERN_TRUNK = Pattern.compile("^(.*/)*?(trunk)(/.*)+$"); //NOI18N
    private static final String BRANCHES_FOLDER = "branches"; //NOI18N
    private static final String TRUNK_FOLDER = "trunk"; //NOI18N
    private static final String BRANCH_TEMPLATE = "[BRANCH_NAME]"; //NOI18N
    private static final String SEP = "----------"; //NOI18N
    private static final String MORE_BRANCHES = NbBundle.getMessage(CopyDialog.class, "LBL_CopyDialog.moreBranchesAndTags"); //NOI18N
    private Set<JComboBox> urlComboBoxes;
    
    protected CopyDialog(JPanel panel, String title, String okLabel) {                
        this.panel = panel;
        
        okButton = new JButton(okLabel);
        okButton.getAccessibleContext().setAccessibleDescription(okLabel);
        cancelButton = new JButton(org.openide.util.NbBundle.getMessage(CopyDialog.class, "CTL_Copy_Cancel"));                                      // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CopyDialog.class, "CTL_Copy_Cancel"));    // NOI18N
       
        dialogDescriptor = new DialogDescriptor(panel, title, true, new Object[] {okButton, cancelButton},
              okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(this.getClass()), null);
        okButton.setEnabled(false);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CopyDialog.class, "CTL_Title"));                // NOI18N
    }

    protected void setupUrlComboBox (RepositoryFile repositoryFile, JComboBox cbo,
            boolean preselectRepositoryFile) {
        if(cbo==null) {
            return;
        }
        List<String> recentFolders = new LinkedList<String>(Utils.getStringList(SvnModuleConfig.getDefault().getPreferences(), CopyDialog.class.getName()));
        Map<String, String> comboItems = setupModel(cbo, repositoryFile, recentFolders, preselectRepositoryFile);
        cbo.setRenderer(new LocationRenderer(comboItems));
        getUrlComboBoxes().add(cbo);
    }    
    
    private Set<JComboBox> getUrlComboBoxes () {
        if(urlComboBoxes == null) {
            urlComboBoxes = new HashSet<JComboBox>();
        }
        return urlComboBoxes;
    }
    
    protected JPanel getPanel() {
        return panel;
    }       
    
    public final boolean showDialog() {                        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CopyDialog.class, "CTL_Title"));                     // NOI18N        
        
        dialog.setVisible(true);
        boolean ret = dialogDescriptor.getValue()==okButton;
        if(ret) {
            storeValidValues();
        }
        return ret;        
    }        
    
    private void storeValidValues() {
        for (JComboBox cbo : urlComboBoxes) {
            Object item = cbo.getEditor().getItem();
            if(item != null && !item.equals("")) { // NOI18N
                Utils.insert(SvnModuleConfig.getDefault().getPreferences(), CopyDialog.class.getName(), (String) item, -1);
            }            
        }                
    }       
    
    protected JButton getOKButton() {
        return okButton;
    }

    /**
     * Model has three categories:
     *   trunk/path (1)
     *   -----
     *   branches and tags (2)
     *   More Branches and Tags
     *   -----
     *   relevant recent urls (3) - those ending with the same file name
     * @param cbo
     * @param repositoryFile
     * @param recentFolders
     * @return 
     */
    static Map<String, String> setupModel (JComboBox cbo, RepositoryFile repositoryFile,
            List<String> recentFolders, boolean preselectCurrentFile) {
        Map<String, String> locations = new HashMap<String, String>(Math.min(recentFolders.size(), 10));
        List<String> model = new LinkedList<String>();
        // all category in the model is sorted by name ignoring case
        Comparator comparator = new Comparator<String>() {
            @Override
            public int compare (String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        };
        // category 3
        TreeMap<String, String> relatedLocations = new TreeMap<String, String>(comparator);
        // category 2
        TreeMap<String, String> branchLocations = new TreeMap<String, String>(comparator);
        String relativePath = SVNUrlUtils.getRelativePath(repositoryFile.getRepositoryUrl(), repositoryFile.getFileUrl());
        String fileName = repositoryFile.getName();
        String[] branchPathSegments = getBranchPathSegments(relativePath);
        String pathInBranch = branchPathSegments[2];
        String preselectedPath = null;
        for (String recentUrl : recentFolders) {
            if (pathInBranch != null && branchLocations.size() < 10) {
                // repository seems to have the recommended branch structure
                String[] pathSegments = getBranchPathSegments(recentUrl);
                if (branchPathSegments[0].equals(pathSegments[0]) && !TRUNK_FOLDER.equals(pathSegments[1])) {
                    // this is a branch or a tag, so get the branch or tag name and build the url relevant for the given file
                    String branchPrefix = pathSegments[0] + pathSegments[1];
                    String loc = branchPrefix + '/' + pathInBranch;
                    // we also emphasize branch/tag name in the renderer, so build the rendered string
                    branchLocations.put(loc, getHtmlVersion(branchPrefix, pathInBranch));
                    if (preselectedPath == null) {
                        preselectedPath = loc;
                    }
                }
            }
            if (recentUrl.endsWith("/" + fileName) && relatedLocations.size() < 10) {
                // this is a relevant (3) url, so add it
                if (pathInBranch == null && preselectedPath == null) {
                    preselectedPath = recentUrl;
                }
                relatedLocations.put(recentUrl, null);
            }
        }
        if (pathInBranch != null) {
            // now let's do some corrections
            // add the single item to cat 1
            String pref = TRUNK_FOLDER;
            if (branchPathSegments[0] != null) {
                pref = branchPathSegments[0] + pref;
            }
            String loc = pref + "/" + pathInBranch;
            locations.put(loc, getHtmlVersion(pref, pathInBranch));
            model.add(loc);
            model.add(SEP);
            // add cat 2 to the model
            model.addAll(branchLocations.keySet());
            locations.putAll(branchLocations);
            model.add(MORE_BRANCHES);
            if (preselectedPath == null) {
                pref = BRANCHES_FOLDER;
                if (branchPathSegments[0] != null) {
                    pref = branchPathSegments[0] + pref;
                }
                preselectedPath = pref + "/" + BRANCH_TEMPLATE + "/" + pathInBranch; //NOI18N
            }
            SelectionListener list = new SelectionListener(repositoryFile, cbo, branchPathSegments[0]);
            cbo.addActionListener(list);
            cbo.addPopupMenuListener(list);
        }
        // do not duplicate entries, so remove all items from (3) that are already in (2)
        relatedLocations.keySet().removeAll(locations.keySet());
        locations.putAll(relatedLocations);
        if (!model.isEmpty() && !relatedLocations.isEmpty()) {
            model.add(SEP);
        }
        model.addAll(relatedLocations.keySet());
        
        ComboBoxModel rootsModel = new DefaultComboBoxModel(model.toArray(new String[0]));
        cbo.setModel(rootsModel);        
        JTextComponent comp = (JTextComponent) cbo.getEditor().getEditorComponent();
        if (preselectCurrentFile) {
            preselectedPath = repositoryFile.getPath();
        }
        if (preselectedPath != null) {
            comp.setText(preselectedPath);
            if (pathInBranch != null) {
                // select the branch name in the offered text - for easy editing
                String[] pathSegments = getBranchPathSegments(preselectedPath);
                String branchPrefix = pathSegments[0] + pathSegments[1];
                int pos = branchPrefix.lastIndexOf('/') + 1;
                if (pos > 0) {
                    comp.setCaretPosition(pos);
                    comp.moveCaretPosition(branchPrefix.length());
                }
            }
        }
        return locations;
    }

    /**
     * Splits a given relative path into segments:
     * <ol>
     * <li>folder prefix where branches/tags/trunk lies in the repository. <code>null</code> for url where trunk/branches/tags are directly in the root folder</li>
     * <li>name of a branch or tag prefixed with the branches or tags prefix, or just simply "trunk" if the url is part of the trunk</li>
     * <li>path to the resource inside trunk/branch/tag</li>
     * </ol>
     * @param relativePath
     * @return 
     */
    private static String[] getBranchPathSegments (String relativePath) {
        String prefix = null;
        String path = null;
        String branchName = null;
        for (Pattern p : new Pattern[] { TEMPLATE_PATTERN_BRANCH, TEMPLATE_PATTERN_TRUNK}) {
            Matcher m = p.matcher(relativePath);
            if (m.matches()) {
                prefix = m.group(1);
                if (prefix == null) {
                    prefix = ""; //NOI18N
                }
                branchName = m.group(2);
                path = m.group(3);
                break;
            }
        }
        if (path != null) {
            path = path.substring(1);
        }
        return new String[] { prefix, branchName, path };
    }
    
    private static String getHtmlVersion (String branchPrefix, String relativePathInBranch) {
        int branchStart = branchPrefix.lastIndexOf('/');
        if (branchStart > 0) {
            return new StringBuilder(2 * (branchPrefix.length() + relativePathInBranch.length())).append("<html>") //NOI18N
                    .append(branchPrefix.substring(0, branchStart + 1)).append("<strong>").append(branchPrefix.substring(branchStart + 1)) //NOI18N
                    .append("</strong>").append('/').append(relativePathInBranch).append("</html>").toString(); //NOI18N
        }
        return null;
    }

    private static class LocationRenderer extends DefaultListCellRenderer {
        private final Map<String, String> empLocations;

        public LocationRenderer (Map<String, String> locations) {
            this.empLocations = locations;
        }
        
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String html;
            if (value instanceof String && (html = empLocations.get(((String) value))) != null) {
                value = html;
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private static class SelectionListener implements ActionListener, PopupMenuListener {
        private final RepositoryFile repositoryFile;
        private final JComboBox combo;
        private boolean popupOn;
        private final String branchesFolderPath;

        public SelectionListener (RepositoryFile repositoryFile, JComboBox combo, String branchesFolderPath) {
            this.repositoryFile = repositoryFile;
            this.combo = combo;
            this.branchesFolderPath = branchesFolderPath;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            if (e.getSource() == combo) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        if (!popupOn && (combo.getSelectedItem() == SEP || combo.getSelectedItem() == MORE_BRANCHES)) {
                            String text = ""; //NOI18N
                            if (combo.getSelectedItem() == MORE_BRANCHES) {
                                BranchPicker picker = new BranchPicker(repositoryFile, branchesFolderPath);
                                if (picker.openDialog()) {
                                    String relativePath = SVNUrlUtils.getRelativePath(repositoryFile.getRepositoryUrl(), repositoryFile.getFileUrl());
                                    text = picker.getSelectedPath() + "/" + getBranchPathSegments(relativePath)[2]; //NOI18N
                                }
                            }
                            ((JTextComponent) combo.getEditor().getEditorComponent()).setText(text); //NOI18N
                        }
                    }
                });
            }
        }

        @Override
        public void popupMenuWillBecomeVisible (PopupMenuEvent e) {
            popupOn = true;
        }

        @Override
        public void popupMenuWillBecomeInvisible (PopupMenuEvent e) {
            popupOn = false;
        }

        @Override
        public void popupMenuCanceled (PopupMenuEvent e) {
            popupOn = true;
        }
    }
}
