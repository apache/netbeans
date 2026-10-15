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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Translator of code locations, based on source maps.
 * An instance of this class caches all registered source maps translations in
 * both ways. Do not hold a strong reference to an instance of this class after
 * it's translations are not needed any more.
 * 
 * @author Antoine Vandecreme, Martin Entlicher
 */
final class SourceMapsTranslatorImpl implements SourceMapsTranslator {
    
    private static final Logger LOG = Logger.getLogger(SourceMapsTranslatorImpl.class.getName());
    
    private static final Pattern SOURCE_MAPPING_URL_PATTERN = Pattern.compile("^//[#@]\\s*sourceMappingURL\\s*=\\s*(\\S+)\\s*$");   // NOI18N
    
    private static final DirectMapping NO_MAPPING = new DirectMapping();
    private final Map<FileObject, DirectMapping> directMappings = new HashMap<>();
    private final Map<FileObject, InverseMapping> inverseMappings = new HashMap<>();
    
    public SourceMapsTranslatorImpl() {
    }
    
    @Override
    public boolean registerTranslation(FileObject source, String sourceMapFileName) {
        return registerTranslation(source, sourceMapFileName, null);
    }
    
    @Override
    public boolean registerTranslation(FileObject source, SourceMap sourceMap) {
        return registerTranslation(source, null, sourceMap);
    }
    
    boolean registerTranslation(FileObject source, String sourceMapFileName, SourceMap sm) {
        DirectMapping dm = getMapping(source, sourceMapFileName, sm);
        return dm != NO_MAPPING;
    }
    
    @Override
    public void unregisterTranslation(FileObject source) {
        DirectMapping dm;
        synchronized (directMappings) {
            dm = directMappings.remove(source);
        }
        if (dm != null && dm != NO_MAPPING) {
            synchronized (inverseMappings) {
                for (String src : dm.sourceMap.getSources()) {
                    FileObject fo = dm.parentFolder.getFileObject(src);
                    inverseMappings.remove(fo);
                }
            }
        }
    }
    
    private DirectMapping getMapping(FileObject source, String sourceMapFileName, SourceMap sm) {
        DirectMapping dm;
        synchronized (directMappings) {
            dm = directMappings.get(source);
            if (dm != null) {
                if (sourceMapFileName == null && sm == null) {
                    // return the cached one
                    return dm;
                } else {
                    // load the source map
                    dm = null;
                }
            }
            if (sourceMapFileName == null && sm == null) {
                String lastLine = null;
                try {
                    List<String> lines = source == null ? Collections.emptyList() : source.asLines();
                    if (!lines.isEmpty()) {
                        lastLine = lines.get(lines.size() - 1);
                    }
                    Matcher matcher;
                    if (lastLine != null && (matcher = SOURCE_MAPPING_URL_PATTERN.matcher(lastLine)).matches()) {
                        sourceMapFileName = matcher.group(1);
                    } else {
                        dm = NO_MAPPING;
                    }
                } catch (IOException ioex) {
                    dm = NO_MAPPING;
                }
            }
            if (dm == null) {
                FileObject fo;
                if (sm == null) {
                    File sourceMapFile = new File(sourceMapFileName);
                    if (sourceMapFile.isAbsolute()) {
                        fo = FileUtil.toFileObject(FileUtil.normalizeFile(sourceMapFile));
                    } else {
                        fo = source.getParent().getFileObject(sourceMapFileName);
                    }
                    if (fo == null) {
                        dm = NO_MAPPING;
                    } else {
                        try {
                            sm = SourceMap.parse(fo.asText("UTF-8"));
                        } catch (IOException | IllegalArgumentException ex) {
                            LOG.log(Level.INFO, "Could not read source map "+fo, ex);
                            dm = NO_MAPPING;
                        }
                    }
                }
                if (sm != null) {
                    dm = new DirectMapping(source.getParent(), sm);
                }
            }
            directMappings.put(source, dm);
        }
        if (dm != NO_MAPPING) { // we created new mapping, register the inverse
            synchronized (inverseMappings) {
                for (String src : dm.sourceMap.getSources()) {
                    FileObject fo = dm.parentFolder.getFileObject(src);
                    inverseMappings.put(fo, new InverseMapping(src, source, dm.sourceMap));
                }
            }
        }
        return dm;
    }
    
    /**
     * Translate a location in the compiled file to the location in the source file.
     * Translation is based on a source map retrieved from the compiled file.
     * @param loc location in the compiled file
     * @return corresponding location in the source file, or the original passed
     *         location if the source map is not found, or does not provide the translation.
     */
    @Override
    public Location getSourceLocation(Location loc) {
        return getSourceLocation(loc, null);
    }
    
    /**
     * Translate a location in the compiled file to the location in the source file.
     * Translation is based on the provided source map.
     * @param loc location in the compiled file
     * @param sourceMapFileName file name of the source map file
     * @return corresponding location in the source file, or the original passed
     *         location if the source map does not provide the translation.
     */
    @Override
    public Location getSourceLocation(Location loc, String sourceMapFileName) {
        DirectMapping dm = getMapping(loc.getFile(), sourceMapFileName, null);
        Location mloc = dm.getMappedLocation(loc.getLine(), loc.getColumn());
        if (mloc != null) {
            return mloc;
        } else {
            return loc;
        }
    }
    
    /**
     * Translate a location in the source file to the location in the compiled file.
     * Translation is based on an inverse application of source maps already registered.
     * @param loc location in the source file
     * @return corresponding location in the compiled file, or the original passed
     *         location if no registered source map provides the appropriate translation.
     */
    @Override
    public Location getCompiledLocation(Location loc) {
        InverseMapping im;
        synchronized (inverseMappings) {
            im = inverseMappings.get(loc.getFile());
        }
        if (im == null) {
            return loc;
        }
        Location mloc = im.getMappedLocation(loc.getLine(), loc.getColumn());
        if (mloc != null) {
            return mloc;
        } else {
            return loc;
        }
    }
    
    @Override
    public List<FileObject> getSourceFiles(FileObject compiledFile) {
        DirectMapping dm = getMapping(compiledFile, null, null);
        if (dm.sourceMap == null) {
            return null;
        }
        List<String> sourcePaths = dm.sourceMap.getSources();
        List<FileObject> sourceFiles = new ArrayList<>(sourcePaths.size());
        for (String sp : sourcePaths) {
            FileObject sourceFile = Location.getSourceFile(sp, dm.parentFolder);
            if (sourceFile != null) {
                sourceFiles.add(sourceFile);
            }
        }
        return sourceFiles;
    }
    
    
    private static class DirectMapping {

        private final FileObject parentFolder;
        private final SourceMap sourceMap;
        
        DirectMapping() {
            this.parentFolder = null;
            this.sourceMap = null;
        }

        DirectMapping(FileObject parentFolder, SourceMap sourceMap) {
            this.parentFolder = parentFolder;
            this.sourceMap = sourceMap;
        }
        
        Location getMappedLocation(int line, int column) {
            if (sourceMap == null) {
                return null;
            }
            Mapping mapping = sourceMap.findMapping(line, column);
            if (mapping == null) {
                return null;
            } else {
                return new Location(sourceMap, mapping, parentFolder);
            }
        }
    }
    
    private static class InverseMapping {
        
        private final String sourceName;
        private final FileObject source;
        private final SourceMap sourceMap;

        private InverseMapping(String sourceName, FileObject source, SourceMap sourceMap) {
            this.sourceName = sourceName;
            this.source = source;
            this.sourceMap = sourceMap;
        }

        private Location getMappedLocation(int line, int column) {
            Mapping mapping = sourceMap.findInverseMapping(sourceName, line, column);
            if (mapping == null) {
                return null;
            } else {
                return new Location(source, mapping.getOriginalLine(), mapping.getOriginalColumn());
            }
        }
        
    }
    
}
