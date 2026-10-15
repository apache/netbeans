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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Position.Bias;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.refactoring.api.CssRefactoringInfo;
import org.netbeans.modules.css.refactoring.api.CssRefactoringExtraInfo;
import org.netbeans.modules.css.refactoring.api.CssRefactoringInfo.Type;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.html.editor.indexing.Entry;
import org.netbeans.modules.html.editor.indexing.HtmlFileModel;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import static java.util.Arrays.asList;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssRenameRefactoringPlugin implements RefactoringPlugin {

    private static final String SELECTOR_RENAME_MSG_KEY = "MSG_Rename_Selector"; //NOI18N
    private static final String UNRELATED_PREFIX_MSG_KEY = "MSG_Unrelated_Prefix"; //NOI18N

    private static final Logger LOGGER = Logger.getLogger(CssRenameRefactoringPlugin.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private final RenameRefactoring refactoring;
    private final Lookup lookup;
    private final CssRefactoringInfo context;

    public CssRenameRefactoringPlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
        this.lookup = refactoring.getRefactoringSource();
        this.context = lookup.lookup(CssRefactoringInfo.class);
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        String newName = refactoring.getNewName();
        if (newName.length() == 0) {
            return new Problem(true, NbBundle.getMessage(CssRenameRefactoringPlugin.class, "MSG_Error_ElementEmpty")); //NOI18N
        }
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return checkParameters();
    }

    @Override
    public void cancelRequest() {
        //no-op
    }

    @Override
    public Problem prepare(final RefactoringElementsBag refactoringElements) {
        Project project = FileOwnerQuery.getOwner(context.getFileObject());
        if (project == null) {
            return null;
        }
        CssIndex index;
        try {
            index = CssIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        ModificationResult modificationResult = new ModificationResult();

        if (context !=null) {
            //get selected element in the editor
            Type kind = context.getType();
            String elementImage = context.getElementName();

            switch (kind) {
                case CLASS:
                    Collection<FileObject> files = index.findClasses(elementImage);
                    refactor(lookup, modificationResult, RefactoringElementType.CLASS, files, context, index, SELECTOR_RENAME_MSG_KEY);
                    break;
                case ID:
                    files = index.findIds(elementImage);
                    refactor(lookup, modificationResult, RefactoringElementType.ID, files, context, index, SELECTOR_RENAME_MSG_KEY);
                    break;
            }

        }

        //commit the transaction and add the differences to the result
        refactoringElements.registerTransaction(new RefactoringCommit(Collections.singletonList(modificationResult)));
        for (FileObject fo : modificationResult.getModifiedFileObjects()) {
            for (Difference diff : modificationResult.getDifferences(fo)) {
                refactoringElements.add(refactoring, DiffElement.create(diff, fo, modificationResult));
            }
        }

        return null;
    }

    private void refactor(Lookup lookup, ModificationResult modificationResult, RefactoringElementType type, Collection<FileObject> files, CssRefactoringInfo context, CssIndex index, String renameMsgKey) {
        String elementImage = context.getElementName();
        List<FileObject> involvedFiles = new LinkedList<>(files);
        DependenciesGraph deps = index.getDependencies(context.getFileObject());
        Collection<FileObject> relatedFiles = deps.getAllRelatedFiles();

        //refactor all occurances support
        CssRefactoringExtraInfo extraInfo
                = lookup.lookup(CssRefactoringExtraInfo.class);

        //if the "refactor all occurances" checkbox hasn't been
        //selected the occurances must be searched only in the related files
        if (extraInfo == null || !extraInfo.isRefactorAll()) {
            //filter out those files which have no relation with the current file.
            //note: the list of involved files also contains the currently edited file.
            involvedFiles.retainAll(relatedFiles);
            //now we have a list of files which contain the given class or id and are
            //related to the base file
        }

        if (LOG) {
            LOGGER.log(Level.FINE, "Refactoring element {0} in file {1}", new Object[]{elementImage, context.getFileObject().getPath()}); //NOI18N
            LOGGER.log(Level.FINE, "Involved files declaring the element {0}:", elementImage); //NOI18N
            for (FileObject fo : involvedFiles) {
                LOGGER.log(Level.FINE, "{0}\n", fo.getPath()); //NOI18N
            }
        }

        String newName = refactoring.getNewName();
        //make css simple models for all involved files
        //where we already have the result
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
                Collection<Entry> entries;

                if(RefactoringElementType.ID == type && model.getIds().containsKey(elementImage)) {
                    entries = asList(model.getIds().get(elementImage));
                } else if (RefactoringElementType.CLASS == type && model.getCssClasses().containsKey(elementImage)) {
                    entries = model.getCssClasses().get(elementImage);
                } else {
                    entries = Collections.emptyList();
                }

                boolean related = relatedFiles.contains(file);

                List<Difference> diffs = new ArrayList<>();
                for (Entry entry : entries) {
                    if (entry.isValidInSourceDocument()) {
                        diffs.add(new Difference(Difference.Kind.CHANGE,
                                editor.createPositionRef(entry.getDocumentRange().getStart(), Bias.Forward),
                                editor.createPositionRef(entry.getDocumentRange().getEnd(), Bias.Backward),
                                entry.getName(),
                                newName,
                                related
                                ? NbBundle.getMessage(CssRenameRefactoringPlugin.class, renameMsgKey)
                                : NbBundle.getMessage(CssRenameRefactoringPlugin.class, UNRELATED_PREFIX_MSG_KEY) + " "
                                + NbBundle.getMessage(CssRenameRefactoringPlugin.class, renameMsgKey))); //NOI18N
                    }
                }
                if (!diffs.isEmpty()) {
                    modificationResult.addDifferences(file, diffs);
                }

            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
