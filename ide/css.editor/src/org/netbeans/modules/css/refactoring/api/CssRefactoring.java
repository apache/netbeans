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
package org.netbeans.modules.css.refactoring.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.css.editor.CssProjectSupport;
import org.netbeans.modules.css.indexing.CssFileModel;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;

/**
 * @author mfukala@netbeans.org
 */
public class CssRefactoring {

    private CssRefactoring() {
    }

    public static Collection<Entry> getAllSelectors(FileObject file, RefactoringElementType type) {
        try {
            return CssFileModel.create(Source.create(file)).get(type);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static Collection<FileObject> findAllStyleSheets(FileObject baseFile) {
        CssProjectSupport sup = CssProjectSupport.findFor(baseFile);
        if(sup != null) {
            CssIndex index = sup.getIndex();
            return index.getAllIndexedFiles();
        }
        return Collections.emptyList();
    }

    public static Map<FileObject, Collection<EntryHandle>> findAllOccurances(String elementName, RefactoringElementType type, FileObject baseFile, boolean nonVirtualOnly) {
        CssProjectSupport sup = CssProjectSupport.findFor(baseFile);
        if (sup == null) {
            return null;
        }
        CssIndex index = sup.getIndex();
        DependenciesGraph deps = index.getDependencies(baseFile);
        Collection<FileObject> relatedFiles = deps.getAllReferedFiles();
        Collection<FileObject> queryResult = index.find(type, elementName);
        Map<FileObject, Collection<EntryHandle>> result = new HashMap<>();

        for (FileObject file : queryResult) {
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
                Collection<Entry> modelEntries = model.get(type);

                Collection<EntryHandle> entries = result.get(file);
                if (entries == null) {
                    entries = new ArrayList<>();
                    result.put(file, entries);
                }

                for (Entry entry : modelEntries) {
                    if (elementName.equals(entry.getName())) {
                        if (entry.isValidInSourceDocument()) {
                            if (nonVirtualOnly && !entry.isVirtual()) {
                                entries.add(EntryHandle.createEntryHandle(entry, relatedFiles.contains(file)));
                            }
                        }
                    }
                }

            } catch (ParseException e) {
                Exceptions.printStackTrace(e);
            }
        }

        return result;

    }
}
