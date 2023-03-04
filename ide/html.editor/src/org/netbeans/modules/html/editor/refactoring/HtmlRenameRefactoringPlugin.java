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
package org.netbeans.modules.html.editor.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Position.Bias;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.html.editor.indexing.HtmlFileModel;
import org.netbeans.modules.html.editor.indexing.Entry;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.DependenciesGraph.Node;
import org.netbeans.modules.html.editor.api.index.HtmlIndex;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.web.common.api.FileReference;
import org.netbeans.modules.web.common.api.FileReferenceModification;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class HtmlRenameRefactoringPlugin implements RefactoringPlugin {

    private static final Logger LOGGER = Logger.getLogger(HtmlRenameRefactoringPlugin.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private RenameRefactoring refactoring;

    public HtmlRenameRefactoringPlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
        //no-op
    }

    @Override
    public Problem prepare(final RefactoringElementsBag refactoringElements) {
        //rename refactoring of files refered from html pages
        Lookup lookup = refactoring.getRefactoringSource();
        FileObject file = lookup.lookup(FileObject.class);
        if (file == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return null;
        }
        HtmlIndex index;
        try {
            index = HtmlIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

        ModificationResult modificationResult = new ModificationResult();
        if(file.isFolder()) {
            refactorFolder(file, modificationResult, index);
        } else {
            refactorFile(file, modificationResult, index);
        }
        
        refactoringElements.registerTransaction(new RefactoringCommit(Collections.singletonList(modificationResult)));

        for (FileObject fo : modificationResult.getModifiedFileObjects()) {
            for (Difference diff : modificationResult.getDifferences(fo)) {
                refactoringElements.add(refactoring, DiffElement.create(diff, fo, modificationResult));
            }
        }

        return null;

    }

    private void refactorFolder(FileObject file, ModificationResult modificationResult, HtmlIndex index) {
        LOGGER.fine("refactor folder " + file); //NOI18N
        String newName = refactoring.getNewName();
        try {
            HtmlIndex.AllDependenciesMaps alldeps = index.getAllDependencies();
            Map<FileObject, Collection<FileReference>> source2dest = alldeps.getSource2dest();
            FileObject renamedFolder = file;

            Map<FileObject, HtmlFileModel> modelsCache = new WeakHashMap<>();
            Set<Entry> refactoredReferenceEntries = new HashSet<>();
            //now I need to find out what links go through the given folder
            for (Map.Entry<FileObject, Collection<FileReference>> source2destEntry : source2dest.entrySet()) {
                List<Difference> diffs = new ArrayList<>();
                FileObject source = source2destEntry.getKey();
                Collection<FileReference> destinations = source2destEntry.getValue();
                for (FileReference dest : destinations) {
                    FileReferenceModification modification = dest.createModification();
                    if (modification.rename(renamedFolder, newName)) {
                        //the link is affected, we need to update it
                        //find the css model and the link position in the file
                        HtmlFileModel model = modelsCache.get(source);
                        if (model == null) {
                            try {
                                model = new HtmlFileModel(Source.create(source)); //use file to parse
                                modelsCache.put(source, model);
                            } catch (ParseException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }

                        if (model != null) {
                            //we have the model for source file
                            Collection<? extends Entry> imports = model.getReferences();
                            //XXX the model should contain string representation 2 entry map
                            //linear search :-(
                            for (Entry entry : imports) {
                                if (!refactoredReferenceEntries.contains(entry) && entry.isValidInSourceDocument() && entry.getName().equals(dest.linkPath())) {
                                    //a matching entry found, add the rename refactoring
                                    CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(source);

                                    diffs.add(new Difference(Difference.Kind.CHANGE,
                                            editor.createPositionRef(entry.getDocumentRange().getStart(),
                                            Bias.Forward),
                                            editor.createPositionRef(entry.getDocumentRange().getEnd(),
                                            Bias.Backward),
                                            entry.getName(),
                                            modification.getModifiedReferencePath(),
                                            NbBundle.getMessage(HtmlRenameRefactoringPlugin.class, "MSG_Modify_File_Import"))); //NOI18N

                                    //remember we already renamed this entry, and ignore it next time.
                                    //There might be several references to the same css file,
                                    //so we iterate over the same css model entries several times
                                    refactoredReferenceEntries.add(entry);
                                }
                            }
                        }
                    }
                }
                if(!diffs.isEmpty()) {
                    modificationResult.addDifferences(source, diffs);
                }
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void refactorFile(FileObject file, ModificationResult modificationResult, HtmlIndex index) {
        //refactor a file in explorer
        LOGGER.fine("refactor file " + file.getPath()); //NOI18N
        String newName = refactoring.getNewName();

        //a. get all importing files
        //b. rename the references
        //c. rename the file itself - done via default rename plugin
        DependenciesGraph deps = index.getDependencies(file);
        if(deps == null) {
            return ; //exception should be logged in index.getDependencies()
        }
        Collection<Node> allRefering = deps.getSourceNode().getReferingNodes();

        for (Node ref : allRefering) {
            FileObject refering = ref.getFile();
            try {
                Source source;
                CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(refering);
                //prefer using editor
                //XXX this approach doesn't match the dependencies graph
                //which is made strictly upon the index data
                if (editor != null && editor.isModified()) {
                    source = Source.create(editor.getDocument());
                } else {
                    source = Source.create(refering);
                }

                HtmlFileModel model = new HtmlFileModel(source);

                List<Difference> diffs = new ArrayList<>();
                for (Entry entry : model.getReferences()) {
                    String imp = entry.getName(); //unquoted
                    FileObject resolvedFileObject = WebUtils.resolve(refering, imp);
                    if (resolvedFileObject != null && resolvedFileObject.equals(file)) {
                        //the import refers to me - lets refactor it
                        if (entry.isValidInSourceDocument()) {
                            //new relative path creation
                            String newImport;
                            String extension = file.getExt(); //use the same extension as source file
                            int slashIndex = imp.lastIndexOf('/'); //NOI18N
                            if (slashIndex != -1) {
                                newImport = imp.substring(0, slashIndex) + "/" + newName + "." + extension; //NOI18N
                            } else {
                                newImport = newName + "." + extension; //NOI18N
                            }

                            diffs.add(new Difference(Difference.Kind.CHANGE,
                                    editor.createPositionRef(entry.getDocumentRange().getStart(), Bias.Forward),
                                    editor.createPositionRef(entry.getDocumentRange().getEnd(), Bias.Backward),
                                    entry.getName(),
                                    newImport,
                                    NbBundle.getMessage(HtmlRenameRefactoringPlugin.class, "MSG_Modify_File_Import"))); //NOI18N
                        }
                    }
                }

                if(!diffs.isEmpty()) {
                    modificationResult.addDifferences(refering, diffs);
                }

            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }
}
