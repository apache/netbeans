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
package org.netbeans.modules.java.api.common.project.ui.customizer;

import java.awt.Color;
import java.awt.Component;
import java.beans.BeanInfo;
import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Visual classpath customizer support.
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public class ClassPathListCellRenderer extends DefaultListCellRenderer {

    private static final Pattern FOREIGN_PLAIN_FILE_REFERENCE = Pattern.compile("\\$\\{file\\.reference\\.([^${}]+)\\}"); // NOI18N
    private static final Pattern UNKNOWN_FILE_REFERENCE = Pattern.compile("\\$\\{([^${}]+)\\}"); // NOI18N

    private static ImageIcon ICON_FOLDER = null;

    private static ImageIcon ICON_BROKEN_JAR;
    private static ImageIcon ICON_BROKEN_LIBRARY;
    private static ImageIcon ICON_BROKEN_ARTIFACT;

    private PropertyEvaluator evaluator;
    private FileObject projectFolder;

    // Contains well known paths in the WebProject
    private static final Map<String, String> WELL_KNOWN_PATHS_NAMES = new HashMap<String, String>();
    static {
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.JAVAC_MODULEPATH, NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_JavacModulepath_DisplayName" ) );
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.JAVAC_CLASSPATH, NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_JavacClasspath_DisplayName" ) );
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.JAVAC_TEST_MODULEPATH, NbBundle.getMessage( ClassPathListCellRenderer.class,"LBL_JavacTestModulepath_DisplayName") );
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.JAVAC_TEST_CLASSPATH, NbBundle.getMessage( ClassPathListCellRenderer.class,"LBL_JavacTestClasspath_DisplayName") );
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.RUN_MODULEPATH, NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_RunModulepath_DisplayName" ) );
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.RUN_CLASSPATH, NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_RunClasspath_DisplayName" ) );
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.RUN_TEST_MODULEPATH, NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_RunTestModulepath_DisplayName" ) );
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.RUN_TEST_CLASSPATH, NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_RunTestClasspath_DisplayName" ) );
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.BUILD_CLASSES_DIR, NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_BuildClassesDir_DisplayName" ) );
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.BUILD_TEST_CLASSES_DIR, NbBundle.getMessage (ClassPathListCellRenderer.class,"LBL_BuildTestClassesDir_DisplayName") );
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.BUILD_MODULES_DIR, NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_BuildModulesDir_DisplayName" ) );
        WELL_KNOWN_PATHS_NAMES.put( ProjectProperties.BUILD_TEST_MODULES_DIR, NbBundle.getMessage (ClassPathListCellRenderer.class,"LBL_BuildTestModulesDir_DisplayName") );
    };

    private  ClassPathListCellRenderer( PropertyEvaluator evaluator, FileObject projectFolder) {
        super();
        this.evaluator = evaluator;
        this.projectFolder = projectFolder;
    }

    public static ListCellRenderer createClassPathListRenderer(PropertyEvaluator evaluator, FileObject projectFolder) {
        return new ClassPathListCellRenderer(evaluator, projectFolder);
    }

    public static TableCellRenderer createClassPathTableRenderer(PropertyEvaluator evaluator, FileObject projectFolder) {
        return new ClassPathListCellRenderer.ClassPathTableCellRenderer(evaluator, projectFolder);
    }

    public static TreeCellRenderer createClassPathTreeRenderer(PropertyEvaluator evaluator, FileObject projectFolder) {
        return new ClassPathListCellRenderer.ClassPathTreeCellRenderer(evaluator, projectFolder);
    }

    @Override
    public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (" ".equals(value)) { // NOI18N
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        if (value != null) {
            assert value instanceof ClassPathSupport.Item : value.getClass().toString();
        }
        ClassPathSupport.Item item = (ClassPathSupport.Item)value;
        super.getListCellRendererComponent( list, getDisplayName( item ), index, isSelected, cellHasFocus );
        setIcon( getIcon( item ) );
        setToolTipText( getToolTipText( item ) );
        return this;
    }


    private String getDisplayName( ClassPathSupport.Item item ) {

        switch ( item.getType() ) {

            case ClassPathSupport.Item.TYPE_LIBRARY:
                if ( item.isBroken() ) {
                    return NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_MISSING_LIBRARY",
                        Integer.toHexString(getErrorForeground().getRGB() & 0xffffff),
                        toHtml(getLibraryName( item )));
                }
                else {
                    return item.getLibrary().getDisplayName();
                }
            case ClassPathSupport.Item.TYPE_CLASSPATH:
                final String name = WELL_KNOWN_PATHS_NAMES.get(CommonProjectUtils.getAntPropertyName(item.getReference()));
                if ( item.isBroken() ) {
                    return NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_MISSING_FILE",
                        Integer.toHexString(getErrorForeground().getRGB() & 0xffffff),
                        toHtml(name == null ? CommonProjectUtils.getAntPropertyName(item.getReference()) : name));
                } else {
                    return name == null ? CommonProjectUtils.getAntPropertyName(item.getReference()) : name;
                }
                
            case ClassPathSupport.Item.TYPE_ARTIFACT:
                if ( item.isBroken() ) {
                    return NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_MISSING_PROJECT",
                        Integer.toHexString(getErrorForeground().getRGB() & 0xffffff),
                        toHtml(getProjectName( item )));
                } else {
                    Project p = item.getArtifact().getProject();
                    String projectName;
                    ProjectInformation pi = ProjectUtils.getInformation(p);
                    projectName = pi.getDisplayName();
                    return MessageFormat.format(NbBundle.getMessage(ClassPathListCellRenderer.class,"MSG_ProjectArtifactFormat"), new Object[] {
                        projectName,
                                item.getArtifactURI().toString()
                    });
                }
           case ClassPathSupport.Item.TYPE_JAR:
                if ( item.isBroken() ) {
                    return NbBundle.getMessage( ClassPathListCellRenderer.class, "LBL_MISSING_FILE",
                        Integer.toHexString(getErrorForeground().getRGB() & 0xffffff),
                        toHtml(getFileRefName( item )));
                }
                else {
                    if (item.getVariableBasedProperty() != null) {
                        String s = item.getVariableBasedProperty();
                        // convert "${var.XXX}/path" to "XXX/path"
                        return s.substring(6, s.indexOf("}")) + s.substring(s.indexOf("}")+1); // NOI18N
                    } else {
                        return item.getFilePath();
                    }
                }
        }

        return item.getReference(); // XXX
    }

    private static @NonNull Color getErrorForeground() {
        Color result = UIManager.getDefaults().getColor("nb.errorForeground");  //NOI18N
        if (result == null) {
            result = Color.RED;
        }
        return result;
    }

    static Icon getIcon( ClassPathSupport.Item item ) {

        switch ( item.getType() ) {

            case ClassPathSupport.Item.TYPE_LIBRARY:
                if ( item.isBroken() ) {
                    if ( ICON_BROKEN_LIBRARY == null ) {
                        ICON_BROKEN_LIBRARY = new ImageIcon( ImageUtilities.mergeImages( ProjectProperties.ICON_LIBRARY.getImage(), ProjectProperties.ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
                    }
                    return ICON_BROKEN_LIBRARY;
                }
                else {
                    return ProjectProperties.ICON_LIBRARY;
                }
            case ClassPathSupport.Item.TYPE_ARTIFACT:
                if ( item.isBroken() ) {
                    if ( ICON_BROKEN_ARTIFACT == null ) {
                        ICON_BROKEN_ARTIFACT = new ImageIcon( ImageUtilities.mergeImages( ProjectProperties.ICON_ARTIFACT.getImage(), ProjectProperties.ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
                    }
                    return ICON_BROKEN_ARTIFACT;
                }
                else {
                    Project p = item.getArtifact().getProject();
                    if (p != null) {
                        ProjectInformation pi = ProjectUtils.getInformation(p);
                        return pi.getIcon();
                    }
                    return ProjectProperties.ICON_ARTIFACT;
                }
            case ClassPathSupport.Item.TYPE_JAR:
                if ( item.isBroken() ) {
                    if ( ICON_BROKEN_JAR == null ) {
                        ICON_BROKEN_JAR = new ImageIcon( ImageUtilities.mergeImages( ProjectProperties.ICON_JAR.getImage(), ProjectProperties.ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
                    }
                    return ICON_BROKEN_JAR;
                }
                else {
                    File file = item.getResolvedFile();
                    ImageIcon icn = file.isDirectory() ? getFolderIcon() : ProjectProperties.ICON_JAR;
                    if (item.getSourceFilePath() != null) {
                        icn =  new ImageIcon( ImageUtilities.mergeImages( icn.getImage(), ProjectProperties.ICON_SOURCE_BADGE.getImage(), 8, 8 ));
                    }
                    if (item.getJavadocFilePath() != null) {
                        icn =  new ImageIcon( ImageUtilities.mergeImages( icn.getImage(), ProjectProperties.ICON_JAVADOC_BADGE.getImage(), 8, 0 ));
                    }
                    return icn;
                }
            case ClassPathSupport.Item.TYPE_CLASSPATH:
                return ProjectProperties.ICON_CLASSPATH;

        }

        return null; // XXX
    }

    private String getToolTipText( ClassPathSupport.Item item ) {
        if ( item.isBroken() &&
             ( item.getType() == ClassPathSupport.Item.TYPE_JAR ||
               item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT )  ) {
            return evaluator.evaluate( item.getReference() );
        }
        switch ( item.getType() ) {
            case ClassPathSupport.Item.TYPE_JAR:
                File f = item.getResolvedFile();
                // if not absolute path:
                if (!f.getPath().equals(item.getFilePath()) || item.getVariableBasedProperty() != null) {
                    return f.getPath();
                }
                break;
            case ClassPathSupport.Item.TYPE_ARTIFACT:                
                final AntArtifact artifact = item.getArtifact();
                if (artifact != null) {
                    final FileObject projDir = artifact.getProject().getProjectDirectory();
                    if (projDir != null) {
                        return FileUtil.getFileDisplayName(projDir);
                    }
                }               
        }

        return null;
    }

    private static ImageIcon getFolderIcon() {

        if ( ICON_FOLDER == null ) {
            DataFolder dataFolder = DataFolder.findFolder( FileUtil.getConfigRoot() );
            ICON_FOLDER = new ImageIcon( dataFolder.getNodeDelegate().getIcon( BeanInfo.ICON_COLOR_16x16 ) );
        }

        return ICON_FOLDER;
    }

    private String getProjectName( ClassPathSupport.Item item ) {
        String ID = item.getReference();
        // something in the form of "${reference.project-name.id}"
        return ID.substring(12, ID.indexOf('.', 12)); // NOI18N
    }

    private String getLibraryName( ClassPathSupport.Item item ) {
        String ID = item.getReference();
        if (ID == null) {
            if (item.getLibrary() != null) {
                return item.getLibrary().getName();
            }
            //TODO HUH? happens when adding new library, then changing
            // the library location to something that doesn't have a reference yet.
            // why are there items without reference upfront?
            return "XXX";
        }
        // something in the form of "${libs.junit.classpath}"
        return ID.substring(7, ID.indexOf(".classpath")); // NOI18N
    }

    private String getFileRefName( ClassPathSupport.Item item ) {
        String ID = item.getReference();
        // something in the form of "${file.reference.smth.jar}"
        Matcher m = FOREIGN_PLAIN_FILE_REFERENCE.matcher(ID);
        if (m.matches()) {
            return m.group(1);
        }
        m = UNKNOWN_FILE_REFERENCE.matcher(ID);
        if (m.matches()) {
            return m.group(1);
        }
        return ID;
    }

    private static String toHtml(@NonNull final String text) {
        final StringBuilder sb = new StringBuilder();
        for (int i=0; i<text.length(); i++) {
            final char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                sb.append("&nbsp;"); //NOI18N
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static class ClassPathTableCellRenderer extends DefaultTableCellRenderer {
        
        private ClassPathListCellRenderer renderer;
        
        ClassPathTableCellRenderer(PropertyEvaluator evaluator, FileObject projectFolder) {
            renderer = new ClassPathListCellRenderer(evaluator, projectFolder);
        }
        
        @Override
        public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
            if (value != null) {
                assert value instanceof ClassPathSupport.Item : value.getClass().toString();
                ClassPathSupport.Item item = (ClassPathSupport.Item)value;
                setIcon( ClassPathListCellRenderer.getIcon( item ) );
                setToolTipText( renderer.getToolTipText( item ) );
                return super.getTableCellRendererComponent(table, renderer.getDisplayName( item ), isSelected, false, row, column);
            } else {
                setIcon( null );
                return super.getTableCellRendererComponent( table, null, isSelected, false, row, column );
            }
        }        
    }
        
    static class ClassPathTreeCellRenderer extends DefaultTreeCellRenderer {
        
        private ClassPathListCellRenderer renderer;
        
        ClassPathTreeCellRenderer(PropertyEvaluator evaluator, FileObject projectFolder) {
            renderer = new ClassPathListCellRenderer(evaluator, projectFolder);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (value instanceof DefaultMutableTreeNode) {
                Object obj = ((DefaultMutableTreeNode) value).getUserObject();
                if (obj instanceof ClassPathSupport.Item) {
                    ClassPathSupport.Item item = (ClassPathSupport.Item)obj;
                    super.getTreeCellRendererComponent(tree, renderer.getDisplayName(item), sel, expanded, leaf, row, false);
                    setIcon(ClassPathListCellRenderer.getIcon(item));
                    setToolTipText(renderer.getToolTipText(item));
                    return this;
                }
            }
            super.getTreeCellRendererComponent(tree, null, sel, expanded, leaf, row, false);
            setIcon(null);
            return this;
        }        
    }        
}
