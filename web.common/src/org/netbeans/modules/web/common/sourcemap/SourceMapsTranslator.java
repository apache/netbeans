/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
            if (file == null) {
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
