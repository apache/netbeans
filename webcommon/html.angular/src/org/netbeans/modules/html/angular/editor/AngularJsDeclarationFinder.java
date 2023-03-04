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
package org.netbeans.modules.html.angular.editor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.angular.Utils;
import org.netbeans.modules.html.angular.index.AngularJsController;
import org.netbeans.modules.html.angular.index.AngularJsIndex;
import org.netbeans.modules.html.angular.model.AngularConfigInterceptor;
import org.netbeans.modules.html.angular.model.AngularWhenInterceptor;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.spi.DeclarationFinder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
@DeclarationFinder.Registration(priority = 13)
public class AngularJsDeclarationFinder implements DeclarationFinder {

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        int embeddedOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(info.getSnapshot(), embeddedOffset);
        if (ts == null) {
            return DeclarationLocation.NONE;
        }

        FileObject fo = info.getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return DeclarationLocation.NONE;
        }

        ts.move(embeddedOffset);
        if (ts.moveNext()) {
            JsTokenId id = ts.token().id();
            String tokenText = ts.token().text().toString();
            if (id == JsTokenId.IDENTIFIER) {
                return findControllerLocation(fo, tokenText);
            } else {
                if (id == JsTokenId.STRING) {
                    OffsetRange range = isValueOfProperty(AngularWhenInterceptor.CONTROLLER_PROP, ts, caretOffset);
                    if (range != null) {
                        return findControllerLocation(fo, tokenText);
                    }
                    range = isValueOfProperty(AngularWhenInterceptor.TEMPLATE_URL_PROP, ts, caretOffset);
                    if (range != null) {
                        return findFileLocation(fo, Utils.cutQueryFromTemplateUrl(tokenText));
                    }
                    range = isValueOfProperty(AngularConfigInterceptor.COMPONENT_PROP, ts, caretOffset);
                    if (range == null) {
                        range = isValueOfProperty(AngularConfigInterceptor.COMPONENTS_PROP, ts, caretOffset);
                        if (range == null) {
                            range = isInObjectValueOfProperty(AngularConfigInterceptor.COMPONENTS_PROP, ts, caretOffset);
                        }
                    }
                    if (range != null) {
                        String controllerName = String.valueOf(tokenText.charAt(0)).toUpperCase()
                                .concat(tokenText.substring(1)).concat(AngularConfigInterceptor.CONTROLLER_SUFFIX);
                        return findControllerLocation(fo, controllerName);
                    }
                }
            }
            
        }
        return DeclarationLocation.NONE;
    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
//        int embeddedOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        final OffsetRange[] value = new OffsetRange[1];
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);

                if (ts == null) {
                    return;
                }

                ts.move(caretOffset);
                if (ts.moveNext()) {
                    JsTokenId id = ts.token().id();
                    if (id == JsTokenId.IDENTIFIER) {
                        value[0] = new OffsetRange(ts.offset(), ts.offset() + ts.token().length());
                        return;
                    }
                    value[0] = isValueOfProperty(AngularWhenInterceptor.CONTROLLER_PROP, ts, caretOffset);
                    if (value[0] != null) {
                        return;
                    }
                    value[0] = isValueOfProperty(AngularWhenInterceptor.TEMPLATE_URL_PROP, ts, caretOffset);
                    if (value[0] != null) {
                        return;
                    }
                    value[0] = isValueOfProperty(AngularConfigInterceptor.COMPONENT_PROP, ts, caretOffset);
                    if (value[0] != null) {
                        return;
                    }
                    value[0] = isValueOfProperty(AngularConfigInterceptor.COMPONENTS_PROP, ts, caretOffset);
                    if (value[0] != null) {
                        return;
                    }
                    value[0] = isInObjectValueOfProperty(AngularConfigInterceptor.COMPONENTS_PROP, ts, caretOffset);
                }
            }
        });
        if (value[0] != null) {
            return value[0];
        }
        return OffsetRange.NONE;
    }

    private DeclarationLocation findFileLocation(FileObject fo, String endPartName) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return DeclarationLocation.NONE;
        }
        Sources sources = project.getLookup().lookup(Sources.class);
        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        
        String[] endPath = endPartName.split("/");
        Map<String, FileObject> files = new HashMap<>();
        for (SourceGroup sourceGroup : sourceGroups) {
            Collection<FileObject> filesInGroup = new ArrayList<>();
            findFilesWithEndPath(endPath, sourceGroup.getRootFolder(), filesInGroup);
            int rootPathLength = sourceGroup.getRootFolder().getPath().length();
            for (FileObject fileObject : filesInGroup) {
                if (SearchInfoUtils.SHARABILITY_FILTER.searchFile(fileObject)) {
                    String shortPathName = fileObject.getPath();
                    shortPathName = shortPathName.substring(rootPathLength + 1);
                    files.put(shortPathName, fileObject);
                }
            }
        }
        
        if (!files.isEmpty()) {
            DeclarationLocation dl = null;
            for (Map.Entry<String, FileObject> entry : files.entrySet()) {
                String shortPathName = entry.getKey();
                FileObject fileObject = entry.getValue();
                DeclarationLocation dloc = new DeclarationLocation(fileObject, 0);
                if (dl == null) {
                    dl = dloc;
                }
                AlternativeLocation aloc = new AlternativeLocationImpl(shortPathName, dloc, new AngularFileHandle(shortPathName, fileObject));
                dl.addAlternative(aloc);
            }
            if (dl != null && dl.getAlternativeLocations().size() == 1) {
                dl.getAlternativeLocations().clear();
            }

            if (dl != null) {
                return dl;
            }
        }
        return DeclarationLocation.NONE;
    }
    
    private void findFilesWithEndPath(String[] endPath, FileObject folder, Collection<FileObject> collected) {
        FileObject root = folder;
        boolean wasFound = true;
        for (int i = 0; i < endPath.length; i++) {
            root = root.getFileObject(endPath[i]);
            if (root == null) {
                wasFound = false;
                break;
            }
        }
        if (wasFound) {
            collected.add(root);
        }
        Enumeration<? extends FileObject> folders = folder.getFolders(false);
        while (folders.hasMoreElements()) {
            FileObject subFolder = folders.nextElement();
            findFilesWithEndPath(endPath, subFolder, collected);
        }
    }
    
    private DeclarationLocation findControllerLocation(FileObject fo, String controllerName) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return DeclarationLocation.NONE;
        }
        try {
            Collection<AngularJsController> controllers = AngularJsIndex.get(project).getControllers(controllerName, true);
            if (!controllers.isEmpty()) {
                DeclarationLocation dl = null;
                for (AngularJsController controller : controllers) {
                    URI uri = null;
                    try {
                        uri = controller.getDeclarationFile().toURI();
                    } catch (URISyntaxException ex) {
                        // nothing
                    }
                    if (uri != null) {
                        File file = new File(uri);
                        FileObject dfo = FileUtil.toFileObject(file);
                        DeclarationLocation dloc = new DeclarationLocation(dfo, controller.getOffset());
                        //grrr, the main declarationlocation must be also added to the alternatives
                        //if there are more than one
                        if (dl == null) {
                            //ugly DeclarationLocation alternatives handling workaround - one of the
                            //locations simply must be "main"!!!
                            dl = dloc;
                        }
                        AlternativeLocation aloc = new AlternativeLocationImpl(controller.getName(), dloc, new ElementHandle.UrlHandle(dfo.getPath()));
                        dl.addAlternative(aloc);
                    }
                }
                //and finally if there was just one entry, remove the "alternative"
                if (dl != null && dl.getAlternativeLocations().size() == 1) {
                    dl.getAlternativeLocations().clear();
                }

                if (dl != null) {
                    return dl;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return DeclarationLocation.NONE;
    }
    
    private OffsetRange isValueOfProperty(String propertyName, TokenSequence<? extends JsTokenId> ts, int caretOffset) {
        ts.move(caretOffset);
        if (ts.moveNext()) {
            JsTokenId id = ts.token().id();
            if (id == JsTokenId.STRING) {
                OffsetRange result = new OffsetRange(ts.offset(), ts.offset() + ts.token().length());
                ts.movePrevious();
                Token<? extends JsTokenId> previous = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT, JsTokenId.STRING_BEGIN));
                if (previous != null && previous.id() == JsTokenId.OPERATOR_COLON && ts.movePrevious()) {
                    previous = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT));
                    if (previous != null && previous.id() == JsTokenId.IDENTIFIER
                            && propertyName.equals(previous.text().toString())) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    private OffsetRange isInObjectValueOfProperty(String propertyName, TokenSequence<? extends JsTokenId> ts, int caretOffset) {
        // e.g. check if "demo" is inside the object which is value of "components" property
        // components: { propA: 'a', propB: 'demo' }
        ts.move(caretOffset);
        if (ts.moveNext()) {
            JsTokenId id = ts.token().id();
            if (id == JsTokenId.STRING) {
                OffsetRange result = new OffsetRange(ts.offset(), ts.offset() + ts.token().length());
                ts.movePrevious();
                Token<? extends JsTokenId> previous = LexUtilities.findPrevious(ts,
                        Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL,
                                JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT,
                                JsTokenId.STRING_BEGIN, JsTokenId.STRING, JsTokenId.STRING_END,
                                JsTokenId.OPERATOR_COMMA, JsTokenId.OPERATOR_COLON, JsTokenId.IDENTIFIER));
                if (previous != null && previous.id() == JsTokenId.BRACKET_LEFT_CURLY && ts.movePrevious()) {
                    previous = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT));
                    if (previous != null && previous.id() == JsTokenId.OPERATOR_COLON && ts.movePrevious()) {
                        previous = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT));
                        if (previous != null && previous.id() == JsTokenId.IDENTIFIER
                                && propertyName.equals(previous.text().toString())) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static class AngularFileHandle implements ElementHandle {

        private final FileObject fileObject;
        private final String displayName;
        
        public AngularFileHandle(final String displayName, final FileObject fileObject) {
            this.fileObject = fileObject;
            this.displayName = displayName;
        }
        
        @Override
        public FileObject getFileObject() {
            return fileObject;
        }

        @Override
        public String getMimeType() {
            return null;
        }

        @Override
        public String getName() {
            return displayName;
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FILE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }
    }
    
    private static class AlternativeLocationImpl implements AlternativeLocation {

        private final DeclarationLocation location;
        private final ElementHandle element;
        private String name;

        public AlternativeLocationImpl(String name, DeclarationLocation location, ElementHandle element) {
            this.location = location;
            this.name = name;
            this.element = element;
        }

        @Override
        public ElementHandle getElement() {
            return element;
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            StringBuilder sb = new StringBuilder();
            sb.append("<font color='black'>");
            sb.append(element.getName());
            sb.append("</font>");
            return sb.toString();
        }

        @Override
        public DeclarationLocation getLocation() {
            return location;
        }

        @Override
        public int compareTo(AlternativeLocation o) {
            //compare according to the file paths
            return getComparableString(this).compareTo(getComparableString(o));
        }

        private static String getComparableString(AlternativeLocation loc) {
            StringBuilder sb = new StringBuilder();
            sb.append(loc.getLocation().getOffset()); //offset
            FileObject fo = loc.getLocation().getFileObject();
            if (fo != null) {
                sb.append(fo.getPath()); //filename
            }
            return sb.toString();
        }

    }
}
