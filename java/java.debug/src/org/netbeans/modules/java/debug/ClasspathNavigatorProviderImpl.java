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
package org.netbeans.modules.java.debug;

import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.java.source.classpath.CacheClassPath;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import static org.openide.util.NbBundle.getMessage;

/**
 *
 * @author Max Sauer
 * @author Jan Lahoda
 */
public class ClasspathNavigatorProviderImpl implements NavigatorPanel {

    private JComponent panel;
    private final ExplorerManager manager = new ExplorerManager();
    
    /**
     * Default constructor for layer instance.
     */
    public ClasspathNavigatorProviderImpl() {
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(ClasspathNavigatorProviderImpl.class, "NM_Classpath");
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(ClasspathNavigatorProviderImpl.class, "SD_Classpath");
    }
    
    public JComponent getComponent() {
        if (panel == null) {
            final PropertySheetView view = new PropertySheetView();
            class Panel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
                // Make sure action context works correctly:
                private final Lookup lookup = ExplorerUtils.createLookup(manager, new ActionMap());
                {
                    setLayout(new BorderLayout());
                    add(view, BorderLayout.CENTER);
                }
                public ExplorerManager getExplorerManager() {
                    return manager;
                }
                public Lookup getLookup() {
                    return lookup;
                }
            }
            panel = new Panel();
        }
        return panel;
    }

    public Lookup getLookup() {
        return null;
    }

    public void panelActivated(Lookup context) {
        FileObject file = context.lookup(FileObject.class);
        if (file != null) {
            try {
                ClasspathNodeImpl node = new ClasspathNodeImpl(file);
                manager.setRootContext(node);
                manager.setSelectedNodes(new Node[] {node});
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void panelDeactivated() {
        manager.setRootContext(new AbstractNode(Children.LEAF));
    }

    private static final class ClasspathNodeImpl extends FilterNode {

        private final FileObject file;

        public ClasspathNodeImpl(FileObject file) throws DataObjectNotFoundException {
            super(DataObject.find(file).getNodeDelegate(), Children.LEAF);
            this.file = file;
        }

        @Override
        public PropertySet[] getPropertySets() {
            Sheet sheet = new Sheet();

            // Add classpath-related properties.
            Sheet.Set ps = new Sheet.Set();
            ps.setName("classpaths"); // NOI18N
            ps.setDisplayName(getMessage(ClasspathNavigatorProviderImpl.class, "LBL_JavaNode_sheet_classpaths"));
            ps.setShortDescription(getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_sheet_classpaths"));
            ps.put(new Node.Property[]{
                        new ClasspathProperty(ClassPath.COMPILE,
                        false,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_compile_classpath"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_compile_classpath")),
                        new ClasspathProperty(JavaClassPathConstants.MODULE_COMPILE_PATH,
                        false,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_module_compile_classpath"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_module_compile_classpath")),
                        new ClasspathProperty(JavaClassPathConstants.MODULE_CLASS_PATH,
                        false,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_module_classpath"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_module_classpath")),
                        new ClasspathProperty(ClassPath.SOURCE,
                        false,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_source_classpath"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_source_classpath")),
                        new ClasspathProperty(JavaClassPathConstants.MODULE_SOURCE_PATH,
                        false,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_module_sourcepath"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_module_sourcepath")),
                        new ClasspathProperty(ClassPath.BOOT,
                        false,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_boot_classpath"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_boot_classpath")),
                        new ClasspathProperty(JavaClassPathConstants.MODULE_BOOT_PATH,
                        false,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_module_boot_classpath"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_module_boot_classpath")),
                        new ClasspathProperty(ClassPath.EXECUTE,
                        false,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_execute_classpath"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_execute_classpath")),
                        new ClasspathProperty(JavaClassPathConstants.PROCESSOR_PATH,
                        false,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_processorpath"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_processorpath")),
                        new ClasspathProperty(ClassPath.COMPILE,
                        true,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_compile_classpath_tr"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_compile_classpath_tr")),
                        new ClasspathProperty(JavaClassPathConstants.MODULE_COMPILE_PATH,
                        true,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_module_compile_classpath_tr"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_module_compile_classpath_tr")),
                        new ClasspathProperty(JavaClassPathConstants.MODULE_CLASS_PATH,
                        true,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_module_classpath_tr"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_module_classpath_tr")),
                        new ClasspathProperty(ClassPath.SOURCE,
                        true,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_source_classpath_tr"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_source_classpath_tr")),
                        new ClasspathProperty(ClassPath.EXECUTE,
                        true,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_execute_classpath_tr"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_execute_classpath_tr")),
                        new ClasspathProperty(ClassPath.BOOT,
                        true,
                        getMessage(ClasspathNavigatorProviderImpl.class, "PROP_JavaNode_boot_classpath_tr"),
                        getMessage(ClasspathNavigatorProviderImpl.class, "HINT_JavaNode_boot_classpath_tr")),
                        new PropertySupport.ReadOnly<String>("ClassPathDefiner", String.class, getMessage(ClasspathNavigatorProviderImpl.class, "DN_ClassPathDefiner"), getMessage(ClasspathNavigatorProviderImpl.class, "HINT_ClassPathDefiner")) {
                            @Override public String getValue() throws IllegalAccessException, InvocationTargetException {
                                for (ClassPathProvider impl  : Lookup.getDefault().lookupResult(ClassPathProvider.class).allInstances()) {
                                    if (impl.findClassPath(file, ClassPath.SOURCE) != null) {
                                        return impl.getClass().getName();
                                    }
                                }

                                return "<none>";
                            }
                        },
                        new PropertySupport.ReadOnly<String>("OwningProject", String.class, getMessage(ClasspathNavigatorProviderImpl.class, "DN_OwningProject"), getMessage(ClasspathNavigatorProviderImpl.class, "HINT_OwningProject")) {
                            @Override public String getValue() throws IllegalAccessException, InvocationTargetException {
                                Project prj = FileOwnerQuery.getOwner(file);

                                if (prj != null) {
                                    return prj.toString() + "(" + prj.getClass().getName() + ")";
                                }

                                return "<none>";
                            }
                        }
                    });
            sheet.put(ps);
            return sheet.toArray();
        }
    
        /**
         * Displays one kind of classpath for this Java source.
         * Tries to use the normal format (directory or JAR names), falling back to URLs if necessary.
         */
        private final class ClasspathProperty extends PropertySupport.ReadOnly<String> {

            private final String id;
            private final boolean translate;

            public ClasspathProperty(String id, boolean translate, String displayName, String shortDescription) {
                super(id + (translate ? "_tr" : ""), /*XXX NbClassPath would be preferable, but needs org.openide.execution*/String.class, displayName, shortDescription);
                this.id = id;
                this.translate = translate;
                // XXX the following does not always work... why?
                setValue("oneline", false); // NOI18N
            }

            public String getValue() {
                ClassPath cp = ClassPath.getClassPath(file, id);
                if (cp != null) {
                    if (translate) {
                        if (ClassPath.SOURCE.equals(id)) {
                            cp = CacheClassPath.forSourcePath(cp, false);
                        } else {
                            cp = CacheClassPath.forClassPath(cp, false);
                        }
                    }
                    StringBuffer sb = new StringBuffer();
                    for (ClassPath.Entry entry : cp.entries()) {
                        URL u = entry.getURL();
                        String item = u.toExternalForm(); // fallback
                        if (u.getProtocol().equals("file")) { // NOI18N
                            item = new File(URI.create(item)).getAbsolutePath();
                        } else if (u.getProtocol().equals("jar") && item.endsWith("!/")) { // NOI18N
                            URL embedded = FileUtil.getArchiveFile(u);
                            assert embedded != null : u;
                            if (embedded.getProtocol().equals("file")) { // NOI18N
                                item = new File(URI.create(embedded.toExternalForm())).getAbsolutePath();
                            }
                        }
                        if (sb.length() > 0) {
                            sb.append('\n');
                        }
                        sb.append(item);
                    }
                    return sb.toString();
                } else {
                    return getMessage(ClasspathNavigatorProviderImpl.class, "LBL_JavaNode_classpath_unknown");
                }
            }
        }
    }
    
}
