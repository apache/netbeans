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
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.SymbolTag;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class Utils {

    public static SymbolKind structureElementKind2SymbolKind (StructureElement.Kind kind) {
        switch (kind) {
            case Array : return SymbolKind.Array;
            case Boolean: return SymbolKind.Boolean;
            case Class: return SymbolKind.Class;
            case Constant: return SymbolKind.Constant;
            case Constructor: return SymbolKind.Constructor;
            case Enum: return SymbolKind.Enum;
            case EnumMember: return SymbolKind.EnumMember;
            case Event: return SymbolKind.Event;
            case Field: return SymbolKind.Field;
            case File: return SymbolKind.File;
            case Function: return SymbolKind.Function;
            case Interface: return SymbolKind.Interface;
            case Key: return SymbolKind.Key;
            case Method: return SymbolKind.Method;
            case Module: return SymbolKind.Module;
            case Namespace: return SymbolKind.Namespace;
            case Null: return SymbolKind.Null;
            case Number: return SymbolKind.Number;
            case Object: return SymbolKind.Object;
            case Operator: return SymbolKind.Operator;
            case Package: return SymbolKind.Package;
            case Property: return SymbolKind.Property;
            case String: return SymbolKind.String;
            case Struct: return SymbolKind.Struct;
            case TypeParameter: return SymbolKind.TypeParameter;
            case Variable: return SymbolKind.Variable;
        }
        return SymbolKind.Object;
    }
    
    @NonNull
    public static QuerySupport.Kind searchType2QueryKind(@NonNull final SearchType searchType) {
        // copy of org.netbeans.modules.jumpto.common.Utils.toQueryKind
        switch (searchType) {
            case CAMEL_CASE:
                return QuerySupport.Kind.CAMEL_CASE;
            case CASE_INSENSITIVE_CAMEL_CASE:
                return QuerySupport.Kind.CASE_INSENSITIVE_CAMEL_CASE;
            case CASE_INSENSITIVE_EXACT_NAME:
            case EXACT_NAME:
                return QuerySupport.Kind.EXACT;
            case CASE_INSENSITIVE_PREFIX:
                return QuerySupport.Kind.CASE_INSENSITIVE_PREFIX;
            case CASE_INSENSITIVE_REGEXP:
                return QuerySupport.Kind.CASE_INSENSITIVE_REGEXP;
            case PREFIX:
                return QuerySupport.Kind.PREFIX;
            case REGEXP:
                return QuerySupport.Kind.REGEXP;
            default:
                throw new IllegalThreadStateException(String.valueOf(searchType));
        }
    }
    
    public static SymbolKind cslElementKind2SymbolKind(final org.netbeans.modules.csl.api.ElementKind elementKind) {
        // copy of org.netbeans.modules.csl.navigation.GsfStructureProvider.convertKind
        switch(elementKind) {
            case ATTRIBUTE: return SymbolKind.Property;
            case CALL: return SymbolKind.Event;
            case CLASS: return SymbolKind.Class;
            case CONSTANT: return SymbolKind.Constant;
            case CONSTRUCTOR: return SymbolKind.Constructor;
            case DB: return SymbolKind.File;
            case ERROR: return SymbolKind.Event;
            case METHOD: return SymbolKind.Method;
            case FILE: return SymbolKind.File;
            case FIELD: return SymbolKind.Field;
            case MODULE: return SymbolKind.Module;
            case VARIABLE: return SymbolKind.Variable;
            case GLOBAL: return SymbolKind.Module;
            case INTERFACE: return SymbolKind.Interface;
            case KEYWORD: return SymbolKind.Key;
            case OTHER: return SymbolKind.Object;
            case PACKAGE: return SymbolKind.Package;
            case PARAMETER: return SymbolKind.Variable;
            case PROPERTY: return SymbolKind.Property;
            case RULE: return SymbolKind.Event;
            case TAG: return SymbolKind.Operator;
            case TEST: return SymbolKind.Function;
        }
        return SymbolKind.Object;
    }
    
    public static List<SymbolTag> elementTags2SymbolTags (Set<StructureElement.Tag> tags) {
        if (tags != null) {
            // we now have only deprecated tag
            return Collections.singletonList(SymbolTag.Deprecated);
        }
        return null;
    }
    
    public static SymbolKind elementKind2SymbolKind(ElementKind kind) {
        switch (kind) {
            case PACKAGE:
                return SymbolKind.Package;
            case ENUM:
                return SymbolKind.Enum;
            case CLASS:
            case RECORD:
                return SymbolKind.Class;
            case ANNOTATION_TYPE:
                return SymbolKind.Interface;
            case INTERFACE:
                return SymbolKind.Interface;
            case ENUM_CONSTANT:
            case RECORD_COMPONENT:
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

    public static String label(CompilationInfo info, Element e, boolean fqn) {
        switch (e.getKind()) {
            case PACKAGE:
                PackageElement pe = (PackageElement) e;
                return fqn ? pe.getQualifiedName().toString() : pe.getSimpleName().toString();
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE:
            case RECORD:
                TypeElement te = (TypeElement) e;
                StringBuilder sb = new StringBuilder();
                sb.append(fqn ? te.getQualifiedName() : te.getSimpleName());
                List<? extends TypeParameterElement> typeParams = te.getTypeParameters();
                if (typeParams != null && !typeParams.isEmpty()) {
                    sb.append("<"); // NOI18N
                    for(Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext();) {
                        TypeParameterElement tp = it.next();
                        sb.append(tp.getSimpleName());
                        List<? extends TypeMirror> bounds = tp.getBounds();
                        if (!bounds.isEmpty()) {
                            if (bounds.size() > 1 || !"java.lang.Object".equals(bounds.get(0).toString())) { // NOI18N
                                sb.append(" extends "); // NOI18N
                                for (Iterator<? extends TypeMirror> bIt = bounds.iterator(); bIt.hasNext();) {
                                    sb.append(Utilities.getTypeName(info, bIt.next(), fqn));
                                    if (bIt.hasNext()) {
                                        sb.append(" & "); // NOI18N
                                    }
                                }
                            }
                        }
                        if (it.hasNext()) {
                            sb.append(", "); // NOI18N
                        }
                    }
                    sb.append(">"); // NOI18N
                }
                return sb.toString();
            case FIELD:
            case ENUM_CONSTANT:
            case RECORD_COMPONENT:
                return e.getSimpleName().toString();
            case CONSTRUCTOR:
            case METHOD:
                ExecutableElement ee = (ExecutableElement) e;
                sb = new StringBuilder();
                if (ee.getKind() == ElementKind.CONSTRUCTOR) {
                    sb.append(ee.getEnclosingElement().getSimpleName());
                } else {
                    sb.append(ee.getSimpleName());
                }
                sb.append("("); // NOI18N
                for (Iterator<? extends VariableElement> it = ee.getParameters().iterator(); it.hasNext();) {
                    VariableElement param = it.next();
                    if (!it.hasNext() && ee.isVarArgs() && param.asType().getKind() == TypeKind.ARRAY) {
                        sb.append(Utilities.getTypeName(info, ((ArrayType) param.asType()).getComponentType(), fqn));
                        sb.append("...");
                    } else {
                        sb.append(Utilities.getTypeName(info, param.asType(), fqn));
                    }
                    sb.append(" "); // NOI18N
                    sb.append(param.getSimpleName());
                    if (it.hasNext()) {
                        sb.append(", "); // NOI18N
                    }
                }
                sb.append(")"); // NOI18N
                return sb.toString();
        }
        return null;
    }

    public static String detail(CompilationInfo info, Element e, boolean fqn) {
        switch (e.getKind()) {
            case FIELD:
                StringBuilder sb = new StringBuilder();
                sb.append(": " );
                sb.append(Utilities.getTypeName(info, e.asType(), fqn));
                return sb.toString();
            case METHOD:
                sb = new StringBuilder();
                TypeMirror rt = ((ExecutableElement) e).getReturnType();
                if (rt.getKind() == TypeKind.VOID) {
                    sb.append(": void" );
                } else {
                    sb.append(": ");
                    sb.append(Utilities.getTypeName(info, rt, fqn));
                }
                return sb.toString();
        }
        return null;
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

    public static Position createPosition(LineDocument doc, int offset) {
        try {
            int line = LineDocumentUtils.getLineIndex(doc, offset);
            int column = offset - LineDocumentUtils.getLineStart(doc, offset);

            return new Position(line, column);
        } catch (BadLocationException ex) {
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

    /**
     * Simple conversion from HTML to plaintext. Removes all html tags incl. attributes,
     * replaces BR, P and HR tags with newlines.
     * @param s html text
     * @return plaintext
     */
    public static String html2plain(String s) {
        boolean inTag = false;
        int tagStart = -1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (inTag) {
                boolean alpha = Character.isAlphabetic(ch);
                if (tagStart > 0 && !alpha) {
                    String t = s.substring(tagStart, i).toLowerCase(Locale.ENGLISH);
                    switch (t) {
                        case "br": case "p": case "hr": // NOI1N
                            sb.append("\n");
                            break;
                    }
                    // prevent entering tagstart state again
                    tagStart = -2;
                }
                if (ch == '>') { // NOI18N
                    inTag = false;
                } else if (tagStart == -1 && alpha) {
                    tagStart = i;
                }
            } else {
                if (ch == '<') { // NOI18N
                    tagStart = -1;
                    inTag = true;
                    continue;
                }
                sb.append(ch);
            }
        }
        return sb.toString();
    }

}
