/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
        Set<JsDescriptor> result = new HashSet<JsDescriptor>();
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
        Set<JsDescriptor> result = new HashSet<JsDescriptor>();
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
        Set<String> sourceIds = new HashSet<String>();
        Set<String> libraryIds = new HashSet<String>();
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
