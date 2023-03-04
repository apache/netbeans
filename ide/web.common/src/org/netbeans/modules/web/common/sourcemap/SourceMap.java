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
package org.netbeans.modules.web.common.sourcemap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * Source map, lines and columns are 0-based.
 *
 * @author Jan Stola, Martin Entlicher, Antoine Vandecreme
 */
public class SourceMap {
    /** Source map version supported by this class. */
    private static final String SUPPORTED_VERSION = "3"; // NOI18N
    private static final String INVALIDATING_STRING = ")]}";    // NOI18N
    /** Cache of parsed source maps. Maps the text of the map to the map itself. */
    private static final Map<String,SourceMap> cache = new WeakHashMap<String,SourceMap>();
    /** JSON representation of this source map. */
    private JSONObject sourceMap;
    /**
     * Mapping provided by this source map. The key is the line number
     * and the value is the list of mappings related to the line. The list
     * is ordered according to the increasing column.
     */
    private final Map<Integer,List<Mapping>> mappings = new HashMap<>();
    private final Map<String, Map<Integer,List<Mapping>>> inverseMappings = new HashMap<>();
    private final String sourceRoot;

    /**
     * Parses the given text representation of a source map.
     * 
     * @param sourceMap text representation of a source map.
     * @return parsed source map.
     * @throws IllegalArgumentException when the parsing fails.
     */
    public static SourceMap parse(String sourceMap) throws IllegalArgumentException {
        SourceMap map = cache.get(sourceMap);
        if (map == null) {
            map = new SourceMap(sourceMap);
            cache.put(sourceMap, map);
        }
        return map;
    }
    
    private static String removeInvalidatingString(String sourceMap) {
        if (sourceMap.startsWith(INVALIDATING_STRING)) {
            int firstLineEnd = sourceMap.indexOf('\n');
            if (firstLineEnd > 0) {
                return sourceMap.substring(firstLineEnd + 1);
            }
        }
        return sourceMap;
    }

    /**
     * Creates a new {@code SourceMap}.
     * 
     * @param sourceMap {@code String} representation of the source map.
     */
    private SourceMap(String sourceMap) {
        this(toJSONObject(removeInvalidatingString(sourceMap)));
    }

    /**
     * Creates a new {@code SourceMap}.
     * 
     * @param sourceMap JSON representation of the source map.
     */
    private SourceMap(JSONObject sourceMap) {
        this.sourceMap = sourceMap;
        Object versionValue = sourceMap.get("version"); // NOI18N
        String version = (versionValue == null) ? null : versionValue.toString();
        if (!SUPPORTED_VERSION.equals(version)) {
            throw new IllegalArgumentException("Unsupported version of the source map: " + version); // NOI18N
        }
        String mappingInfo = (String)sourceMap.get("mappings"); // NOI18N
        MappingTokenizer tokenizer = new MappingTokenizer(mappingInfo);
        int line = 0;
        List<Mapping> lineInfo = null;
        for (Mapping mapping : tokenizer) {
            if (mapping == Mapping.NEW_LINE) {
                if (lineInfo != null) {
                    mappings.put(line, lineInfo);
                    lineInfo = null;
                }
                line++;
            } else {
                if (lineInfo == null) {
                    lineInfo = new ArrayList<Mapping>();
                } else {
                    // Check the last mapping
                    Mapping lastMapping = lineInfo.get(lineInfo.size() - 1);
                    if (lastMapping.getColumn() == mapping.getColumn()) {
                        // Identical position, ignore.
                        continue;
                    }
                }
                lineInfo.add(mapping);
                registerInverseMapping(line, mapping);
            }
        }
        if (lineInfo != null) {
            mappings.put(line, lineInfo);
        }
        sourceRoot = (String) sourceMap.get("sourceRoot"); // NOI18N
    }
    
    private void registerInverseMapping(int line, Mapping mapping) {
        String sourcePath = getSourcePath(mapping.getSourceIndex());
        Map<Integer, List<Mapping>> imm = inverseMappings.get(sourcePath);
        if (imm == null) {
            imm = new HashMap<>();
            inverseMappings.put(sourcePath, imm);
        }
        int origLine = mapping.getOriginalLine();
        int origColumn = mapping.getOriginalColumn();
        List<Mapping> ims = imm.get(origLine);
        if (ims == null) {
            ims = new ArrayList<>(6);   // Typically, there's less columns in the inverse mapping
            imm.put(origLine, ims);
        }
        // Is the mapping there already?
        int index = binarySearch(ims, origColumn);
        if (index >= 0) {
            return ;
        }
        index = -index - 1;
        //if (index >= ims.size()) {
        //    index--;
        //}
        Mapping im = new Mapping();
        im.setOriginalLine(line);
        im.setColumn(origColumn);
        im.setOriginalColumn(mapping.getColumn());
        ims.add(index, im);
    }
    
    /**
     * A name of the generated file, that this source map is associated with. Can return <code>null</code>.
     * @return a name of the generated file, or <code>null</code>.
     */
    public String getFile() {
        return (String) sourceMap.get("file");
    }
    
    /**
     * A list of original source file names used by this map.
     * @return A list of original source file names.
     */
    public List<String> getSources() {
        JSONArray sources = (JSONArray)sourceMap.get("sources"); // NOI18N
        return Collections.unmodifiableList(sources);
    }

    /**
     * Returns path of the source with the specified index. The path is relative
     * to the location of this source map.
     * 
     * @param sourceIndex source index.
     * @return path of the source with the specified index.
     */
    public String getSourcePath(int sourceIndex) {
        JSONArray sources = (JSONArray)sourceMap.get("sources"); // NOI18N
        String source = (String)sources.get(sourceIndex);
        if (sourceRoot != null && !sourceRoot.isEmpty()) {
            return sourceRoot + File.separator + source;
        } else {
            return source;
        }
    }
    
    /**
     * Returns name at the specified index.
     * @param nameIndex name index.
     * @return name at the specified index.
     */
    public String getName(int nameIndex) {
        JSONArray names = (JSONArray)sourceMap.get("names"); // NOI18N
        return (String)names.get(nameIndex);
    }

    /**
     * Returns the mapping that corresponds to the given line and column.
     * 
     * @param line line of the location.
     * @param column column of the location.
     * @return mapping that corresponds to the given line and column
     * or {@code null} if there is no mapping known for the given position.
     */
    public Mapping findMapping(int line, int column) {
        Mapping result = null;
        List<Mapping> lineInfo = mappings.get(line);
        if (lineInfo != null) {
            int index = binarySearch(lineInfo, column);
            if (index < 0) {
                index = -index - 2;
                if (index < 0) {
                    index = 0;  // Return the first known
                }
            }
            return lineInfo.get(index);
        }
        return result;
    }

    /**
     * Returns the first mapping for the specified line.
     * 
     * @param line line we are interested in.
     * @return first mapping for the specified line.
     */
    public Mapping findMapping(int line) {
        Mapping result = null;
        List<Mapping> lineInfo = mappings.get(line);
        if (lineInfo != null && !lineInfo.isEmpty()) {
            result = lineInfo.get(0);
        }
        return result;
    }
    
    /** For tests only. */
    List<Mapping> findAllMappings(int line) {
        return mappings.get(line);
    }

    public Mapping findInverseMapping(String sourcePath, int originalLine, int originalColumn) {
        Mapping result = null;
        Map<Integer, List<Mapping>> imm = inverseMappings.get(sourcePath);
        if (imm != null) {
            List<Mapping> lineInfo = imm.get(originalLine);
            if (lineInfo != null) {
                int index = binarySearch(lineInfo, originalColumn);
                if (index < 0) {
                    index = -index - 2;
                    if (index < 0) {
                        index = 0;  // Return the first known
                    }
                }
                return lineInfo.get(index);
            }
        }
        return result;
    }

    public Mapping findInverseMapping(String sourcePath, int originalLine) {
        Mapping result = null;
        Map<Integer, List<Mapping>> imm = inverseMappings.get(sourcePath);
        if (imm != null) {
            List<Mapping> lineInfo = imm.get(originalLine);
            if (lineInfo != null&& !lineInfo.isEmpty()) {
                result = lineInfo.get(0);
            }
        }
        return result;
    }
        
    /** For tests only. */
    List<Mapping> findAllInverseMappings(String sourcePath, int line) {
        Map<Integer, List<Mapping>> imm = inverseMappings.get(sourcePath);
        if (imm != null) {
            return imm.get(line);
        } else {
            return null;
        }
    }

    /**
     * Parses the given {@code text} and returns the corresponding JSON object.
     * 
     * @param text text to parse.
     * @return JSON object that corresponds to the given text.
     * @throws IllegalArgumentException when the given text is not a valid
     * representation of a JSON object.
     */
    private static JSONObject toJSONObject(String text) throws IllegalArgumentException {
        try {
            JSONObject json = (JSONObject)JSONValue.parseWithException(text);
            return json;
        } catch (ParseException ex) {
            throw new IllegalArgumentException(text);
        }
    }
    
    private static int binarySearch(List<Mapping> mappings, int column) {
        int i1 = 0;
        int i2 = mappings.size()-1;
        while (i1 <= i2) {
            int i = (i1 + i2) >>> 1;
            Mapping middle = mappings.get(i);
            int mc = middle.getColumn();
            if (mc == column) {
                return i;
            }
            if (mc < column) {
                i1 = i + 1;
            } else if (mc > column) {
                i2 = i - 1;
            } else {
                return i;
            }
        }
        return -i1 - 1;
    }
    
    private static boolean sortedAdd(Mapping m, List<Mapping> mappings, Comparator<Mapping> cmp) {
        int pos = binarySearch(mappings, m.getColumn());
        if (pos >= 0) {
            return false;
        }
        mappings.add(-pos, m);
        return true;
    }
    
}
