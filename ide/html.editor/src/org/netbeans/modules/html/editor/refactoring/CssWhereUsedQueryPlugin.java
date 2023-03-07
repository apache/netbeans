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
package org.netbeans.modules.html.editor.refactoring;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.refactoring.api.CssRefactoringInfo;
import org.netbeans.modules.css.refactoring.api.CssRefactoringExtraInfo;
import org.netbeans.modules.css.refactoring.api.CssRefactoringInfo.Type;
import org.netbeans.modules.html.editor.indexing.Entry;
import org.netbeans.modules.html.editor.indexing.HtmlFileModel;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

import static java.util.Arrays.asList;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssWhereUsedQueryPlugin implements RefactoringPlugin {

    private static final Logger LOG = Logger.getLogger(CssWhereUsedQueryPlugin.class.getName());

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
    public Problem prepare(final RefactoringElementsBag refactoringElements) {
        if (cancelled) {
            return null;
        }

        Lookup lookup = refactoring.getRefactoringSource();
        CssRefactoringInfo context = lookup.lookup(CssRefactoringInfo.class);

        if (context != null) {
            Project containingProject = FileOwnerQuery.getOwner(context.getFileObject());

            if(containingProject == null) {
                return null;
            }

            CssIndex index = null;
            try {
                index = CssIndex.get(containingProject);
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Failed to open CssIndex for: " + containingProject, ex);
            }
            if (index == null) {
                return null;
            }

            Type cssType = context.getType();

            if(!(cssType == Type.CLASS || cssType == Type.ID)) {
                return null;
            }

            //find usages of: class or id selector
            Collection<FileObject> files;
            ElementKind kind;
            String elementImage = context.getElementName();

            switch (cssType) {
                case CLASS:
                    files = index.findClasses(elementImage);
                    kind = ElementKind.CLASS;
                    break;
                case ID:
                    files = index.findIds(elementImage);
                    kind = ElementKind.ATTRIBUTE;
                    break;
                default:
                    //cannot happen
                    files = null;
                    kind = null;
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

                    HtmlFileModel model = new HtmlFileModel(source);

                    List<Entry> entries = null;

                    if (Type.CLASS == cssType) {
                        entries = model.getCssClasses().get(elementImage);
                    } else if (Type.ID == cssType) {
                        Entry e = model.getIds().get(elementImage);
                        entries = e == null ? null : asList(e);
                    }

                    if(entries == null) {
                        continue;
                    }

                    boolean related = relatedFiles.contains(file);

                    for (Entry e : entries) {
                        refactoringElements.add(refactoring, WhereUsedElement.create(file, e, kind, related));
                    }
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        return null;
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
