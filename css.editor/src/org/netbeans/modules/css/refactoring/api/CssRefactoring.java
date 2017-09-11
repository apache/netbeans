/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
