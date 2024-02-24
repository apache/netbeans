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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.JavadocRootsProvider;
import org.netbeans.modules.apisupport.project.universe.JavadocRootsSupport;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.SourceRootsProvider;
import org.netbeans.modules.apisupport.project.universe.SourceRootsSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 * Factory for creating miscellaneous UI components, their models and renderers
 * as they are needed through the code of this module.
 *
 * @author Martin Krauskopf
 */
public final class PlatformComponentFactory {
    
    private static final Color INVALID_PLAF_COLOR = UIManager.getColor("nb.errorForeground"); // NOI18N
    
    /** Set of suites added by the user in <em>this</em> IDE session. */
    private static Set<String> userSuites = new TreeSet<String>(Collator.getInstance());
    
    private PlatformComponentFactory() {
        // don't allow instances
    }
    
    /**
     * Returns <code>JComboBox</code> initialized with {@link
     * NbPlatformListModel} which contains all NetBeans platform.
     */
    public static JComboBox getNbPlatformsComboxBox() {
        JComboBox plafComboBox = new JComboBox(new NbPlatformListModel());
        plafComboBox.setRenderer(new NbPlatformListRenderer());
        return plafComboBox;
    }
    
    /**
     * Returns <code>JList</code> initialized with {@link NbPlatformListModel}
     * which contains all NetBeans platform.
     */
    public static JList getNbPlatformsList() {
        JList plafList = new JList(new NbPlatformListModel());
        plafList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plafList.setCellRenderer(new NbPlatformListRenderer());
        return plafList;
    }
    
    /**
     * Returns <code>JComboBox</code> containing all suites. Also see
     * {@link #addUserSuite}.
     */
    public static JComboBox getSuitesComboBox() {
        MutableComboBoxModel model = new SuiteListModel(userSuites);
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            String suiteDir = SuiteUtils.getSuiteDirectoryPath(projects[i]);
            if (suiteDir != null) {
                model.addElement(suiteDir);
            }
        }
        JComboBox suiteCombo = new JComboBox(model);
        if (model.getSize() > 0) {
            suiteCombo.setSelectedIndex(0);
        }
        return suiteCombo;
    }
    
    /**
     * Adds <code>suiteDir</code> to the list of suites returned by the
     * {@link #getSuitesComboBox} method. Such a suites are remembered
     * <b>only</b> for the current IDE session.
     */
    public static void addUserSuite(String suiteDir) {
        userSuites.add(suiteDir);
    }
    
    public static ListCellRenderer getURLListRenderer() {
        return new URLListRenderer();
    }
    
    /**
     * Render {@link NbPlatform} using its computed display name. If computation
     * fails platform ID is used as a fallback. For <code>null</code> values
     * renders an empty string.
     * <p>Use in conjuction with {@link NbPlatformListModel}</p>
     */
    private static class NbPlatformListRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public NbPlatformListRenderer () {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            NbPlatform plaf = ((NbPlatform) value);
            // NetBeans.org modules doesn't have platform at all --> null
            String text = plaf == null ? "" : plaf.getLabel(); // NOI18N
            setText(text);
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            if (plaf != null && !plaf.isValid()) {
                setForeground(INVALID_PLAF_COLOR);
            }
            
            return this;
        }
        
        // #93658: GTK needs name to render cell renderer "natively"
        public @Override String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    }
    
    /**
     * Returns model containing all <em>currently</em> registered NbPlatforms.
     * See also {@link NbPlatform#getPlatforms}.
     * <p>Use in conjuction with {@link NbPlatformListRenderer}</p>
     */
    public static class NbPlatformListModel extends AbstractListModel
            implements ComboBoxModel {
        
        private static NbPlatform[] getSortedPlatforms(NbPlatform extra) {
            Set<NbPlatform> _platforms = NbPlatform.getPlatforms();
            if (extra != null) {
                _platforms.add(extra);
            }
            NbPlatform[] platforms = _platforms.toArray(new NbPlatform[0]);
            Arrays.sort(platforms, new Comparator<NbPlatform>() {
                public int compare(NbPlatform p1, NbPlatform p2) {
                    int res = Collator.getInstance().compare(p1.getLabel(), p2.getLabel());
                    if (res != 0) {
                        return res;
                    } else {
                        return System.identityHashCode(p1) - System.identityHashCode(p2);
                    }
                }
            });
            return platforms;
        }
        
        private NbPlatform[] nbPlafs;
        private Object selectedPlaf;
        
        public NbPlatformListModel() {
            nbPlafs = getSortedPlatforms(null);
            if (nbPlafs.length > 0) {
                selectedPlaf = nbPlafs[0];
            }
        }
        
        public NbPlatformListModel(NbPlatform initiallySelected) {
            nbPlafs = getSortedPlatforms(initiallySelected);
            selectedPlaf = initiallySelected;
        }
        
        public int getSize() {
            return nbPlafs.length;
        }
        
        public Object getElementAt(int index) {
            return index < nbPlafs.length ? nbPlafs[index] : null;
        }

        public void setSelectedItem(Object plaf) {
            assert plaf == null || plaf instanceof NbPlatform;
            if (selectedPlaf != plaf) {
                selectedPlaf = plaf;
                fireContentsChanged(this, -1, -1);
            }
        }
        
        public Object getSelectedItem() {
            return selectedPlaf;
        }
        
        void removePlatform(NbPlatform plaf) {
            try {
                NbPlatform.removePlatform(plaf);
                nbPlafs = getSortedPlatforms(null); // refresh
                fireContentsChanged(this, 0, nbPlafs.length - 1);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        NbPlatform addPlatform(String id, String destdir, String label) {
            try {
                NbPlatform def = NbPlatform.getDefaultPlatform();
                NbPlatform plaf = def != null ?
                    NbPlatform.addPlatform(id, new File(destdir), /* #71629 */ def.getHarnessLocation(), label) :
                    // Installation somehow corrupted, but try to behave gracefully:
                    NbPlatform.addPlatform(id, new File(destdir), label);
                nbPlafs = getSortedPlatforms(null); // refresh
                fireContentsChanged(this, 0, nbPlafs.length - 1);
                return plaf;
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
            return null;
        }
    }
    
    static class ModuleEntryListModel extends AbstractListModel {
        
        private ModuleEntry[] mes;
        
        ModuleEntryListModel(ModuleEntry[] mes) {
            this.mes = mes;
        }
        
        public int getSize() {
            return mes.length;
        }
        
        public Object getElementAt(int index) {
            return mes[index].getLocalizedName();
        }
    }
    
    private static class SuiteListModel extends AbstractListModel
            implements MutableComboBoxModel {
        
        private Set<String> suites = new TreeSet<String>(Collator.getInstance());
        private String selectedSuite;
        
        SuiteListModel(Set<String> suites) {
            this.suites.addAll(suites);
        }
        
        public void setSelectedItem(Object suite) {
            if (suite == null) {
                return;
            }
            if (selectedSuite != suite) {
                selectedSuite = (String) suite;
                fireContentsChanged(this, -1, -1);
            }
        }
        
        public Object getSelectedItem() {
            return selectedSuite;
        }
        
        public int getSize() {
            return suites.size();
        }
        
        public Object getElementAt(int index) {
            return suites.toArray()[index];
        }
        
        public void addElement(Object obj) {
            suites.add((String) obj);
            fireIntervalAdded(this, 0, suites.size());
        }
        
        /** Shouldn't be needed in the meantime. */
        public void insertElementAt(Object obj, int index) {
            assert false : "Who needs to insertElementAt?"; // NOI18N
        }
        
        /** Shouldn't be needed in the meantime. */
        public void removeElement(Object obj) {
            assert false : "Who needs to removeElement?"; // NOI18N
        }
        
        /** Shouldn't be needed in the meantime. */
        public void removeElementAt(int index) {
            assert false : "Who needs to call removeElementAt?"; // NOI18N
        }
    }
    
    /**
     * <code>ListModel</code> capable to manage NetBeans platform source roots.
     * <p>Can be used in conjuction with {@link URLListRenderer}</p>
     */
    static final class SourceRootsModel extends AbstractListModel {
        
        private SourceRootsProvider srcRP;
        private URL[] srcRoots;
        
        SourceRootsModel(SourceRootsProvider srp) {
            this.srcRP = srp;
            this.srcRoots = srp.getSourceRoots();
        }
        
        public Object getElementAt(int index) {
            return srcRoots[index];
        }
        
        public int getSize() {
            return srcRoots.length;
        }
        
        void removeSourceRoot(URL[] srcRootToRemove) {
            try {
                srcRP.removeSourceRoots(srcRootToRemove);
                this.srcRoots = srcRP.getSourceRoots(); // refresh
                fireContentsChanged(this, 0, srcRootToRemove.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void addSourceRoot(URL srcRootToAdd) {
            try {
                srcRP.addSourceRoot(srcRootToAdd);
                this.srcRoots = srcRP.getSourceRoots(); // refresh
                fireContentsChanged(this, 0, srcRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void moveSourceRootsDown(int[] toMoveDown) {
            try {
                for (int i = 0; i < toMoveDown.length; i++) {
                    srcRP.moveSourceRootDown(toMoveDown[i]);
                }
                this.srcRoots = srcRP.getSourceRoots(); // refresh
                fireContentsChanged(this, 0, srcRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void moveSourceRootsUp(int[] toMoveUp) {
            try {
                for (int i = 0; i < toMoveUp.length; i++) {
                    srcRP.moveSourceRootUp(toMoveUp[i]);
                }
                this.srcRoots = srcRP.getSourceRoots(); // refresh
                fireContentsChanged(this, 0, srcRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        boolean containsRoot(URL srcRootToAdd) {
            return SourceRootsSupport.containsRoot(srcRP, srcRootToAdd);
        }
    }
    
    /**
     * <code>ListModel</code> capable to manage NetBeans platform javadoc roots.
     * <p>Can be used in conjuction with {@link URLListRenderer}</p>
     */
    static final class JavadocRootsModel extends AbstractListModel {

        private static final RequestProcessor RP = new RequestProcessor(JavadocRootsModel.class);
        
        private JavadocRootsProvider jrp;
        private URL[] javadocRoots;
        
        JavadocRootsModel(final JavadocRootsProvider jrp) {
            this.jrp = jrp;
            javadocRoots = new URL[0];
            RP.post(new Runnable() { // #207451: can load core.kit project
                @Override public void run() {
                    final URL[] roots = jrp.getJavadocRoots();
                    EventQueue.invokeLater(new Runnable() {
                        @Override public void run() {
                            javadocRoots = roots;
                            fireContentsChanged(this, 0, javadocRoots.length);
                        }
                    });
                }
            });
        }
        
        public Object getElementAt(int index) {
            return javadocRoots[index];
        }
        
        public int getSize() {
            return javadocRoots.length;
        }
        
        void removeJavadocRoots(URL[] jdRootToRemove) {
            try {
                jrp.removeJavadocRoots(jdRootToRemove);
                this.javadocRoots = jrp.getJavadocRoots(); // refresh
                fireContentsChanged(this, 0, javadocRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void addJavadocRoot(URL jdRootToAdd) {
            try {
                jrp.addJavadocRoot(jdRootToAdd);
                this.javadocRoots = jrp.getJavadocRoots(); // refresh
                fireContentsChanged(this, 0, javadocRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void moveJavadocRootsDown(int[] toMoveDown) {
            try {
                for (int i = 0; i < toMoveDown.length; i++) {
                    jrp.moveJavadocRootDown(toMoveDown[i]);
                }
                this.javadocRoots = jrp.getJavadocRoots(); // refresh
                fireContentsChanged(this, 0, javadocRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }
        
        void moveJavadocRootsUp(int[] toMoveUp) {
            try {
                for (int i = 0; i < toMoveUp.length; i++) {
                    jrp.moveJavadocRootUp(toMoveUp[i]);
                }
                this.javadocRoots = jrp.getJavadocRoots(); // refresh
                fireContentsChanged(this, 0, javadocRoots.length);
            } catch (IOException e) {
                // tell the user that something goes wrong
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
            }
        }

        boolean containsRoot(URL rootToAdd) {
            return JavadocRootsSupport.containsRoot(jrp, rootToAdd);
        }
    }
    
    /**
     * Render {@link java.net.URL} using {@link java.net.URL#getFile}.
     * <p>Use in conjuction with {@link SourceRootsModel} and
     * {@link JavadocRootsModel}</p>
     */
    static final class URLListRenderer extends DefaultListCellRenderer {
        
        public @Override Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            URL u = (URL) value;
            File f = FileUtil.archiveOrDirForURL(u);
            String text = f != null ? f.getAbsolutePath() : u.toString();
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }
    
}
