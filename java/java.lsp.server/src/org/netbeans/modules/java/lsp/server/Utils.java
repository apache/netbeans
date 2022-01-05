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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
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
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.editor.java.Utilities;
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

    public static String label(CompilationInfo info, Element e, boolean fqn) {
        switch (e.getKind()) {
            case PACKAGE:
                PackageElement pe = (PackageElement) e;
                return fqn ? pe.getQualifiedName().toString() : pe.getSimpleName().toString();
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE:
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

    public static Range selectionRange(CompilationInfo info, Tree tree) {
        int[] span = null;
        switch (tree.getKind()) {
            case CLASS:
                span = info.getTreeUtilities().findNameSpan((ClassTree) tree);
                break;
            case METHOD:
                span = info.getTreeUtilities().findNameSpan((MethodTree)tree);
                break;
            case VARIABLE:
                span = info.getTreeUtilities().findNameSpan((VariableTree)tree);
                break;
        }
        if (span == null) {
            return null;
        }
        return new Range(createPosition(info.getCompilationUnit(), (int) span[0]),
                         createPosition(info.getCompilationUnit(), (int) span[1]));
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
