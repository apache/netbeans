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
package org.netbeans.modules.javascript2.debug;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.modules.javascript2.debug.spi.SourceElementsQuery;
import org.netbeans.modules.javascript2.debug.spi.SourceElementsQuery.Var;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Translator of names in a source file, based on a source map.
 * 
 * @author Martin Entlicher
 */
public final class NamesTranslator {

    private static final boolean USE_SOURCE_MAPS
            = Boolean.parseBoolean(System.getProperty("javascript.debugger.useSourceMaps", "true"));

    private final SourceMapsTranslator smt;
    private final FileObject fileObject;
    private final Source source;
    private final int offset;

    private String declarationNodeName;
    private final Map<String, String> directMap = new HashMap<>();
    private final Map<String, String> reverseMap = new HashMap<>();
    private boolean varTranslationsRegistered;

    /**
     * Create a names translator.
     * @param smt translator handling the source maps.
     * @param fileObject the generated source file
     * @param lineNumber line number of the position at which we translate the names
     * @param columnNumber column number of the position at which we translate the names
     * @return an instance of names translator, or <code>null</code> when the corresponding source can not be loaded.
     */
    public static NamesTranslator create(SourceMapsTranslator smt, FileObject fileObject,
                                         int lineNumber, int columnNumber) {
        if (!USE_SOURCE_MAPS) {
            return null;
        }
        Source source = Source.create(fileObject);
        if (source == null) {
            return null;
        }
        Document doc = source.getDocument(true);
        if (doc == null) {
            return null;
        }
        // Check lineNumber:
        
        try {
            int lastLine = LineDocumentUtils.getLineIndex((LineDocument) doc, doc.getLength()-1);
            if (lineNumber > lastLine) {
                lineNumber = lastLine;
            }
        } catch (BadLocationException blex) {}
        int offset = LineDocumentUtils.getLineStartFromIndex((LineDocument) doc, lineNumber) + columnNumber;

        return new NamesTranslator(smt, fileObject, source, offset);
    }

    private NamesTranslator(SourceMapsTranslator smt, FileObject fileObject,
                            Source source, int offset) {
        this.smt = smt;
        this.fileObject = fileObject;
        this.source = source;
        this.offset = offset;
    }

    public synchronized String translate(final String name) {
        registerVarTranslations();
        String tname = directMap.get(name);
        if (tname != null) {
            return tname;
        } else {
            return name;
        }
    }

    public synchronized String reverseTranslate(String name) {
        registerVarTranslations();
        String tname = reverseMap.get(name);
        if (tname != null) {
            return tname;
        } else {
            return name;
        }
    }

    private void registerVarTranslations() {
        assert Thread.holdsLock(this);
        if (varTranslationsRegistered) {
            return;
        }
        varTranslationsRegistered = true;
        SourceElementsQuery seq = Lookup.getDefault().lookup(SourceElementsQuery.class);
        if (seq != null) {
            Collection<Var> vars = seq.getVarsAt(source, offset);
            for (Var var : vars) {
                int voffset = var.getOffset();
                Document doc = source.getDocument(true);
                int line;
                try {
                    line = LineDocumentUtils.getLineIndex((LineDocument) doc, voffset);
                } catch (BadLocationException blex) {
                    continue;
                }
                //int column = NbDocument.findLineColumn((StyledDocument) doc, voffset);
                int column = voffset - LineDocumentUtils.getLineStart((LineDocument) doc, voffset);
                SourceMapsTranslator.Location loc = new SourceMapsTranslator.Location(fileObject, line, column);
                loc = smt.getSourceLocation(loc);
                String tname = loc.getName();
                if (tname != null) {
                    String name = var.getName();
                    directMap.put(name, tname);
                    reverseMap.put(tname, name);
                }
            }
        }
    }

    public synchronized String translateDeclarationNodeName(String defaultName) {
        if (declarationNodeName == null) {
            String nodeName;
            SourceElementsQuery seq = Lookup.getDefault().lookup(SourceElementsQuery.class);
            if (seq != null) {
                int doffset = seq.getObjectOffsetAt(source, offset);
                if (doffset >= 0) {
                    Document doc = source.getDocument(true);
                    try {
                        int line = LineDocumentUtils.getLineIndex((LineDocument) doc, doffset);
                        int column = doffset - LineDocumentUtils.getLineStart((LineDocument) doc, doffset);
                        SourceMapsTranslator.Location loc = new SourceMapsTranslator.Location(fileObject, line, column);
                        loc = smt.getSourceLocation(loc);
                        nodeName = loc.getName();
                        if (nodeName == null) {
                            nodeName = defaultName;
                        }
                    } catch (BadLocationException blex) {
                        nodeName = defaultName;
                    }
                } else {
                    nodeName = defaultName;
                }
            } else {
                nodeName = defaultName;
            }
            declarationNodeName = nodeName;
        }
        return declarationNodeName;
    }
}
