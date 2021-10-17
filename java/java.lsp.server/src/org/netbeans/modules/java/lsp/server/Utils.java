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
package org.netbeans.modules.java.lsp.server;

import com.google.gson.stream.JsonWriter;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.lang.model.element.ElementKind;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.Places;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class Utils {

    public static SymbolKind elementKind2SymbolKind(ElementKind kind) {
        switch (kind) {
            case PACKAGE:
                return SymbolKind.Package;
            case ENUM:
                return SymbolKind.Enum;
            case CLASS:
                return SymbolKind.Class;
            case ANNOTATION_TYPE:
                return SymbolKind.Interface;
            case INTERFACE:
                return SymbolKind.Interface;
            case ENUM_CONSTANT:
                return SymbolKind.EnumMember;
            case FIELD:
                return SymbolKind.Field; //TODO: constant
            case PARAMETER:
                return SymbolKind.Variable;
            case LOCAL_VARIABLE:
                return SymbolKind.Variable;
            case EXCEPTION_PARAMETER:
                return SymbolKind.Variable;
            case METHOD:
                return SymbolKind.Method;
            case CONSTRUCTOR:
                return SymbolKind.Constructor;
            case TYPE_PARAMETER:
                return SymbolKind.TypeParameter;
            case RESOURCE_VARIABLE:
                return SymbolKind.Variable;
            case MODULE:
                return SymbolKind.Module;
            case STATIC_INIT:
            case INSTANCE_INIT:
            case OTHER:
            default:
                return SymbolKind.File; //XXX: what here?
        }
    }

    public static Range treeRange(CompilationInfo info, Tree tree) {
        long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);
        long end   = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree);
        if (end == (-1)) {
            end = start;
        }
        return new Range(createPosition(info.getCompilationUnit(), (int) start),
                         createPosition(info.getCompilationUnit(), (int) end));
    }

    public static Position createPosition(CompilationUnitTree cut, int offset) {
        return createPosition(cut.getLineMap(), offset);
    }

    public static Position createPosition(LineMap lm, int offset) {
        return new Position((int) lm.getLineNumber(offset) - 1,
                            (int) lm.getColumnNumber(offset) - 1);
    }

    public static Position createPosition(FileObject file, int offset) {
        try {
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            StyledDocument doc = ec.openDocument();
            int line = NbDocument.findLineNumber(doc, offset);
            int column = NbDocument.findLineColumn(doc, offset);

            return new Position(line, column);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static int getOffset(LineDocument doc, Position pos) {
        return LineDocumentUtils.getLineStartFromIndex(doc, pos.getLine()) + pos.getCharacter();
    }

    public static synchronized String toUri(FileObject file) {
        return URITranslator.getDefault().uriToLSP(file.toURI().toString());
    }

    public static synchronized FileObject fromUri(String uri) throws MalformedURLException {
        uri = URITranslator.getDefault().uriFromLSP(uri);
        return URLMapper.findFileObject(URI.create(uri).toURL());
    }

    private static final char[] SNIPPET_ESCAPE_CHARS = new char[] { '\\', '$', '}' };
    /**
     * Escape special characters in a completion snippet. Characters '$' and '}'
     * are escaped via backslash.
     */
    public static String escapeCompletionSnippetSpecialChars(String text) {
        if (text.isEmpty()) {
            return text;
        }
        for (char c : SNIPPET_ESCAPE_CHARS) {
            StringBuilder replaced = null;
            int lastPos = 0;
            int i = 0;
            while ((i = text.indexOf(c, i)) >= 0) {
                if (replaced == null) {
                    replaced = new StringBuilder(text.length() + 5); // Text length + some escapes
                }
                replaced.append(text.substring(lastPos, i));
                replaced.append('\\');
                lastPos = i;
                i += 1;
            }
            if (replaced != null) {
                replaced.append(text.substring(lastPos, text.length()));
                text = replaced.toString();
            }
            replaced = null;
        }
        return text;
    }

    /**
     * Encode a String value to a valid JSON value. Enclose into quotes explicitly when needed.
     */
    public static String encode2JSON(String value) {
        if (value.isEmpty()) {
            return value;
        }
        StringWriter sw = new StringWriter();
        try (JsonWriter w = new JsonWriter(sw)) {
            w.beginArray();
            w.value(value);
            w.endArray();
            w.flush();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        String encoded = sw.toString();
        // We have ["value"], remove the array and quotes
        return encoded.substring(2, encoded.length() - 2);
    }

}
