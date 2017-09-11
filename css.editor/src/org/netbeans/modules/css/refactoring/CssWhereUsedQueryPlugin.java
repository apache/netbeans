/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.css.refactoring;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.CssProjectSupport;
import org.netbeans.modules.css.indexing.CssFileModel;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.refactoring.api.Entry;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssWhereUsedQueryPlugin implements RefactoringPlugin {

    private final WhereUsedQuery refactoring;
    private boolean cancelled = false;

    /**
     * Creates a new instance of WhereUsedQuery
     */
    public CssWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem prepare(final RefactoringElementsBag elements) {
        if (cancelled) {
            return null;
        }

        Lookup lookup = refactoring.getRefactoringSource();
        CssElementContext context = lookup.lookup(CssElementContext.class);

        if (context instanceof CssElementContext.Editor) {
            CssElementContext.Editor econtext = (CssElementContext.Editor) context;
            CssProjectSupport sup = CssProjectSupport.findFor(context.getFileObject());
            if (sup == null) {
                return null;
            }
            CssIndex index = sup.getIndex();

            Node element = econtext.getElement();

            //find usages of: 
            //1.class or id selector
            //2.hash color
            Collection<FileObject> files;
            ElementKind kind;
            String elementImage = econtext.getElementName();
            RefactoringElementType type;
            switch (element.type()) {
                case cssClass:
                    elementImage = elementImage.substring(1); //cut off the dot
                    files = index.findClasses(elementImage);
                    kind = ElementKind.CLASS;
                    type = RefactoringElementType.CLASS;
                    break;
                case cssId:
                    elementImage = elementImage.substring(1); //cut off the hash
                    files = index.findIds(elementImage);
                    kind = ElementKind.ATTRIBUTE;
                    type = RefactoringElementType.ID;
                    break;
                case hexColor:
                    files = index.findColor(elementImage);
                    kind = ElementKind.FIELD;
                    type = RefactoringElementType.COLOR;
                    break;
                case resourceIdentifier:
                    Node token = NodeUtil.getChildTokenNode(element, CssTokenId.STRING);
                    if (token != null) {
                        String unquoted = WebUtils.unquotedValue(token.image());
                        FileObject resolved = WebUtils.resolve(context.getFileObject(), unquoted);
                        if (resolved != null) {
                            refactorFile(resolved, elements);
                        }
                        return null;
                    }
                //fallback if the resourceIdentifier contains URI (no STRING) token
                case term:
                    //uri in term
                    token = NodeUtil.getChildTokenNode(element, CssTokenId.URI);
                    if (token != null) {
                        CharSequence image = token.image();
                        Matcher m = Css3Utils.URI_PATTERN.matcher(image);
                        if (m.matches()) {
                            int groupIndex = 1;
                            String content = m.group(groupIndex);
                            String unquoted = WebUtils.unquotedValue(content);
                            FileObject resolved = WebUtils.resolve(context.getFileObject(), unquoted);
                            if (resolved != null) {
                                refactorFile(resolved, elements);
                            }
                        }
                    }
                    return null;

                default:
                    //cannot happen
                    files = null;
                    kind = null;
                    type = null;
            }

            List<FileObject> involvedFiles = new LinkedList<>(files);
            DependenciesGraph deps = index.getDependencies(context.getFileObject());
            Collection<FileObject> relatedFiles = deps.getAllRelatedFiles();

            //refactor all occurances support
            CssRefactoringExtraInfo extraInfo
                    = lookup.lookup(CssRefactoringExtraInfo.class);

            if (extraInfo == null || !extraInfo.isRefactorAll()) {
                //if the "refactor all occurances" checkbox hasn't been
                //selected the occurances must be searched only in the related files

                //filter out those files which have no relation with the current file.
                //note: the list of involved files also contains the currently edited file.
                involvedFiles.retainAll(relatedFiles);
                //now we have a list of files which contain the given class or id and are
                //related to the base file
            }

            for (FileObject file : involvedFiles) {
                try {
                    Source source;
                    CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(file);
                    //prefer using editor
                    //XXX this approach doesn't match the dependencies graph
                    //which is made strictly upon the index data
                    if (editor != null && editor.isModified()) {
                        source = Source.create(editor.getDocument());
                    } else {
                        source = Source.create(file);
                    }

                    CssFileModel model = CssFileModel.create(source);
                    Collection<Entry> entries = model.get(type);

                    boolean related = relatedFiles.contains(file);
                    for (Entry entry : entries) {
                        if (entry.isValidInSourceDocument()
                                && LexerUtils.equals(elementImage, entry.getName(), type == RefactoringElementType.COLOR, false)) {
                            WhereUsedElement elem = WhereUsedElement.create(file, entry, kind, related);
                            elements.add(refactoring, elem);
                        }
                    }

                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        } else if (context instanceof CssElementContext.File) {
            //css file where used query
            refactorFile(context.getFileObject(), elements);
        }

        return null;
    }

    private void refactorFile(FileObject base, RefactoringElementsBag elements) {
        CssProjectSupport sup = CssProjectSupport.findFor(base);
        if (sup == null) {
            return;
        }

        CssIndex index = sup.getIndex();
        DependenciesGraph deps = index.getDependencies(base);

        //find only files directly importing the base file
        String baseFileName = base.getNameExt();
        for (org.netbeans.modules.web.common.api.DependenciesGraph.Node referingNode : deps.getSourceNode().getReferingNodes()) {
            try {
                FileObject file = referingNode.getFile();
                CssFileModel model = CssFileModel.create(Source.create(file));
                Collection<Entry> imports = model.getImports();
                //find the import of the base file
                for (Entry e : imports) {
                    if (e.isValidInSourceDocument() && e.getName().indexOf(baseFileName) != -1) {
                        //found
                        WhereUsedElement elem = WhereUsedElement.create(file, e, ElementKind.FILE);
                        elements.add(refactoring, elem);
                    }
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
        cancelled = true;
    }
}
