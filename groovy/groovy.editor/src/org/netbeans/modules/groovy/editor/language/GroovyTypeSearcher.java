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

package org.netbeans.modules.groovy.editor.language;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.IndexSearcher.Descriptor;
import org.netbeans.modules.csl.api.IndexSearcher.Helper;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class GroovyTypeSearcher implements IndexSearcher {

    private static final Logger LOGGER = Logger.getLogger(GroovyTypeSearcher.class.getName());

    @Override
    public Set<? extends Descriptor> getSymbols(Project project, String textForQuery, Kind kind, Helper helper) {
        // TODO - search for methods too!!

        // For now, just at a minimum do the types
        return getTypes(project, textForQuery, kind, helper);
    }

    @Override
    public Set<? extends Descriptor> getTypes(Project project, String textForQuery, Kind kind, Helper helper) {
        GroovyIndex index = GroovyIndex.get(QuerySupport.findRoots(project, Collections.singleton(ClassPath.SOURCE), Collections.<String>emptySet(), Collections.<String>emptySet()));

        kind = adjustKind(kind, textForQuery);
        
        if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX /*|| kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP*/) {
            textForQuery = textForQuery.toLowerCase(Locale.ENGLISH);
        }
        
        Set<IndexedClass> classes = null;
        if (textForQuery.length() > 0) {
            classes = index.getClasses(textForQuery, kind);
        } else {
            return Collections.emptySet();
        }
        
        Set<GroovyTypeDescriptor> result = new HashSet<GroovyTypeDescriptor>();
        
        for (IndexedClass cls : classes) {
            result.add(new GroovyTypeDescriptor(cls, helper));
        }
        
        return result;
    }

    private static boolean isAllUpper( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            char c = text.charAt(i);
            if (!Character.isUpperCase(c) && c != ':' ) {
                return false;
            }
        }
        
        return true;
    }

    private static Pattern camelCasePattern = Pattern.compile("(?:\\p{javaUpperCase}(?:\\p{javaLowerCase}|\\p{Digit}|\\:|\\.|\\$)*){2,}"); // NOI18N
    
    private static boolean isCamelCase(String text) {
         return camelCasePattern.matcher(text).matches();
    }

    private QuerySupport.Kind cachedKind;
    private String cachedString = "/";

    private QuerySupport.Kind adjustKind(QuerySupport.Kind kind, String text) {
        if (text.equals(cachedString)) {
            return cachedKind;
        }
        if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX) {
            if ((isAllUpper(text) && text.length() > 1) || isCamelCase(text)) {
                kind = QuerySupport.Kind.CAMEL_CASE;
            }
        }

        cachedString = text;
        cachedKind = kind;
        return kind;
    }
    
    private class GroovyTypeDescriptor extends Descriptor {
        private final IndexedElement element;
        private String projectName;
        private Icon projectIcon;
        private final Helper helper;
        private boolean isLibrary;
        
        public GroovyTypeDescriptor(IndexedElement element, Helper helper) {
            this.element = element;
            this.helper = helper;
        }

        @Override
        public Icon getIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            return helper.getIcon(element);
        }

        @Override
        public String getTypeName() {
            return element.getName();
        }

        @Override
        public String getProjectName() {
            if (projectName == null) {
                initProjectInfo();
            }
            return projectName;
        }

        private void initProjectInfo() {
            FileObject fo = element.getFileObject();
            if (fo != null) {
                // Findbugs-Removed: File f = FileUtil.toFile(fo);
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {

                    ProjectInformation pi = ProjectUtils.getInformation(p);
                    projectName = pi.getDisplayName();
                    projectIcon = pi.getIcon();

                }
            } else {
                isLibrary = true;
                LOGGER.log(Level.FINE, "No fileobject for {0}", element.toString());
            }
            if (projectName == null) {
                projectName = "";
            }
        }
        
        @Override
        public Icon getProjectIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            if (isLibrary) {
                return ImageUtilities.loadImageIcon(GroovySources.GROOVY_FILE_ICON_16x16, false);
            }
            return projectIcon;
        }

        @Override
        public FileObject getFileObject() {
            return element.getFileObject();
        }

        @Override
        public void open() {
            FileObject fileObject = element.getFileObject();
            if (fileObject == null) {
                NotifyDescriptor nd =
                    new NotifyDescriptor.Message(NbBundle.getMessage(GroovyTypeSearcher.class, "FileDeleted"),
                    NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                // TODO: Try to remove the item from the index? Can't fix yet because the url is wiped
                // out by getFileObject (to avoid checking file existence multiple times; use a boolean
                // flag for that instead)
            } else {

                // TODO: Would be good to change offset to the start of the class decalaration instead 
                // of zero. Unfortunatelly we don't have such an information in the index so far and
                // parsing whole AST for that is too expensive (see issue 183727 for background)
                GsfUtilities.open(fileObject, 0, element.getName());
            }
        }

        @Override
        public String getContextName() {
            // XXX This is lame - move formatting logic to the goto action!
            StringBuilder sb = new StringBuilder();
//            String require = element.getRequire();
//            String fqn = element.getFqn();
            String fqn = element.getIn() != null ? element.getIn() + "." + element.getName() : element.getName();
            if (element.getName().equals(fqn)) {
                fqn = null;
            }
            if (fqn != null/* || require != null*/) {
                if (fqn != null) {
                    sb.append(fqn);
                }
//                if (require != null) {
//                    if (fqn != null) {
//                        sb.append(" ");
//                    }
//                    sb.append("in ");
//                    sb.append(require);
//                    sb.append(".rb");
//                }
                return sb.toString();
            } else {
                return null;
            }
        }

        @Override
        public ElementHandle getElement() {
            return element;
        }

        @Override
        public int getOffset() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getSimpleName() {
            return element.getName();
        }

        @Override
        public String getOuterName() {
            return null;
        }

    }

}
