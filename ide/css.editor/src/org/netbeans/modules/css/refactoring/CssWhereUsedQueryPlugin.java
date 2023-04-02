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
import org.netbeans.modules.css.refactoring.api.CssRefactoringExtraInfo;
import org.netbeans.modules.css.refactoring.api.CssRefactoringInfo;
import org.netbeans.modules.css.refactoring.api.CssRefactoringInfo.Type;
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
        CssRefactoringInfo context = lookup.lookup(CssRefactoringInfo.class);

        if (context != null) {
            CssRefactoringInfo econtext = (CssRefactoringInfo) context;
            CssProjectSupport sup = CssProjectSupport.findFor(context.getFileObject());
            if (sup == null) {
                return null;
            }
            CssIndex index = sup.getIndex();

            Type cssType = econtext.getType();

            //find usages of: 
            //1.class or id selector
            //2.hash color
            Collection<FileObject> files;
            ElementKind kind;
            String elementImage = econtext.getElementName();
            RefactoringElementType type;
            switch (cssType) {
                case CLASS:
                    files = index.findClasses(elementImage);
                    kind = ElementKind.CLASS;
                    type = RefactoringElementType.CLASS;
                    break;
                case ID:
                    files = index.findIds(elementImage);
                    kind = ElementKind.ATTRIBUTE;
                    type = RefactoringElementType.ID;
                    break;
                case HEX_COLOR:
                    files = index.findColor(elementImage);
                    kind = ElementKind.FIELD;
                    type = RefactoringElementType.COLOR;
                    break;
                case RESOURCE_IDENTIFIER:
                    {
                        FileObject resolved = WebUtils.resolve(context.getFileObject(), elementImage);
                        if (resolved != null) {
                            refactorFile(resolved, elements);
                        }
                    }
                    return null;
                //fallback if the resourceIdentifier contains URI (no STRING) token
                case URI:
                    {
                        Matcher m = Css3Utils.URI_PATTERN.matcher(elementImage);
                        if (m.matches()) {
                            int groupIndex = 1;
                            String content = m.group(groupIndex);
                            String unquoted = WebUtils.unquotedValue(content);
                            FileObject resolved = WebUtils.resolve(context.getFileObject(), elementImage);
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

        } else {
            FileObject fileObject = refactoring.getRefactoringSource().lookup(FileObject.class);
            if (fileObject != null && fileObject.isData()) {
                //css file where used query
                refactorFile(fileObject, elements);
            }
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
