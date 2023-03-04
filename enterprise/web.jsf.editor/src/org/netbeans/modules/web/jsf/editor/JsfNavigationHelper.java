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
package org.netbeans.modules.web.jsf.editor;

import java.util.Collections;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.jsf.api.editor.JsfFacesComponentsProvider.FacesComponentLibrary;
import org.netbeans.modules.web.jsf.api.editor.JsfFacesComponentsProvider.FacesLibraryComponent;
import org.netbeans.modules.web.jsf.editor.facelets.CompositeComponentLibrary;
import org.netbeans.modules.web.jsf.editor.index.CompositeComponentModel;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Contains methods used for the navigation resolution.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsfNavigationHelper {

    private JsfNavigationHelper() {
    }

    /**
     * Gets HTML token sequence at given caret offset.
     *
     * @param th token hierarchy
     * @param caretOffset caret offset
     * @return HTML token sequence moved at given offset or {@code null}
     */
    static TokenSequence getTokenSequenceAtCaret(TokenHierarchy th, int caretOffset) {
        TokenSequence ts = Utils.getJoinedHtmlSequence(th, caretOffset);
        if (ts == null) {
            return null;
        }

        ts.move(caretOffset);
        if (ts.moveNext() || ts.movePrevious()) {
            return ts;
        }

        return null;
    }

    /**
     * Navigation implementation for JSF Composite Component libraries.
     *
     * @param htmlresult parser result
     * @param caretOffset carret offset
     * @param lib library which contains given element
     * @return declaration location where to navigate
     */
    static DeclarationLocation goToCompositeComponentLibrary(HtmlParserResult htmlresult, final int caretOffset, Library lib) {
        Snapshot snapshot = htmlresult.getSnapshot();
        Element leaf = htmlresult.findByPhysicalRange(caretOffset, true);
        OpenTag openTag = (OpenTag) leaf;
        String tagName = openTag.unqualifiedName().toString();
        LibraryComponent component = lib.getComponent(tagName);
        if (component == null) {
            return DeclarationLocation.NONE;
        }
        if (!(component instanceof CompositeComponentLibrary.CompositeComponent)) {
            //TODO add hyperlinking to class components
            return DeclarationLocation.NONE;
        }
        CompositeComponentModel model = ((CompositeComponentLibrary.CompositeComponent) component).getComponentModel();
        FileObject file = model.getSourceFile();

        //find to what exactly the user points, the AST doesn't contain attributes as nodes :-(
        int astOffset = snapshot.getEmbeddedOffset(caretOffset);

        int jumpOffset = 0;
        TokenSequence htmlTs = snapshot.getTokenHierarchy().tokenSequence();
        htmlTs.move(astOffset);
        if (htmlTs.moveNext() || htmlTs.movePrevious()) {
            if (htmlTs.token().id() == HTMLTokenId.TAG_OPEN) {
                //jumpOffset = 0;
            } else if (htmlTs.token().id() == HTMLTokenId.ARGUMENT) {
                final String attributeName = htmlTs.token().text().toString();
                //find the attribute in the interface

                Source source = Source.create(file);
                final int[] attrOffset = new int[1];
                try {
                    ParserManager.parse(Collections.singleton(source), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            Parser.Result result = resultIterator.getParserResult(caretOffset);
                            if (result instanceof HtmlParserResult) {
                                HtmlParserResult hresult = (HtmlParserResult) result;
                                Element root = hresult.root(LibraryUtils.COMPOSITE_LIBRARY_NS);
                                ElementUtils.visitChildren(root, new ElementVisitor() {
                                    @Override
                                    public void visit(Element node) {
                                        OpenTag ot = (OpenTag) node;
                                        if (LexerUtils.equals("interface", ot.unqualifiedName(), true, true)) { //NOI18N
                                            for (Element child : ot.children(ElementType.OPEN_TAG)) {
                                                OpenTag otch = (OpenTag) child;
                                                if (LexerUtils.equals("attribute", otch.unqualifiedName(), true, true)) { //NOI18N
                                                    org.netbeans.modules.html.editor.lib.api.elements.Attribute nameAttr = otch.getAttribute("name"); //NOI18N
                                                    if (nameAttr != null) {
                                                        CharSequence value = nameAttr.unquotedValue();
                                                        if (value != null) {
                                                            if (LexerUtils.equals(attributeName, value, true, false)) {
                                                                //we found it
                                                                attrOffset[0] = child.from(); //offset of the attribute tag is fine
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }, ElementType.OPEN_TAG);
                            }
                        }
                    });
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
                jumpOffset = attrOffset[0];
            }
        }

        if (file != null) {
            return new DeclarationLocation(file, jumpOffset);
        }
        return DeclarationLocation.NONE;
    }

    /**
     * Navigation implementation for JSF component defined by @FacesComponent annotation.
     *
     * @param result parser result
     * @param caretOffset carret offset
     * @param lib library which contains given element
     * @return declaration location where to navigate
     */
    static DeclarationLocation goToFacesComponentLibrary(HtmlParserResult htmlresult, int caretOffset, FacesComponentLibrary lib) {
        Element leaf = htmlresult.findByPhysicalRange(caretOffset, true);
        OpenTag openTag = (OpenTag) leaf;
        String tagName = openTag.unqualifiedName().toString();
        LibraryComponent component = lib.getComponent(tagName);
        if (component == null) {
            return DeclarationLocation.NONE;
        }
        if (!(component instanceof FacesLibraryComponent)) {
            return DeclarationLocation.NONE;
        }

        FacesLibraryComponent facesComponent = (FacesLibraryComponent) component;
        FileObject fileObject = htmlresult.getSnapshot().getSource().getFileObject();
        if (fileObject != null) {
            Project project = FileOwnerQuery.getOwner(fileObject);
            if (project != null) {
                FileObject file = facesComponent.getComponentFile(project);
                if (file != null) {
                    return new DeclarationLocation(file, 0);
                }
            }
        }

        return DeclarationLocation.NONE;
    }

    /**
     * Navigation implementation files references.
     *
     * @param htmlresult parser result
     * @param caretOffset carret offset
     * @param tag tag name
     * @param attribute attribute name
     * @param value attribute value
     * @return declaration location where to navigate
     */
    static DeclarationLocation goToReferencedFile(HtmlParserResult htmlresult, int caretOffset, String tag, String attribute, String value) {
        if (tag.isEmpty() || attribute.isEmpty() || value.isEmpty()) {
            return DeclarationLocation.NONE;
        }

        // navigation for value of the ui:include src attribute
        if (tag.contains("include") && "src".equals(attribute)) { //NOI18N
            FileObject fileObject = htmlresult.getSnapshot().getSource().getFileObject().getParent().getFileObject(value);
            if (fileObject != null) {
                return new DeclarationLocation(fileObject, 0);
            }
        }
        return DeclarationLocation.NONE;
    }
}
