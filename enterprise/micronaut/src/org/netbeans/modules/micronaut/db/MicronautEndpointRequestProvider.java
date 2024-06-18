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
package org.netbeans.modules.micronaut.db;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.micronaut.symbol.MicronautSymbolFinder;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CommandProvider.class)
public class MicronautEndpointRequestProvider implements CommandProvider {

    private static final String GENERATE_DATA_ENDPOINT = "nbls.micronaut.get.endpoint.request.body";

    @Override
    public Set<String> getCommands() {
        return Set.of(GENERATE_DATA_ENDPOINT);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            if (arguments.size() < 3) {
                future.complete(null);
            } else {
                String uri = ((JsonPrimitive) arguments.get(0)).getAsString();
                String type = ((JsonPrimitive) arguments.get(1)).getAsString();
                int line = ((JsonObject) arguments.get(2)).get("line").getAsInt();
                int column = ((JsonObject) arguments.get(2)).get("character").getAsInt();
                FileObject fo = URLMapper.findFileObject(java.net.URI.create(uri).toURL());
                JavaSource js = fo != null ? JavaSource.forFileObject(fo) : null;
                if (js == null) {
                    future.complete(null);
                } else {
                    js.runUserActionTask(cc -> {
                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        int offset = (int) cc.getCompilationUnit().getLineMap().getPosition(line, column);
                        TreePath path = cc.getTreeUtilities().pathFor(offset);
                        path = cc.getTreeUtilities().getPathElementOfKind(Tree.Kind.METHOD, path);
                        if (path == null) {
                            future.complete(null);
                        } else {
                            Element el = cc.getTrees().getElement(path);
                            if (el != null && el.getKind() == ElementKind.METHOD) {
                                String contentType = getContentType(cc, el, type);
                                List<VariableElement> fileUploads = new ArrayList<>();
                                Map<String, VariableElement> body2params = new LinkedHashMap<>();
                                for (VariableElement param : ((ExecutableElement) el).getParameters()) {
                                    if (param.asType().getKind() == TypeKind.DECLARED
                                            && "io.micronaut.http.multipart.CompletedFileUpload".contentEquals(((TypeElement) ((DeclaredType) param.asType()).asElement()).getQualifiedName())) {
                                        fileUploads.add(param);
                                        if (contentType == null) {
                                            contentType = "multipart/form-data";
                                        }
                                    } else {
                                        AnnotationMirror ann = Utils.getAnnotation(param.getAnnotationMirrors(), "io.micronaut.http.annotation.Body");
                                        if (ann != null) {
                                            String id = "";
                                            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : ann.getElementValues().entrySet()) {
                                                if ("value".contentEquals(entry.getKey().getSimpleName())) {
                                                    id = (String) entry.getValue().getValue();
                                                    if (contentType == null) {
                                                        contentType = "application/json";
                                                    }
                                                }
                                            }
                                            body2params.put(id, param);
                                            if (contentType == null && id.isEmpty()) {
                                                TypeMirror tm = param.asType();
                                                if (tm.getKind() == TypeKind.TYPEVAR) {
                                                    tm = ((TypeVariable) tm).getUpperBound();
                                                }
                                                if (tm.getKind() == TypeKind.DECLARED && Utils.getAnnotation(((DeclaredType) tm).asElement().getAnnotationMirrors(), "io.micronaut.serde.annotation.Serdeable") != null) {
                                                    contentType = "application/json";
                                                } else if (!cc.getTypes().isAssignable(param.asType(), cc.getElements().getTypeElement("java.lang.CharSequence").asType())) {
                                                    contentType = "";
                                                }
                                            }
                                        }
                                    }
                                }
                                AtomicInteger cnt = new AtomicInteger();
                                StringBuilder sb = new StringBuilder();
                                if (contentType != null) {
                                    sb.append("\nContent-Type: ${").append(cnt.incrementAndGet()).append(':').append(contentType).append("}");
                                }
                                if (!fileUploads.isEmpty() && "multipart/form-data".equals(contentType)) {
                                    sb.append("; boundary=mfd\n\n");
                                    for (VariableElement fileUpload : fileUploads) {
                                        int fnNum = cnt.incrementAndGet();
                                        sb.append("--mfd\nContent-Disposition: form-data; name=").append(fileUpload.getSimpleName()).append("; filename=\"${").append(fnNum).append(":file-name}\"\n\n");
                                        sb.append("< ${").append(cnt.incrementAndGet()).append(":/path/to/}${").append(fnNum).append("}\n");
                                    }
                                    sb.append("--mfd--");
                                } else if (body2params.isEmpty()) {
                                    sb.append("\n\n${").append(cnt.incrementAndGet()).append(":// TODO: Fill the value").append(contentType).append("}");
                                } else if (body2params.size() == 1 && body2params.keySet().iterator().next().isEmpty()) {
                                    sb.append("\n\n");
                                    fillJSON(cc, 0, null, body2params.values().iterator().next(), sb, cnt);
                                } else {
                                    sb.append("\n\n{\n");
                                    for (Iterator<Map.Entry<String, VariableElement>> it = body2params.entrySet().iterator(); it.hasNext();) {
                                        Map.Entry<String, VariableElement> entry = it.next();
                                        fillJSON(cc, 1, entry.getKey(), entry.getValue(), sb, cnt);
                                        if (it.hasNext()) {
                                            sb.append(',');
                                        }
                                        sb.append('\n');
                                    }
                                    sb.append('}');
                                }
                                future.complete(sb.toString());
                            } else {
                                future.complete(null);
                            }
                        }
                    }, true);
                }
            }
        } catch (IOException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    private static void fillJSON(CompilationInfo info, int level, String name, VariableElement ve, StringBuilder sb, AtomicInteger cnt) {
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
        if (name != null) {
            sb.append('"').append(name).append("\": ");
        }
        TypeMirror tm = ve.asType();
        if (tm.getKind() == TypeKind.TYPEVAR) {
            tm = ((TypeVariable) tm).getUpperBound();
        }
        if (tm.getKind() == TypeKind.DECLARED) {
            TypeElement te = (TypeElement) ((DeclaredType) tm).asElement();
            if ("java.lang.String".contentEquals(te.getQualifiedName())) {
                sb.append("\"${").append(cnt.incrementAndGet()).append(':').append(ve.getSimpleName()).append("}\"");
            } else if (Utils.getAnnotation(te.getAnnotationMirrors(), "io.micronaut.serde.annotation.Serdeable") != null) {
                sb.append("{\n");
                if (level > 0) {
                    VariableElement id = Utils.getIdElement(ElementFilter.fieldsIn(te.getEnclosedElements()));
                    if (id != null) {
                        fillJSON(info, level + 1, id.getSimpleName().toString(), id, sb, cnt);
                    } else {
                        sb.append("${").append(cnt.incrementAndGet()).append(": // TODO: Fill the value for '").append(id.getSimpleName()).append("' of type '").append(Utils.getTypeName(info, id.asType(), false, false)).append("'}");
                    }
                    sb.append('\n');
                } else {
                    for (Iterator<VariableElement> it = ElementFilter.fieldsIn(te.getEnclosedElements()).iterator(); it.hasNext();) {
                        VariableElement field = it.next();
                        fillJSON(info, level + 1, field.getSimpleName().toString(), field, sb, cnt);
                        if (it.hasNext()) {
                            sb.append(',');
                        }
                        sb.append('\n');
                    }
                }
                for (int i = 0; i < level; i++) {
                    sb.append("  ");
                }
                sb.append('}');
            } else {
                Types types = info.getTypes();
                if (types.isAssignable(types.erasure(tm), info.getElements().getTypeElement("java.util.Collection").asType())) {
                    sb.append("[]");
                } else if (types.isAssignable(types.erasure(tm), info.getElements().getTypeElement("java.lang.Number").asType())) {
                    sb.append("${").append(cnt.incrementAndGet()).append(':').append(ve.getSimpleName()).append('}');
                } else {
                    try {
                        tm = info.getTypes().unboxedType(tm);
                        sb.append("${").append(cnt.incrementAndGet()).append(':').append(ve.getSimpleName()).append('}');
                    } catch (IllegalArgumentException e) {
                        sb.append("${").append(cnt.incrementAndGet()).append(": // TODO: Fill the value for '").append(ve.getSimpleName()).append("' of type '").append(Utils.getTypeName(info, ve.asType(), false, false)).append("'}");
                    }
                }
            }
        } else if (tm.getKind().isPrimitive()) {
            sb.append("${").append(cnt.incrementAndGet()).append(':').append(ve.getSimpleName()).append('}');
        } else {
            sb.append("${").append(cnt.incrementAndGet()).append(": // TODO: Fill the value for '").append(ve.getSimpleName()).append("' of type '").append(Utils.getTypeName(info, ve.asType(), false, false)).append("'}");
        }
    }

    private static String type2AnnType(String type) {
        switch (type) {
            case "POST":
                return "io.micronaut.http.annotation.Post";
            case "PUT":
                return "io.micronaut.http.annotation.Put";
        }
        return null;
    }

    private static String getContentType(CompilationInfo info, Element el, String type) {
        String annType = type2AnnType(type);
        if (annType != null) {
            MicronautSymbolFinder.MthIterator it = new MicronautSymbolFinder.MthIterator(el, info.getElements(), info.getTypes());
            while (it.hasNext()) {
                ExecutableElement mth = it.next();
                AnnotationMirror ann = Utils.getAnnotation(mth.getAnnotationMirrors(), annType);
                if (ann != null) {
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : ann.getElementValues().entrySet()) {
                        if ("consumes".contentEquals(entry.getKey().getSimpleName())) {
                            return entry.getValue().accept(new SimpleAnnotationValueVisitor6<String, Void>() {
                                @Override
                                public String visitString(String s, Void p) {
                                    return s;
                                }
                                @Override
                                public String visitArray(List<? extends AnnotationValue> vals, Void p) {
                                    for (AnnotationValue val : vals) {
                                        return val.accept(this, p);
                                    }
                                    return defaultAction(vals, p);
                                }
                            }, null);
                        }
                    }
                }
            }
        }
        return null;
    }
}
