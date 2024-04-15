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

package org.netbeans.modules.web.common.sourcemap;

import java.io.File;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Translator of code locations, based on source maps.
 * 
 * @author Antoine Vandecreme, Martin Entlicher
 */
public interface SourceMapsTranslator {
    
    /**
     * Create a default implementation of source maps translator.
     * It caches all registered source maps translations in both ways.
     * Do not hold a strong reference to to this translator after
     * it's translations are not needed any more.
     * @return A new default implementation of source maps translator.
     */
    public static SourceMapsTranslator create() {
        return new SourceMapsTranslatorImpl();
    }
    
    /**
     * Register a new source map.
     * @param source the generated file source
     * @param sourceMapFileName file name of the associated source map file
     * @return <code>true</code> when the mapping was successfully parsed and registered,
     *         <code>false</code> otherwise.
     */
    public boolean registerTranslation(FileObject source, String sourceMapFileName);
    
    /**
     * Register a new source map.
     * @param source the generated file source
     * @param sourceMap the associated source map
     * @return <code>true</code> when the mapping was successfully registered,
     *         <code>false</code> otherwise.
     */
    public boolean registerTranslation(FileObject source, SourceMap sourceMap);
    
    /**
     * Unregister a translation for the given file.
     * @param source the generated file source for which the translation is to be removed.
     */
    public void unregisterTranslation(FileObject source);
    
    /**
     * Translate a location in the compiled file to the location in the source file.
     * Translation is based on a source map retrieved from the compiled file.
     * @param loc location in the compiled file
     * @return corresponding location in the source file, or the original passed
     *         location if the source map is not found, or does not provide the translation.
     */
    public Location getSourceLocation(Location loc);
    
    /**
     * Translate a location in the compiled file to the location in the source file.
     * Translation is based on the provided source map.
     * @param loc location in the compiled file
     * @param sourceMapFileName file name of the source map file
     * @return corresponding location in the source file, or the original passed
     *         location if the source map does not provide the translation.
     */
    public Location getSourceLocation(Location loc, String sourceMapFileName);
    
    /**
     * Translate a location in the source file to the location in the compiled file.
     * Translation is based on an inverse application of source maps already registered.
     * @param loc location in the source file
     * @return corresponding location in the compiled file, or the original passed
     *         location if no registered source map provides the appropriate translation.
     */
    public Location getCompiledLocation(Location loc);
    
    /**
     * Get a list of source files that were compiled into the given file.
     * @param compiledFile The compiled (generated) file
     * @return a list of source files, or <code>null</code> when no mapping was found.
     */
    public List<FileObject> getSourceFiles(FileObject compiledFile);
    
    public static final class Location {
        
        private FileObject file;
        private int line;
        private int column;
        
        private SourceMap sourceMap;
        private Mapping mapping;
        private FileObject parentFolder;
        
        public Location(FileObject file, int line, int column) {
            this.file = file;
            this.line = line;
            this.column = column;
        }
        
        Location(SourceMap sourceMap, Mapping mapping, FileObject parentFolder) {
            this.sourceMap = sourceMap;
            this.mapping = mapping;
            this.parentFolder = parentFolder;
            this.line = -1;
            this.column = -1;
        }
        
        static FileObject getSourceFile(String sourcePath, FileObject parentFolder) {
            File sourceFile = new File(sourcePath);
            FileObject fo;
            if (sourceFile.isAbsolute()) {
                fo = FileUtil.toFileObject(FileUtil.normalizeFile(sourceFile));
            } else {
                fo = parentFolder.getFileObject(sourcePath);
            }
            return fo;
        }
        
        public FileObject getFile() {
            if (file == null && parentFolder != null && mapping != null) {
                int sourceIndex = mapping.getSourceIndex();
                String sourcePath = sourceMap.getSourcePath(sourceIndex);
                file = getSourceFile(sourcePath, parentFolder);
            }
            return file;
        }
        
        public int getLine() {
            if (line < 0) {
                line = mapping.getOriginalLine();
            }
            return line;
        }
        
        public int getColumn() {
            if (column < 0 && mapping != null) {
                column = mapping.getOriginalColumn();
            }
            return column;
        }
        
        /**
         * Get the name at this location.
         * @return The name or <code>null</code> when no name is available.
         */
        public String getName() {
            if (sourceMap != null && mapping != null && mapping.getNameIndex() >= 0) {
                return sourceMap.getName(mapping.getNameIndex());
            } else {
                return null;
            }
        }
    }
    
}
