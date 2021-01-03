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
package org.netbeans.modules.python.source;

import java.awt.Toolkit;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.modules.python.source.elements.IndexedElement;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.IndexSearcher.Descriptor;
import org.netbeans.modules.csl.api.IndexSearcher.Helper;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.python.antlr.PythonTree;

public class PythonIndexSearcher implements IndexSearcher {

    @Override
    public Set<? extends Descriptor> getTypes(Project prjct, String textForQuery, QuerySupport.Kind kind, Helper helper) {
        PythonIndex index = PythonIndex.get(prjct);
        Set<PythonSymbol> result = new HashSet<>();
        Set<? extends IndexedElement> elements;

        // TODO - do some filtering if you use ./#
        //        int dot = textForQuery.lastIndexOf('.');
        //        if (dot != -1 && (kind == QuerySupport.Kind.PREFIX || kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX)) {
        //            String prefix = textForQuery.substring(dot+1);
        //            String in = textForQuery.substring(0, dot);

        elements = index.getClasses(textForQuery, kind, null, true);
        for (IndexedElement element : elements) {
            result.add(new PythonSymbol(element, helper));
        }

        return result;
    }

    @Override
    public Set<? extends Descriptor> getSymbols(Project prjct, String textForQuery, QuerySupport.Kind kind, Helper helper) {
        PythonIndex index = PythonIndex.get(prjct);
        Set<PythonSymbol> result = new HashSet<>();
        Set<? extends IndexedElement> elements;

        // TODO - do some filtering if you use ./#
        //        int dot = textForQuery.lastIndexOf('.');
        //        if (dot != -1 && (kind == QuerySupport.Kind.PREFIX || kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX)) {
        //            String prefix = textForQuery.substring(dot+1);
        //            String in = textForQuery.substring(0, dot);

        elements = index.getAllMembers(textForQuery, kind, null, true);
        for (IndexedElement element : elements) {
            result.add(new PythonSymbol(element, helper));
        }
        elements = index.getClasses(textForQuery, kind, null, true);
        for (IndexedElement element : elements) {
            result.add(new PythonSymbol(element, helper));
        }
        elements = index.getModules(textForQuery, kind);
        for (IndexedElement element : elements) {
            result.add(new PythonSymbol(element, helper));
        }

        return result;
    }

    private class PythonSymbol extends Descriptor {
        private final IndexedElement element;
        private String projectName;
        private Icon projectIcon;
        private final Helper helper;
        private boolean isLibrary;
        private static final String ICON_PATH = "org/netbeans/modules/python/editor/resources/pyc_16.png"; //NOI18N

        public PythonSymbol(IndexedElement element, Helper helper) {
            this.element = element;
            this.helper = helper;
        }

        @Override
        public Icon getIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            //if (isLibrary) {
            //    return new ImageIcon(org.openide.util.ImageUtilities.loadImage(PYTHON_KEYWORD));
            //}
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
//                File f = FileUtil.toFile(fo);
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {
//                    JsPlatform platform = JsPlatform.platformFor(p);
//                    if (platform != null) {
//                        String lib = platform.getLib();
//                        if (lib != null && f.getPath().startsWith(lib)) {
//                            projectName = "Js Library";
//                            isLibrary = true;
//                        }
//                    } else {
                    ProjectInformation pi = ProjectUtils.getInformation(p);
                    projectName = pi.getDisplayName();
                    projectIcon = pi.getIcon();
//                    }
                }
            } else {
                isLibrary = true;
                Logger.getLogger(PythonIndexSearcher.class.getName()).fine("No fileobject for " + element.toString() + " with fileurl=" + element.getFilenameUrl());
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
                return ImageUtilities.loadImageIcon(ICON_PATH, false);
            }
            return projectIcon;
        }

        @Override
        public FileObject getFileObject() {
            return element.getFileObject();
        }

        @Override
        public void open() {
            PythonParserResult[] parserResultRet = new PythonParserResult[1];
            PythonTree node = PythonAstUtils.getForeignNode(element, parserResultRet);

            if (node != null) {
                int astOffset = PythonAstUtils.getRange(node).getStart();
                int lexOffset = PythonLexerUtils.getLexerOffset(parserResultRet[0], astOffset);
                if (lexOffset == -1) {
                    lexOffset = 0;
                }
                GsfUtilities.open(element.getFileObject(), lexOffset, element.getName());
                return;
            }

            FileObject fileObject = element.getFileObject();
            if (fileObject == null) {
                // This should no longer be needed - we perform auto deletion in GSF
                Toolkit.getDefaultToolkit().beep();
                return;
            }

            helper.open(fileObject, element);
        }

        @Override
        public String getContextName() {
            // XXX This is lame - move formatting logic to the goto action!
//            StringBuilder sb = new StringBuilder();
//            String require = element.getRequire();
//            String fqn = element.getFqn();
            String fqn = element.getIn() != null ? element.getIn() + "." + element.getName() : element.getName();
            if (element.getName().equals(fqn)) {
                fqn = null;
                String url = element.getFilenameUrl();
                if (url != null) {
                    return url.substring(url.lastIndexOf('/') + 1);
                }
            }

            return fqn;
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
