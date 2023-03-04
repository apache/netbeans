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
public abstract class CompileWorker {

    protected abstract ParsingOutput compile(ParsingOutput previous, Context context, JavaParsingContext javaContext, Collection<? extends CompileTuple> files);

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
    
    protected static class ModuleName {
        public String name;
        public boolean assigned;
        
        public ModuleName(final String name) {
            this.name = name;
        }
    }

    public static class ParsingOutput {
        public final boolean success;
        public final boolean lowMemory;
        public final String moduleName;
        public final Map<JavaFileObject, List<String>> file2FQNs;
        public final Set<ElementHandle<TypeElement>> addedTypes;
        public final Set<ElementHandle<ModuleElement>> addedModules;
        public final Set<File> createdFiles;
        public final Set<Indexable> finishedFiles;
        public final Set<ElementHandle<TypeElement>> modifiedTypes;
        public final Set<javax.tools.FileObject> aptGenerated;

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

        public static ParsingOutput success (
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

        public static ParsingOutput failure(
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

        public static ParsingOutput lowMemory(
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
