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
package org.netbeans.modules.j2ee.persistence.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Dongmei Cao
 */
public class JPAEditorUtil {
    
    public static final String JDBCURLKEY="url";//NOI18N
    public static final String JDBCUSERKEY="user";//NOI18N
    public static final String JDBCDRIVERKEY="driver";//NOI18N

    public static JavaSource getJavaSource(Document doc) {
        FileObject fileObject = NbEditorUtilities.getFileObject(doc);
        if (fileObject == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return null;
        }
        // XXX this only works correctly with projects with a single sourcepath,
        // but we don't plan to support another kind of projects anyway (what about Maven?).
        // mkleint: Maven has just one sourceroot for java sources, the config files are placed under
        // different source root though. JavaProjectConstants.SOURCES_TYPE_RESOURCES
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sourceGroup : sourceGroups) {
            return JavaSource.create(ClasspathInfo.create(sourceGroup.getRootFolder()));
        }
        return null;
    }

    public static Node getClassNode(Node tag) {
        if (tag == null) {
            return null;
        }

        if (tag.getNodeName().equalsIgnoreCase("class") || // NOI18N
                tag.getNodeName().equalsIgnoreCase("subclass") || // NOI18N
                tag.getNodeName().equalsIgnoreCase("joined-subclass") || // NOI18N
                tag.getNodeName().equalsIgnoreCase("union-subclass")) { // NOI18N
            return tag;
        }

        Node current = tag;
        while (true) {
            Node parent = current.getParentNode();
            if(parent == null) {
                // See issue 138974
                return null;
            }
            if (parent.getNodeName().equalsIgnoreCase("class") || // NOI18N
                    parent.getNodeName().equalsIgnoreCase("subclass") || // NOI18N
                    parent.getNodeName().equalsIgnoreCase("joined-subclass") || // NOI18N
                    parent.getNodeName().equalsIgnoreCase("union-subclass")) {
                // Found it
                return parent;
            } else if (parent.getNodeName().equalsIgnoreCase("hibernate-mapping")) {
                // Hit the root element
                return null;
            } else {
                // Keep going
                current = parent;
            }
        }
    }

    public static String getClassName(Node tag) {
        Node classNode = getClassNode(tag);
        if (classNode != null) {
            NamedNodeMap attribs = classNode.getAttributes();
            if (attribs != null && attribs.getNamedItem("name") != null) { // NOI18N
                return attribs.getNamedItem("name").getNodeValue(); // NOI18N
            }
        }

        return null;
    }

    public static String getTableName(Node tag) {
        Node classNode = getClassNode(tag);
        if (classNode != null) {
            NamedNodeMap attribs = classNode.getAttributes();
            if (attribs != null && attribs.getNamedItem("table") != null) { // NOI18N
                return attribs.getNamedItem("table").getNodeValue(); // NOI18N
            }
        }

        return null;
    }

    public static String getPersistencePropertyName(Node tag) {
        if (!tag.getNodeName().equalsIgnoreCase("property")) {
            return null;
        } else {
            NamedNodeMap attribs = tag.getAttributes();
            if (attribs != null && attribs.getNamedItem("name") != null) { // NOI18N
                return attribs.getNamedItem("name").getNodeValue();
            }
        }

        return null;
    }

    public static TypeElement findClassElementByBinaryName(final String binaryName, CompilationController cc) {
        if (!binaryName.contains("$")) { // NOI18N
            // fast search based on fqn
            return cc.getElements().getTypeElement(binaryName);
        } else {
            // get containing package
            String packageName = ""; // NOI18N
            int dotIndex = binaryName.lastIndexOf('.'); // NOI18N
            if (dotIndex != -1) {
                packageName = binaryName.substring(0, dotIndex);
            }
            PackageElement packElem = cc.getElements().getPackageElement(packageName);
            if (packElem == null) {
                return null;
            }

            // scan for element matching the binaryName
            return new BinaryNameTypeScanner().visit(packElem, binaryName);
        }
    }

    private static class BinaryNameTypeScanner extends SimpleElementVisitor6<TypeElement, String> {

        @Override
        public TypeElement visitPackage(PackageElement packElem, String binaryName) {
            for (Element e : packElem.getEnclosedElements()) {
                if (e.getKind().isClass()) {
                    TypeElement ret = e.accept(this, binaryName);
                    if (ret != null) {
                        return ret;
                    }
                }
            }

            return null;
        }
    }

    public static void findAndOpenJavaClass(final String classBinaryName, Document doc) {
        final JavaSource js = getJavaSource(doc);
        if (js != null) {
            try {
                js.runUserActionTask( (CompilationController cc) -> {
                    boolean opened = false;
                    TypeElement element = findClassElementByBinaryName(classBinaryName, cc);
                    if (element != null) {
                        opened = ElementOpen.open(js.getClasspathInfo(), element);
                    }
                    if (!opened) {
                        String msg = NbBundle.getMessage(JPAEditorUtil.class, "LBL_SourceNotFound", classBinaryName);
                        StatusDisplayer.getDefault().setStatusText(msg);
                    }
                }, false);
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
    
    public static VariableElement findFieldElementOnType(ElementUtilities eu, TypeMirror type, String fieldName) {
        FieldAcceptor fieldAcceptor = new FieldAcceptor(fieldName);
        Iterable<? extends Element> matchingProp = eu.getMembers(type, fieldAcceptor);
        Iterator<? extends Element> it = matchingProp.iterator();
        
        // no matching element found
        if (!it.hasNext()) {
            return null;
        } else {
            return (VariableElement)it.next();
        }
        
    }

    private static class FieldAcceptor implements ElementUtilities.ElementAcceptor {

        private String fieldName;

        public FieldAcceptor(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public boolean accept(Element e, TypeMirror type) {
            
            if (e.getKind() != ElementKind.FIELD) {
                return false;
            } else {
                if( e.getSimpleName().toString().equals(fieldName) ) {
                     return true;
                }
            }
            
            return false;
        }
    }
    public static DatabaseConnection findDatabaseConnection(PersistenceUnit pu, Project project) {

        // try to find a connection specified using the PU properties
        DatabaseConnection dbcon = ProviderUtil.getConnection(pu);
        if (dbcon != null) {
            return dbcon;
        }

        // try to find a datasource-based connection, but only for a FileObject-based context,
        // otherwise we don't have a J2eeModuleProvider to retrieve the DS's from
        String datasourceName = ProviderUtil.getDatasourceName(pu);
        if (datasourceName == null) {
            return null;
        }

        if (project == null) {
            return null;
        }
        JPADataSource datasource = null;
        JPADataSourceProvider dsProvider = project.getLookup().lookup(JPADataSourceProvider.class);
        if (dsProvider == null) {
            return null;
        }
        for (JPADataSource each : dsProvider.getDataSources()) {
            if (datasourceName.equals(each.getJndiName())) {
                datasource = each;
            }
        }
        if (datasource == null) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "The " + datasourceName + " was not found."); // NOI18N
            return null;
        }
        List<DatabaseConnection> dbconns = findDatabaseConnections(datasource);
        if (!dbconns.isEmpty()) {
            return dbconns.get(0);
        }
        return null;
    }
    public static HashMap<String,String> findDatabaseConnectionProperties(PersistenceUnit pu, Project project) {

        // try to find a connection specified using the PU properties
        HashMap<String,String> props = ProviderUtil.getConnectionProperties(pu);
        if (props != null && !props.isEmpty()) {
            return props;
        }

        // try to find a datasource-based connection, but only for a FileObject-based context,
        // otherwise we don't have a J2eeModuleProvider to retrieve the DS's from
        String datasourceName = ProviderUtil.getDatasourceName(pu);
        if (datasourceName == null) {
            return null;
        }

        if (project == null) {
            return null;
        }
        JPADataSource datasource = null;
        JPADataSourceProvider dsProvider = project.getLookup().lookup(JPADataSourceProvider.class);
        if (dsProvider == null) {
            return null;
        }
        for (JPADataSource each : dsProvider.getDataSources()) {
            if (datasourceName.equals(each.getJndiName())) {
                datasource = each;
            }
        }
        if (datasource == null) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "The " + datasourceName + " was not found."); // NOI18N
            return null;
        }
        props = new HashMap<String,String>();
        props.put(JDBCURLKEY,datasource.getUrl());
        props.put(JDBCUSERKEY,datasource.getUsername());
        props.put(JDBCDRIVERKEY, datasource.getDriverClassName());
        return props;
    }
    private static List<DatabaseConnection> findDatabaseConnections(JPADataSource datasource) {
        // copied from j2ee.common.DatasourceHelper (can't depend on that)
        if (datasource == null) {
            throw new NullPointerException("The datasource parameter cannot be null."); // NOI18N
        }
        String databaseUrl = datasource.getUrl();
        String user = datasource.getUsername();
        if (databaseUrl == null || user == null) {
            return Collections.emptyList();
        }
        List<DatabaseConnection> result = new ArrayList<>();
        for (DatabaseConnection dbconn : ConnectionManager.getDefault().getConnections()) {
            if (databaseUrl.equals(dbconn.getDatabaseURL()) && user.equals(dbconn.getUser())) {
                result.add(dbconn);
            }
        }
        if (!result.isEmpty()) {
            return Collections.unmodifiableList(result);
        } else {
            return Collections.emptyList();
        }
    }

}

