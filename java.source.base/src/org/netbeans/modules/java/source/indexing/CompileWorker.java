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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.indexing;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.util.*;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
abstract class CompileWorker {

    abstract ParsingOutput compile(ParsingOutput previous, Context context, JavaParsingContext javaContext, Collection<? extends CompileTuple> files);

    protected void computeFQNs(final Map<JavaFileObject, List<String>> file2FQNs, CompilationUnitTree cut, CompileTuple tuple) {
        String pack;
        if (cut.getPackageName() != null) {
            pack = cut.getPackageName().toString() + "."; //XXX
        } else {
            pack = "";
        }
        String path = tuple.indexable.getRelativePath();
        int i = path.lastIndexOf('.');
        if (i >= 0)
            path = path.substring(0, i);
        path = FileObjects.convertFolder2Package(path);
        List<String> fqns = new LinkedList<String>();
        boolean hasClassesLivingElsewhere = false;
        for (Tree t : cut.getTypeDecls()) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                String fqn = pack + ((ClassTree) t).getSimpleName().toString();
                fqns.add(fqn);
                if (!path.equals(fqn)) {
                    hasClassesLivingElsewhere = true;
                }
            }
        }
        
        if (hasClassesLivingElsewhere) {
            file2FQNs.put(tuple.jfo, fqns);
        }
    }

    protected final boolean isLowMemory(final boolean[] tryToFree) {
        final LowMemoryWatcher lm = LowMemoryWatcher.getInstance();
        boolean ilm = lm.isLowMemory();
        if (ilm && tryToFree != null && tryToFree[0]) {
            lm.free();
            ilm = lm.isLowMemory();
            tryToFree[0] = false;
        }
        return ilm;
    }

    protected final void freeMemory(final boolean freeCaches) {
        final LowMemoryWatcher lm = LowMemoryWatcher.getInstance();
        lm.free(freeCaches);
    }
    
    static class ModuleName {
        String name;
        boolean assigned;
        
        ModuleName(final String name) {
            this.name = name;
        }
    }

    static class ParsingOutput {
        final boolean success;
        final boolean lowMemory;
        final String moduleName;
        final Map<JavaFileObject, List<String>> file2FQNs;
        final Set<ElementHandle<TypeElement>> addedTypes;
        final Set<ElementHandle<ModuleElement>> addedModules;
        final Set<File> createdFiles;
        final Set<Indexable> finishedFiles;
        final Set<ElementHandle<TypeElement>> modifiedTypes;
        final Set<javax.tools.FileObject> aptGenerated;

        private ParsingOutput(
                final boolean success,
                final boolean lowMemory,
                @NullAllowed final String moduleName,
                Map<JavaFileObject, List<String>> file2FQNs,
                Set<ElementHandle<TypeElement>> addedTypes,
                Set<ElementHandle<ModuleElement>> addedModules,
                Set<File> createdFiles,
                Set<Indexable> finishedFiles,
                Set<ElementHandle<TypeElement>> modifiedTypes,
                Set<javax.tools.FileObject> aptGenerated) {
            assert (success && !lowMemory) || !success;
            this.success = success;
            this.lowMemory = lowMemory;
            this.moduleName = moduleName;
            this.file2FQNs = file2FQNs;
            this.addedTypes = addedTypes;
            this.addedModules = addedModules;
            this.createdFiles = createdFiles;
            this.finishedFiles = finishedFiles;
            this.modifiedTypes = modifiedTypes;
            this.aptGenerated = aptGenerated;
        }

        static ParsingOutput success (
                @NullAllowed final String moduleName,
                final Map<JavaFileObject, List<String>> file2FQNs,
                final Set<ElementHandle<TypeElement>> addedTypes,
                final Set<ElementHandle<ModuleElement>> addedModules,
                final Set<File> createdFiles,
                final Set<Indexable> finishedFiles,
                final Set<ElementHandle<TypeElement>> modifiedTypes,
                final Set<javax.tools.FileObject> aptGenerated) {
            return new ParsingOutput(true, false, moduleName, file2FQNs,
                    addedTypes, addedModules, createdFiles, finishedFiles,
                    modifiedTypes, aptGenerated);
        }

        static ParsingOutput failure(
                @NullAllowed final String moduleName,
                final Map<JavaFileObject, List<String>> file2FQNs,
                final Set<ElementHandle<TypeElement>> addedTypes,
                final Set<ElementHandle<ModuleElement>> addedModules,
                final Set<File> createdFiles,
                final Set<Indexable> finishedFiles,
                final Set<ElementHandle<TypeElement>> modifiedTypes,
                final Set<javax.tools.FileObject> aptGenerated) {
            return new ParsingOutput(false, false, moduleName, file2FQNs,
                    addedTypes, addedModules, createdFiles, finishedFiles,
                    modifiedTypes, aptGenerated);
        }

        static ParsingOutput lowMemory(
                @NullAllowed final String moduleName,
                final Map<JavaFileObject, List<String>> file2FQNs,
                final Set<ElementHandle<TypeElement>> addedTypes,
                final Set<ElementHandle<ModuleElement>> addedModules,
                final Set<File> createdFiles,
                final Set<Indexable> finishedFiles,
                final Set<ElementHandle<TypeElement>> modifiedTypes,
                final Set<javax.tools.FileObject> aptGenerated) {
            return new ParsingOutput(false, true, moduleName, file2FQNs,
                    addedTypes, addedModules, createdFiles, finishedFiles,
                    modifiedTypes, aptGenerated);
        }
    }
}
