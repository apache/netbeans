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
package org.netbeans.modules.javascript2.editor.navigation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.javascript2.model.api.Index;
import org.netbeans.modules.javascript2.model.api.IndexedElement;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Pisl
 */
public class JsIndexSearcher implements IndexSearcher{


    @Override
    public Set<? extends Descriptor> getTypes(Project project, String textForQuery, Kind searchType, Helper helper) {
        Set<JsDescriptor> result = new HashSet<>();
        final Index index = getIndex(project);

        if (index != null) {
            String fieldToSearch = (searchType == Kind.CASE_INSENSITIVE_CAMEL_CASE || searchType == Kind.CASE_INSENSITIVE_PREFIX || searchType == Kind.CASE_INSENSITIVE_REGEXP)
                    ? Index.FIELD_BASE_NAME_INSENSITIVE : Index.FIELD_BASE_NAME;
            Collection<? extends IndexResult> indexResults = index.query(fieldToSearch, textForQuery, searchType, Index.TERMS_BASIC_INFO);
            for (IndexResult indexResult : indexResults) {
                IndexedElement element = IndexedElement.create(indexResult);
                if (element.getJSKind() == JsElement.Kind.CONSTRUCTOR
                        || element.getJSKind() == JsElement.Kind.OBJECT_LITERAL) {
                    result.add(new JsDescriptor(helper, element));
                }
            }
        }
        return result;
    }

    @Override
    public Set<? extends Descriptor> getSymbols(Project project, String textForQuery, Kind searchType, Helper helper) {
        Set<JsDescriptor> result = new HashSet<>();
        final Index index = getIndex(project);

        if (index != null) {
            String fieldToSearch = (searchType == Kind.CASE_INSENSITIVE_CAMEL_CASE || searchType == Kind.CASE_INSENSITIVE_PREFIX || searchType == Kind.CASE_INSENSITIVE_REGEXP)
                    ? Index.FIELD_BASE_NAME_INSENSITIVE : Index.FIELD_BASE_NAME;
            Collection<? extends IndexResult> indexResults = index.query(fieldToSearch, textForQuery, searchType, Index.TERMS_BASIC_INFO);
            for (IndexResult indexResult : indexResults) {
                result.add(new JsDescriptor(helper, IndexedElement.create(indexResult)));
            }
        }
        return result;
    }

    private Index getIndex(Project project) {
        Set<String> sourceIds = new HashSet<>();
        Set<String> libraryIds = new HashSet<>();
        Collection<? extends PathRecognizer> lookupAll = Lookup.getDefault().lookupAll(PathRecognizer.class);
        for (PathRecognizer pathRecognizer : lookupAll) {
            Set<String> source = pathRecognizer.getSourcePathIds();
            if (source != null) {
                sourceIds.addAll(source);
            }
            Set<String> library = pathRecognizer.getLibraryPathIds();
            if (library != null) {
                libraryIds.addAll(library);
            }
        }

        final Collection<FileObject> findRoots = QuerySupport.findRoots(project,
                sourceIds,
                libraryIds,
                Collections.<String>emptySet());
        return Index.get(findRoots);
    }

    private static class JsDescriptor extends Descriptor {

        private final Helper helper;
        private final IndexedElement element;
        private String projectName;
        private Icon projectIcon;

        public JsDescriptor(Helper helper, IndexedElement element) {
            this.helper = helper;
            this.element = element;
            this.projectName = null;
        }

        @Override
        public ElementHandle getElement() {
            return element;
        }

        @Override
        public String getSimpleName() {
            return element.getName();
        }

        @Override
        public String getOuterName() {
            return null;
        }

        @Override
        public String getTypeName() {
            return element.getName();
        }

        @Override
        public String getContextName() {
            StringBuilder sb = new StringBuilder();

            FileObject file = getFileObject();
            if (file != null) {
                sb.append(FileUtil.getFileDisplayName(file));
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
            return null;
        }

        @Override
        public Icon getIcon() {
            return helper.getIcon(element);
        }

        @Override
        public String getProjectName() {
            if (projectName == null) {
                initProjectInfo();
            }
            return projectName;
        }

        @Override
        public Icon getProjectIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            return projectIcon;
        }

        @Override
        public FileObject getFileObject() {
            return element.getFileObject();
        }

        @Override
        public int getOffset() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void open() {
            FileObject fileObject = element.getFileObject();
            if (fileObject != null) {
                GsfUtilities.open(fileObject, element.getOffset(), element.getName());
            }
        }

        private void initProjectInfo() {
            FileObject fo = element.getFileObject();
            if (fo != null) {
                Project p = ProjectConvertors.getNonConvertorOwner(fo);
                if (p != null) {
                    ProjectInformation pi = ProjectUtils.getInformation(p);
                    projectName = pi.getDisplayName();
                    projectIcon = pi.getIcon();
                }
            }

            if (projectName == null) {
                projectName = "";
            }
        }

    }
}
