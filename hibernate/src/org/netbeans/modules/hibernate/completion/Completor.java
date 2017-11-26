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
package org.netbeans.modules.hibernate.completion;

import org.netbeans.modules.hibernate.editor.HibernateEditorUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.hibernate.service.TableColumn;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Various completor for code completing XML tags and attributes in Hibername 
 * configuration and mapping file
 * 
 * @author Dongmei Cao
 */
public abstract class Completor {

    private int anchorOffset = -1;

    public abstract List<HibernateCompletionItem> doCompletion(CompletionContext context);

    protected void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    /**
     * A simple completor for general attribute value items
     * 
     * Takes an array of strings, the even elements being the display text of the items
     * and the odd ones being the corresponding documentation of the items
     * 
     */
    public static class AttributeValueCompletor extends Completor {

        private String[] itemTextAndDocs;

        public AttributeValueCompletor(String[] itemTextAndDocs) {
            this.itemTextAndDocs = itemTextAndDocs;
        }

        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            for (int i = 0; i < itemTextAndDocs.length; i += 2) {
                if (itemTextAndDocs[i].startsWith(typedChars.trim())) {
                    HibernateCompletionItem item = HibernateCompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            itemTextAndDocs[i], itemTextAndDocs[i + 1]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentTokenOffset() + 1);
            return results;
        }
    }

    /**
     * A  completor for completing the cascade attribute with cascade styles
     * 
     */
    public static class CascadeStyleCompletor extends Completor {

        private String[] itemTextAndDocs;

        public CascadeStyleCompletor(String[] itemTextAndDocs) {
            this.itemTextAndDocs = itemTextAndDocs;
        }

        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            String styleName = null;
            if (typedChars.contains(",")) {
                int index = typedChars.lastIndexOf(",");
                styleName = typedChars.substring(index + 1);
            } else {
                styleName = typedChars;
            }

            for (int i = 0; i < itemTextAndDocs.length; i += 2) {
                if (itemTextAndDocs[i].startsWith(styleName.trim())) {
                    HibernateCompletionItem item = HibernateCompletionItem.createCascadeStyleItem(caretOffset - styleName.length(),
                            itemTextAndDocs[i], itemTextAndDocs[i + 1]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentTokenOffset() + 1);
            return results;
        }
    }

    /**
     * A completor for completing the Java class attributes
     */
    public static class JavaClassCompletor extends Completor {

        private boolean packageOnly = false;

        public JavaClassCompletor(boolean packageOnly) {
            this.packageOnly = packageOnly;
        }

        public List<HibernateCompletionItem> doCompletion(final CompletionContext context) {
            final List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            try {
                Document doc = context.getDocument();
                final String typedChars = context.getTypedPrefix();

                JavaSource js = HibernateEditorUtil.getJavaSource(doc);
                if (js == null) {
                    return Collections.emptyList();
                }

                if (typedChars.contains(".") || typedChars.equals("")) { // Switch to normal completion
                    doNormalJavaCompletion(js, results, typedChars, context.getCurrentTokenOffset() + 1);
                } else { // Switch to smart class path completion
                    doSmartJavaCompletion(js, results, typedChars, context.getCurrentTokenOffset() + 1);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return results;
        }

        private void doNormalJavaCompletion(JavaSource js, final List<HibernateCompletionItem> results,
                final String typedPrefix, final int substitutionOffset) throws IOException {
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                    ClassIndex ci = cc.getClasspathInfo().getClassIndex();
                    int index = substitutionOffset;
                    String packName = typedPrefix;
                    String classPrefix = "";
                    int dotIndex = typedPrefix.lastIndexOf('.'); // NOI18N
                    if (dotIndex != -1) {
                        index += (dotIndex + 1);  // NOI18N
                        packName = typedPrefix.substring(0, dotIndex);
                        classPrefix = (dotIndex + 1 < typedPrefix.length()) ? typedPrefix.substring(dotIndex + 1) : "";
                    }
                    addPackages(ci, results, typedPrefix, index);

                    PackageElement pkgElem = cc.getElements().getPackageElement(packName);
                    if (pkgElem == null) {
                        return;
                    }

                    if (!packageOnly) {
                        List<? extends Element> pkgChildren = pkgElem.getEnclosedElements();
                        for (Element pkgChild : pkgChildren) {
                            if ((pkgChild.getKind() == ElementKind.CLASS) && pkgChild.getSimpleName().toString().startsWith(classPrefix)) {
                                TypeElement typeElement = (TypeElement) pkgChild;
                                HibernateCompletionItem item = HibernateCompletionItem.createTypeItem(substitutionOffset,
                                        typeElement, ElementHandle.create(typeElement),
                                        cc.getElements().isDeprecated(pkgChild), false);
                                results.add(item);
                            }
                        }
                    }

                    setAnchorOffset(index);
                }
            }, true);
        }

        private void doSmartJavaCompletion(final JavaSource js, final List<HibernateCompletionItem> results,
                final String typedPrefix, final int substitutionOffset) throws IOException {
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                    ClassIndex ci = cc.getClasspathInfo().getClassIndex();
                    // add packages
                    addPackages(ci, results, typedPrefix, substitutionOffset);

                    if (!packageOnly) {
                        // add classes 
                        Set<ElementHandle<TypeElement>> matchingTypes = ci.getDeclaredTypes(typedPrefix,
                                NameKind.CASE_INSENSITIVE_PREFIX, EnumSet.allOf(SearchScope.class));
                        for (ElementHandle<TypeElement> eh : matchingTypes) {
                            if (eh.getKind() == ElementKind.CLASS) {
                                if (eh.getKind() == ElementKind.CLASS) {
                                    LazyTypeCompletionItem item = LazyTypeCompletionItem.create(substitutionOffset, eh, js);
                                    results.add(item);
                                }
                            }
                        }
                    }
                }
            }, true);

            setAnchorOffset(substitutionOffset);
        }

        private void addPackages(ClassIndex ci, List<HibernateCompletionItem> results, String typedPrefix, int substitutionOffset) {
            Set<String> packages = ci.getPackageNames(typedPrefix, true, EnumSet.allOf(SearchScope.class));
            for (String pkg : packages) {
                if (pkg.length() > 0) {
                    HibernateCompletionItem item = HibernateCompletionItem.createPackageItem(substitutionOffset, pkg, false);
                    results.add(item);
                }
            }
        }
    }

    /**
     * A completor for completing Java properties/fields attributes
     */
    public static class PropertyCompletor extends Completor {

        public PropertyCompletor() {
        }

        @Override
        public List<HibernateCompletionItem> doCompletion(final CompletionContext context) {

            final List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            final int caretOffset = context.getCaretOffset();
            final String typedChars = context.getTypedPrefix();

            final String className = HibernateEditorUtil.getClassName(context.getTag());
            if (className == null) {
                return Collections.emptyList();
            }

            try {
                // Compile the class and find the fiels
                JavaSource classJavaSrc = HibernateEditorUtil.getJavaSource(context.getDocument());
                classJavaSrc.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController cc) throws Exception {
                        cc.toPhase(Phase.ELEMENTS_RESOLVED);
                        TypeElement typeElem = cc.getElements().getTypeElement(className);

                        if (typeElem == null) {
                            return;
                        }

                        List<? extends Element> clsChildren = typeElem.getEnclosedElements();
                        for (Element clsChild : clsChildren) {
                            if (clsChild.getKind() == ElementKind.FIELD) {
                                VariableElement elem = (VariableElement) clsChild;
                                HibernateCompletionItem item = HibernateCompletionItem.createClassPropertyItem(caretOffset - typedChars.length(), elem, ElementHandle.create(elem), cc.getElements().isDeprecated(clsChild));
                                results.add(item);
                            }
                        }
                    }
                }, true);


            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            setAnchorOffset(context.getCurrentTokenOffset() + 1);

            return results;
        }
    }

    /**
     * A completor for completing database table names
     */
    public static class DatabaseTableCompletor extends Completor {

        public DatabaseTableCompletor() {
        }

        @Override
        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            List<String> tableNames = getDatabaseTableNamesForThisMappingFile(context);

            for (String tableName : tableNames) {
                if(tableName.startsWith(typedChars.trim())){
                    HibernateCompletionItem item = HibernateCompletionItem.createDatabaseTableItem(
                            caretOffset - typedChars.length(), tableName);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentTokenOffset() + 1);

            return results;
        }

        private List<String> getDatabaseTableNamesForThisMappingFile(CompletionContext context) {
            List<String> tableNames = new ArrayList<String>();
            FileObject mappingFile = org.netbeans.modules.editor.NbEditorUtilities.getFileObject(context.getDocument());
            Project enclosingProject = FileOwnerQuery.getOwner(
                    mappingFile);
            HibernateEnvironment env = enclosingProject.getLookup().lookup(HibernateEnvironment.class);
            if(env != null) {
                tableNames = env.getDatabaseTables(mappingFile);
            } 
            return tableNames;
        }
    }

    /**
     * A completor for completing database table column names
     */
    public static class DatabaseTableColumnCompletor extends Completor {

        public DatabaseTableColumnCompletor() {
        }

        @Override
        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            final String tableName = HibernateEditorUtil.getTableName(context.getTag());

            if (tableName == null) {
                return Collections.emptyList();
            }

            List<TableColumn> tableColumns = getColumnsForTable(context, tableName);

            for (TableColumn tableColumn : tableColumns) {
                if(tableColumn.getColumnName().startsWith(typedChars.trim())) {
                    HibernateCompletionItem item = HibernateCompletionItem.createDatabaseColumnItem(
                            caretOffset - typedChars.length(), tableColumn.getColumnName(), tableColumn.isPrimaryKey());
                    results.add(item);
                }
            }
            setAnchorOffset(context.getCurrentTokenOffset() + 1);

            return results;
        }

        private List<TableColumn> getColumnsForTable(CompletionContext context, String tableName) {
            List<TableColumn> tableColumns = new ArrayList<TableColumn>();
            FileObject currentFileObject = org.netbeans.modules.editor.NbEditorUtilities.getFileObject(context.getDocument());
            Project enclosingProject = FileOwnerQuery.getOwner(
                    currentFileObject);
            HibernateEnvironment env = enclosingProject.getLookup().lookup(HibernateEnvironment.class);
            if(env != null) {
                tableColumns = env.getColumnsForTable(tableName, currentFileObject);
            }
            return tableColumns;
        }
    }
    
    /**
     * A completor for completing the Hibernate property names in Hibernate configuration file
     * 
     */
    public static class HbPropertyNameCompletor extends Completor {

        private String[] itemTextAndDocs;

        public HbPropertyNameCompletor(String[] itemTextAndDocs) {
            this.itemTextAndDocs = itemTextAndDocs;
        }

        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();
            
            for (int i = 0; i < itemTextAndDocs.length; i += 2) {
                if (itemTextAndDocs[i].startsWith(typedChars.trim()) 
                        || itemTextAndDocs[i].startsWith( "hibernate." + typedChars.trim()) ) { // NOI18N
                    HibernateCompletionItem item = HibernateCompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            itemTextAndDocs[i], itemTextAndDocs[i + 1]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentTokenOffset() + 1);
            return results;
        }
    }

    /**
     * A completor for completing Hibernate mapping files
     */
    public static class HbMappingFileCompletor extends Completor {

        public HbMappingFileCompletor() {
        }

        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            String[] mappingFiles = getMappingFilesFromProject(context);

            for (int i = 0; i < mappingFiles.length; i++) {
                if (mappingFiles[i].startsWith(typedChars.trim())) {
                    HibernateCompletionItem item =
                            HibernateCompletionItem.createHbMappingFileItem(caretOffset - typedChars.length(),
                            mappingFiles[i]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentTokenOffset() + 1);
            return results;
        }
        
        // Gets the list of mapping files from HibernateEnvironment.
        private String[] getMappingFilesFromProject(CompletionContext context) {
            Project enclosingProject = FileOwnerQuery.getOwner(
                    NbEditorUtilities.getFileObject(context.getDocument())
                    );
            HibernateEnvironment env = enclosingProject.getLookup().lookup(HibernateEnvironment.class);
            if(env != null) {
                return env.getAllHibernateMappings().toArray(new String[]{});
            } else {
                return new String[0];
            }
                
        }
    }
}
