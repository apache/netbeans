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
package org.netbeans.modules.j2ee.persistence.editor.completion;

import java.io.IOException;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.editor.CompletionContext;
import org.netbeans.modules.j2ee.persistence.editor.JPAEditorUtil;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;

/**
 * Various completor for code completing XML tags and attributes in Hibername
 * configuration and mapping file
 *
 * @author Dongmei Cao
 */
public abstract class PUCompletor {


    static class JtaDatasourceCompletor extends PUCompletor {

        @Override
        public List<JPACompletionItem> doCompletion(CompletionContext context) {
            List<JPACompletionItem> results = new ArrayList<>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();
            Project project = FileOwnerQuery.getOwner(
                    NbEditorUtilities.getFileObject(context.getDocument()));
            JPADataSourceProvider dsProvider = project.getLookup().lookup(JPADataSourceProvider.class);
            if(dsProvider != null){
                for (JPADataSource val : dsProvider.getDataSources()) {
                    if(val.getDisplayName().toLowerCase().startsWith(typedChars.trim().toLowerCase())){
                        JPACompletionItem item = JPACompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                                val.getDisplayName());
                        results.add(item);
                    }
                }
            }
            setAnchorOffset(context.getCurrentTokenOffset() + 1);
            return results;
        }
    }

    static class ProviderCompletor extends PUCompletor {

        @Override
        public List<JPACompletionItem> doCompletion(CompletionContext context) {
            List<JPACompletionItem> results = new ArrayList<>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();
            HashSet<String> providers = new HashSet<>();
            Project project = FileOwnerQuery.getOwner(
                    NbEditorUtilities.getFileObject(context.getDocument()));
            for(Provider provider: Util.getProviders(project)){
                String cl = provider.getProviderClass();
                if(cl.toLowerCase().startsWith(typedChars.trim().toLowerCase())){
                    providers.add(cl);
                }
            }

            for (String cl: providers) {
                    JPACompletionItem item = JPACompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            cl);
                    results.add(item);
            }

            setAnchorOffset(context.getCurrentTokenOffset() + 1);
            return results;
        }
    }
    

    static class ExUnlistedClassesCompletor  extends PUCompletor {
        
        @Override
        public List<JPACompletionItem> doCompletion(CompletionContext context) {
            List<JPACompletionItem> results = new ArrayList<>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();
            for (String val : new String[]{"true", "false"}) {//NOI18N
                if(val.toLowerCase().startsWith(typedChars.trim().toLowerCase())){
                    JPACompletionItem item = JPACompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            val);
                    results.add(item);
                }
            }
            setAnchorOffset(context.getCurrentTokenOffset() + 1);
            return results;
        }
    }    
    
    
    private int anchorOffset = -1;

    public abstract List<JPACompletionItem> doCompletion(CompletionContext context);

    protected void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    /**
     * A simple completor for general attribute value items
     *
     * Takes an array of strings, the even elements being the display text of
     * the items and the odd ones being the corresponding documentation of the
     * items
     *
     */
    public static class AttributeValueCompletor extends PUCompletor {

        private String[] itemTexts;

        public AttributeValueCompletor(String[] itemTextAndDocs) {
            this.itemTexts = itemTextAndDocs;
        }

        @Override
        public List<JPACompletionItem> doCompletion(CompletionContext context) {
            List<JPACompletionItem> results = new ArrayList<>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            for (int i = 0; i < itemTexts.length; i += 1) {
                if (itemTexts[i].startsWith(typedChars.trim())) {
                    JPACompletionItem item = JPACompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            itemTexts[i]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentTokenOffset() + 1);
            return results;
        }
    }

    /**
     * A completor for completing class tag
     */
    public static class EntityClassCompletor extends PUCompletor {

        @Override
        public List<JPACompletionItem> doCompletion(final CompletionContext context) {
            final List<JPACompletionItem> results = new ArrayList<>();
            try {
                Document doc = context.getDocument();
                final String typedChars = context.getTypedPrefix();

                JavaSource js = JPAEditorUtil.getJavaSource(doc);
                if (js == null) {
                    return Collections.emptyList();
                }
                FileObject fo = NbEditorUtilities.getFileObject(context.getDocument());
                doJavaCompletion(fo, js, results, typedChars, context.getCurrentTokenOffset());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return results;
        }

        private void doJavaCompletion(final FileObject fo, final JavaSource js, final List<JPACompletionItem> results,
                final String typedPrefix, final int substitutionOffset) throws IOException {
            js.runUserActionTask( (CompilationController cc) -> {
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                Project project = FileOwnerQuery.getOwner(fo);
                EntityClassScopeProvider provider = project.getLookup().lookup(EntityClassScopeProvider.class);
                EntityClassScope ecs = null;
                Entity[] entities = null;
                if (provider != null) {
                    ecs = provider.findEntityClassScope(fo);
                }
                if (ecs != null) {
                    entities = ecs.getEntityMappingsModel(false).runReadAction( metadata -> metadata.getRoot().getEntity() );
                }
                // add classes
                if(entities != null) {
                    for (Entity entity : entities) {
                        if (typedPrefix.length() == 0 || entity.getClass2().toLowerCase().startsWith(typedPrefix.toLowerCase()) 
                                || entity.getName().toLowerCase().startsWith(typedPrefix.toLowerCase())) {
                            JPACompletionItem item = JPACompletionItem.createAttribValueItem(substitutionOffset, entity.getClass2());
                            results.add(item);
                        }
                    }
                }
            }, true);

            setAnchorOffset(substitutionOffset);
        }
    }

    /**
     * A completor for completing Java properties/fields attributes
     */
    public static class PropertyCompletor extends PUCompletor {

        public PropertyCompletor() {
        }

        @Override
        public List<JPACompletionItem> doCompletion(final CompletionContext context) {

            final List<JPACompletionItem> results = new ArrayList<>();
            final int caretOffset = context.getCaretOffset();
            final String typedChars = context.getTypedPrefix();

            final String className = JPAEditorUtil.getClassName(context.getTag());
            if (className == null) {
                return Collections.emptyList();
            }

            try {
                // Compile the class and find the fiels
                JavaSource classJavaSrc = JPAEditorUtil.getJavaSource(context.getDocument());
                classJavaSrc.runUserActionTask( (CompilationController cc) -> {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElem = cc.getElements().getTypeElement(className);
                    
                    if (typeElem == null) {
                        return;
                    }
                    
                    List<? extends Element> clsChildren = typeElem.getEnclosedElements();
                    for (Element clsChild : clsChildren) {
                        if (clsChild.getKind() == ElementKind.FIELD) {
                            VariableElement elem = (VariableElement) clsChild;
                            JPACompletionItem item = JPACompletionItem.createClassPropertyItem(caretOffset - typedChars.length(), elem, ElementHandle.create(elem), cc.getElements().isDeprecated(clsChild));
                            results.add(item);
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
     * A completor for completing the persistence property names in
     * persistence.xml file
     *
     */
    public static class PersistencePropertyNameCompletor extends PUCompletor {

        private Map<Provider, Map<String, String[]>> allKeyAndValues;

        PersistencePropertyNameCompletor(Map<Provider, Map<String, String[]>> allKeyAndValues) {
            this.allKeyAndValues = allKeyAndValues;
        }

        @Override
        public List<JPACompletionItem> doCompletion(CompletionContext context) {
            List<JPACompletionItem> results = new ArrayList<>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();
            String providerClass = getProviderClass(context.getTag());
            Project enclosingProject = FileOwnerQuery.getOwner(
                    NbEditorUtilities.getFileObject(context.getDocument()));
            Provider provider = ProviderUtil.getProvider(providerClass, enclosingProject);
            ArrayList<String> keys = new ArrayList<>();
            String ver = provider == null ? context.getDocumentContext().getVersion() : ProviderUtil.getVersion(provider);
            if (provider == null || (ver!=null && !Persistence.VERSION_1_0.equals(ver))) {
                keys.addAll(allKeyAndValues.get(null).keySet());
            }
            if (provider != null && allKeyAndValues.get(provider) != null) {
                keys.addAll(allKeyAndValues.get(provider).keySet());
            }
            String itemTexts[] = keys.toArray(new String[]{});
            for (int i = 0; i < itemTexts.length; i++) {
                if (itemTexts[i].startsWith(typedChars.trim())
                        || itemTexts[i].startsWith("javax.persistence." + typedChars.trim())) { // NOI18N
                    JPACompletionItem item = JPACompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            itemTexts[i]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentTokenOffset() + 1);
            return results;
        }
    }

    /**
     * A completor for completing the persistence property value in
     * persistence.xml file
     *
     */
    public static class PersistencePropertyValueCompletor extends PUCompletor {

        private Map<Provider, Map<String, String[]>> allKeyAndValues;

        PersistencePropertyValueCompletor(Map<Provider, Map<String, String[]>> allKeyAndValues) {
            this.allKeyAndValues = allKeyAndValues;
        }

        @Override
        public List<JPACompletionItem> doCompletion(CompletionContext context) {
            List<JPACompletionItem> results = new ArrayList<>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();
            String propertyName = getPropertyName(context.getTag());
            if (propertyName == null || propertyName.isEmpty()) {
                return results;
            }
            String providerClass = getProviderClass(context.getTag());
            Project enclosingProject = FileOwnerQuery.getOwner(
                    NbEditorUtilities.getFileObject(context.getDocument()));
            Provider provider = ProviderUtil.getProvider(providerClass, enclosingProject);
            String[] values = null;
            String ver = provider == null ? context.getDocumentContext().getVersion() : ProviderUtil.getVersion(provider);
            if (provider == null || (ver!=null && !Persistence.VERSION_1_0.equals(ver))) {
                values = allKeyAndValues.get(null).get(propertyName);
            }
            if (provider != null && allKeyAndValues.get(provider) != null) {
                String [] tmp2 = allKeyAndValues.get(provider).get(propertyName);
                if(tmp2 != null) {
                    values = tmp2;
                }
                if (values == null && propertyName.equals(provider.getJdbcUrl())) {

                    //always allow this property completion, even for container managed(it's in jta-data-source  tag, not in properties)
                    DatabaseConnection[] cns = ConnectionManager.getDefault().getConnections();
                    for (DatabaseConnection cn : cns) {
                        JPACompletionItem item = JPACompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                                cn.getDatabaseURL());
                        results.add(item);
                    }
                    results.add(new JPACompletionItem.AddConnectionElementItem());
                }
            }
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    JPACompletionItem item = JPACompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            values[i]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentTokenOffset() + 1);
            return results;
        }
    }

    /**
     * A completor for completing orm mapping files
     */
    public static class PersistenceMappingFileCompletor extends PUCompletor {

        public PersistenceMappingFileCompletor() {
        }

        @Override
        public List<JPACompletionItem> doCompletion(CompletionContext context) {
            List<JPACompletionItem> results = new ArrayList<>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            String[] mappingFiles = getMappingFilesFromProject(context);

            for (int i = 0; i < mappingFiles.length; i++) {
                if (mappingFiles[i].startsWith(typedChars.trim())) {
                    JPACompletionItem item =
                            JPACompletionItem.createMappingFileItem(caretOffset - typedChars.length(),
                            mappingFiles[i]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentTokenOffset() + 1);
            return results;
        }

        // Gets the list of mapping files.
        private String[] getMappingFilesFromProject(CompletionContext context) {
            Project enclosingProject = FileOwnerQuery.getOwner(
                    NbEditorUtilities.getFileObject(context.getDocument()));
            //use persistence environment when will be supported
            if (null != null) {
                return null;
            } else {
                return new String[0];
            }

        }
    }

    private static String getProviderClass(Node tag) {
        String name = null;
        while (tag != null && !"persistence-unit".equals(tag.getNodeName())) {
            tag = tag.getParentNode();//NOI18N
        }
        if (tag != null) {
            for (Node ch = tag.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
                if ("provider".equals(ch.getNodeName())) {//NOI18N
                    name = ch.getFirstChild().getNodeValue();
                }
            }
        }
        return name;
    }

    private static String getPropertyName(Node tag) {
        String name = null;
        while (tag != null && !"property".equals(tag.getNodeName())) {
            tag = tag.getParentNode();//NOI18N
        }
        if (tag != null) {
            Node nmN = tag.getAttributes().getNamedItem("name");//NOI18N
            if (nmN != null) {
                name = nmN.getNodeValue();
            }
        }
        return name;
    }
}
