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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.*;
import com.sun.source.util.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.lib.editor.codetemplates.spi.*;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.preprocessorbridge.api.JavaSourceUtil;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dusan Balek
 */
public class JavaCodeTemplateProcessor implements CodeTemplateProcessor {
    
    public static final String INSTANCE_OF = "instanceof"; //NOI18N
    public static final String ARRAY = "array"; //NOI18N
    public static final String ITERABLE = "iterable"; //NOI18N
    public static final String TYPE = "type"; //NOI18N
    public static final String TYPE_VAR = "typeVar"; //NOI18N
    public static final String ITERABLE_ELEMENT_TYPE = "iterableElementType"; //NOI18N
    public static final String LEFT_SIDE_TYPE = "leftSideType"; //NOI18N
    public static final String RIGHT_SIDE_TYPE = "rightSideType"; //NOI18N
    public static final String CAST = "cast"; //NOI18N
    public static final String NEW_VAR_NAME = "newVarName"; //NOI18N
    public static final String NAMED = "named"; //NOI18N
    public static final String UNCAUGHT_EXCEPTION_TYPE = "uncaughtExceptionType"; //NOI18N
    public static final String UNCAUGHT_EXCEPTION_CATCH_STATEMENTS = "uncaughtExceptionCatchStatements"; //NOI18N
    public static final String CURRENT_CLASS_NAME = "currClassName"; //NOI18N
    public static final String CURRENT_CLASS_FULLY_QUALIFIED_NAME = "currClassFQName"; //NOI18N
    public static final String CURRENT_PACKAGE_NAME = "currPackageName"; //NOI18N
    public static final String CURRENT_METHOD_NAME = "currMethodName"; //NOI18N

    private static final String TRUE = "true"; //NOI18N
    private static final String NULL = "null"; //NOI18N
    private static final String ZERO = "0"; //NOI18N
    private static final String ERROR = "<error>"; //NOI18N
    private static final String CLASS = "class"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor("Update Imports", 1, false, false); //NOI18N
    
    private final CodeTemplateInsertRequest request;

    private int caretOffset;
    private CompilationInfo cInfo = null;
    private TreePath treePath = null;
    private Scope scope = null;
    private TypeElement enclClass = null;
    private List<Element> locals = null;
    private List<Element> typeVars = null;
    private final Map<CodeTemplateParameter, String> param2hints = new HashMap<>();
    private final Map<CodeTemplateParameter, TypeMirror> param2types = new HashMap<>();
    private final Set<String> autoImportedTypeNames = Collections.synchronizedSet(new HashSet<String>());
    private final AtomicReference<RequestProcessor.Task> task = new AtomicReference<>();
    
    private JavaCodeTemplateProcessor(CodeTemplateInsertRequest request) {
        this.request = request;
        boolean needsParsing = false;
        for (CodeTemplateParameter param : request.getMasterParameters()) {
            for (String hint : param.getHints().keySet()) {
                if (INSTANCE_OF.equals(hint)
                        || ARRAY.equals(hint)
                        || ITERABLE.equals(hint)
                        || NEW_VAR_NAME.equals(hint)
                        || TYPE_VAR.equals(hint)) {
                    initParsing(true);
                    return;
                }
            }
            if (CodeTemplateParameter.SELECTION_PARAMETER_NAME.equals(param.getName())) {
                needsParsing = true;
            }
            for (String hint : param.getHints().keySet()) {
                if (UNCAUGHT_EXCEPTION_CATCH_STATEMENTS.equals(hint)
                        || TYPE.equals(hint)
                        || ITERABLE_ELEMENT_TYPE.equals(hint)
                        || LEFT_SIDE_TYPE.equals(hint)
                        || RIGHT_SIDE_TYPE.equals(hint)
                        || CAST.equals(hint)
                        || NEW_VAR_NAME.equals(hint)
                        || CURRENT_CLASS_NAME.equals(hint)
                        || CURRENT_CLASS_FULLY_QUALIFIED_NAME.equals(hint)
                        || CURRENT_PACKAGE_NAME.equals(hint)
                        || CURRENT_METHOD_NAME.equals(hint)
                        || ITERABLE_ELEMENT_TYPE.equals(hint)
                        || UNCAUGHT_EXCEPTION_TYPE.equals(hint)) {
                    needsParsing = true;
                }
            }
        }
        if (needsParsing) {
            initParsing(false);
        }
    }
    
    @Override
    public void updateDefaultValues() {
        updateTemplateEnding();
        updateTemplateBasedOnCatchers();
        updateTemplateBasedOnSelection();
        boolean cont = true;
        while (cont) {
            cont = false;
            for (Object p : request.getMasterParameters()) {
                CodeTemplateParameter param = (CodeTemplateParameter)p;
                String value = getProposedValue(param); 
                if (value != null && !value.equals(param.getValue())) {
                    param.setValue(value);
                    cont = true;
                }
            }
        }
        updateImports();
    }
    
    @Override
    public void parameterValueChanged(CodeTemplateParameter masterParameter, boolean typingChange) {
        if (typingChange) {
            for (Object p : request.getMasterParameters()) {
                CodeTemplateParameter param = (CodeTemplateParameter)p;
                if (!param.isUserModified()) {
                    String value = getProposedValue(param);
                    if (value != null && !value.equals(param.getValue())) {
                        param.setValue(value);
                    }
                } else {
                    param2types.remove(param);
                }
            }
            updateImports();                    
        }
    }
    
    @Override
    public void release() {
    }

    private void updateTemplateEnding() {
        String text = request.getParametrizedText();
        if (text.endsWith("\n")) { //NOI18N
            JTextComponent component = request.getComponent();
            int offset = component.getSelectionEnd();
            Document doc = component.getDocument();
            if (doc.getLength() > offset) {
                try {
                    if ("\n".equals(doc.getText(offset, 1))) {
                        request.setParametrizedText(text.substring(0, text.length() - 1));
                    }
                } catch (BadLocationException ble) {
                }
            }
        }
    }

    private void updateTemplateBasedOnCatchers() {
        for (CodeTemplateParameter parameter : request.getAllParameters()) {
            for (String hint : parameter.getHints().keySet()) {
                if (UNCAUGHT_EXCEPTION_CATCH_STATEMENTS.equals(hint) && cInfo != null) {
                    SourcePositions[] sourcePositions = new SourcePositions[1];
                    TreeUtilities tu = cInfo.getTreeUtilities();
                    StatementTree stmt = tu.parseStatement("{" + request.getInsertText() + "}", sourcePositions); //NOI18N
                    if (!Utilities.containErrors(stmt)) {
                        TreePath path = tu.pathFor(new TreePath(treePath, stmt), parameter.getInsertTextOffset(), sourcePositions[0]);
                        path = tu.getPathElementOfKind(Tree.Kind.TRY, path);
                        if (path != null && ((TryTree)path.getLeaf()).getBlock() != null) {
                            tu.attributeTree(stmt, scope);
                            StringBuilder sb = new StringBuilder();
                            int cnt = 0;
                            for (TypeMirror tm : tu.getUncaughtExceptions(new TreePath(path, ((TryTree)path.getLeaf()).getBlock()))) {
                                sb.append("catch ("); //NOI18N
                                sb.append("${_GEN_UCE_TYPE_" + cnt++ + " type=" + Utilities.getTypeName(cInfo, tm, true) + " default=" + Utilities.getTypeName(cInfo, tm, false) + "}"); //NOI18N
                                sb.append(" ${_GEN_UCE_NAME_" + cnt++ + " newVarName}){}"); //NOI18N
                            }
                            if (sb.length() > 0) {
                                StringBuilder ptBuilder = new StringBuilder(request.getParametrizedText());
                                ptBuilder.replace(parameter.getParametrizedTextStartOffset(), parameter.getParametrizedTextEndOffset(), sb.toString());
                                request.setParametrizedText(ptBuilder.toString());
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void updateTemplateBasedOnSelection() {
        for (CodeTemplateParameter parameter : request.getAllParameters()) {
            if (CodeTemplateParameter.SELECTION_PARAMETER_NAME.equals(parameter.getName())) {
                JTextComponent component = request.getComponent();
                if (component.getSelectionStart() != component.getSelectionEnd()) {
                    if (cInfo != null) {
                        TreeUtilities tu = cInfo.getTreeUtilities();
                        StatementTree stat = tu.parseStatement(request.getInsertText(), null);
                        EnumSet<Tree.Kind> kinds = EnumSet.of(Tree.Kind.BLOCK, Tree.Kind.DO_WHILE_LOOP,
                                Tree.Kind.ENHANCED_FOR_LOOP, Tree.Kind.FOR_LOOP, Tree.Kind.IF, Tree.Kind.SYNCHRONIZED,
                                Tree.Kind.TRY, Tree.Kind.WHILE_LOOP);
                        if (stat != null && kinds.contains(stat.getKind())) {
                            TreePath treePath = tu.pathFor(component.getSelectionStart());
                            Tree tree = treePath.getLeaf();
                            if (tree.getKind() == Tree.Kind.BLOCK && tree == tu.pathFor(component.getSelectionEnd()).getLeaf()) {
                                String selection = component.getSelectedText();
                                int idx = 0;
                                while ((idx < selection.length()) && (selection.charAt(idx) <= ' '))
                                    idx++;
                                final StringBuilder selectionText = new StringBuilder(parameter.getValue().trim());
                                final int caretOffset = component.getSelectionStart() + idx;
                                final StringBuilder sb = new StringBuilder();
                                final Trees trees = cInfo.getTrees();
                                final SourcePositions sp = trees.getSourcePositions();
                                final Map<VariableElement, VariableTree> vars = new HashMap<>();
                                final LinkedList<VariableTree> varList = new LinkedList<>();
                                ErrorAwareTreePathScanner scanner = new ErrorAwareTreePathScanner() {
                                    private int cnt = 0;
                                    @Override
                                    public Object visitIdentifier(IdentifierTree node, Object p) {
                                        Element e = trees.getElement(getCurrentPath());
                                        VariableTree var;
                                        if (e != null && (var = vars.remove(e)) != null) {
                                            sb.append(var.getType()).append(' ').append(var.getName());
                                            TypeMirror tm = ((VariableElement)e).asType();
                                            switch(tm.getKind()) {
                                                case ARRAY:
                                                case DECLARED:
                                                    sb.append(" = ${_GEN_PARAM_" + cnt++ + " default=\"null\"}"); //NOI18N
                                                    break;
                                                case BOOLEAN:
                                                    sb.append(" = ${_GEN_PARAM_" + cnt++ + " default=\"false\"}"); //NOI18N
                                                    break;
                                                case BYTE:
                                                case CHAR:
                                                case DOUBLE:
                                                case FLOAT:
                                                case INT:
                                                case LONG:
                                                case SHORT:
                                                    sb.append(" = ${_GEN_PARAM_" + cnt++ + " default=\"0\"}"); //NOI18N
                                                    break;
                                            }
                                            sb.append(";\n"); //NOI18N
                                        }
                                        return null;
                                    }
                                };
                                for (StatementTree st : ((BlockTree)tree).getStatements()) {
                                    if (sp.getStartPosition(cInfo.getCompilationUnit(), st) >= component.getSelectionStart()) {
                                        if (sp.getEndPosition(cInfo.getCompilationUnit(), st) <= component.getSelectionEnd()) {
                                            if (st.getKind() == Tree.Kind.VARIABLE) {
                                                Element e = trees.getElement(new TreePath(treePath, st));
                                                if (e != null && e.getKind() == ElementKind.LOCAL_VARIABLE) {
                                                    vars.put((VariableElement)e, (VariableTree)st);
                                                    varList.addFirst((VariableTree)st);
                                                }
                                            }
                                        } else {
                                            scanner.scan(new TreePath(treePath, st), null);
                                        }
                                    }
                                }
                                Collection<VariableTree> vals = vars.values();
                                for (VariableTree var : varList) {
                                    if (!vals.contains(var)) {
                                        int start = (int) sp.getStartPosition(cInfo.getCompilationUnit(), var) - caretOffset;
                                        int end = (int) sp.getEndPosition(cInfo.getCompilationUnit(), var.getType()) - caretOffset;
                                        selectionText.delete(start, end);
                                    }
                                }
                                if (sb.length() > 0) {
                                    request.setParametrizedText(sb.toString() + request.getParametrizedText());
                                    for (CodeTemplateParameter p : request.getAllParameters()) {
                                        if (CodeTemplateParameter.SELECTION_PARAMETER_NAME.equals(p.getName())) {
                                            p.setValue(selectionText.toString());
                                            break;
                                        }                                            
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }
    
    private void updateImports() {
        final AutoImport imp = AutoImport.get(cInfo);
        for (Map.Entry<CodeTemplateParameter, TypeMirror> entry : param2types.entrySet()) {
            CodeTemplateParameter param = entry.getKey();
            TypeMirror tm = param2types.get(param);
            TreePath tp = cInfo.getTreeUtilities().pathFor(caretOffset + param.getInsertTextOffset());
            CharSequence typeName = imp.resolveImport(tp, tm);
            if (CAST.equals(param2hints.get(param))) {
                param.setValue("(" + typeName + ")"); //NOI18N
            } else if (INSTANCE_OF.equals(param2hints.get(param))) {
                String value = param.getValue().substring(param.getValue().lastIndexOf('.') + 1); //NOI18N
                param.setValue(typeName + "." + value); //NOI18N
            } else {
                param.setValue(typeName.toString());
            }
        }
        final Set<String> toRemove = new HashSet<>();
        synchronized(autoImportedTypeNames) {
            toRemove.addAll(autoImportedTypeNames);
            autoImportedTypeNames.addAll(imp.getAutoImportedTypes());
        }
        if (!toRemove.isEmpty()) {
            RequestProcessor.Task oldTask = task.getAndSet(RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        JavaCompletionProvider.JavaCompletionQuery.javadocBreak.set(true);
                        ModificationResult.runModificationTask(Collections.singleton(cInfo.getSnapshot().getSource()), new UserTask() {
                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                                copy.toPhase(JavaSource.Phase.RESOLVED);
                                for (Element usedElement : Utilities.getUsedElements(copy)) {
                                    switch (usedElement.getKind()) {
                                        case CLASS:
                                        case INTERFACE:
                                        case ENUM:
                                        case ANNOTATION_TYPE:
                                            toRemove.remove(((TypeElement)usedElement).getQualifiedName().toString());
                                    }
                                }
                                TreeMaker tm = copy.getTreeMaker();
                                CompilationUnitTree cut = copy.getCompilationUnit();
                                for (String typeName : toRemove) {
                                    for (ImportTree importTree : cut.getImports()) {
                                        if (!importTree.isStatic()) {
                                            if (typeName.equals(importTree.getQualifiedIdentifier().toString())) {
                                                cut = tm.removeCompUnitImport(cut, importTree);
                                                break;
                                            }
                                        }
                                    }
                                }
                                copy.rewrite(copy.getCompilationUnit(), cut);
                            }
                        }).commit();
                        autoImportedTypeNames.removeAll(toRemove);
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }));
            if (oldTask != null) {
                oldTask.cancel();
            }
        }
    }
    
    private String getProposedValue(CodeTemplateParameter param) {
        param2hints.remove(param);
        param2types.remove(param);
        String name = null;
        for (Object e : param.getHints().entrySet()) {
            Map.Entry entry = (Map.Entry)e;
            if (INSTANCE_OF.equals(entry.getKey())) {
                VariableElement ve = instanceOf((String)entry.getValue(), name);
                if (ve != null) {
                    param2hints.put(param, INSTANCE_OF);
                    return ve.getSimpleName().toString();
                } else if (name != null) {
                    ve = staticInstanceOf((String)entry.getValue(), name);
                    if (ve != null) {
                        TypeMirror tm = ve.getEnclosingElement().asType();
                        tm = cInfo.getTypes().erasure(tm);
                        String value = tm != null ? Utilities.getTypeName(cInfo, tm, true) + "." + ve.getSimpleName() : null;
                        if (value != null) {
                            param2hints.put(param, INSTANCE_OF);
                            if (containsDeclaredType(tm)) {
                                param2types.put(param, tm);
                            }
                            return value;
                        }
                    } else {
                        return valueOf((String)entry.getValue());
                    }                    
                }
            } else if (ARRAY.equals(entry.getKey())) {
                VariableElement ve = array();
                if (ve != null) {
                    param2hints.put(param, ARRAY);
                    return ve.getSimpleName().toString();
                }
            } else if (ITERABLE.equals(entry.getKey())) {
                VariableElement ve = iterable();
                if (ve != null) {
                    param2hints.put(param, ITERABLE);
                    return ve.getSimpleName().toString();
                }
            } else if (TYPE.equals(entry.getKey())) {
                TypeMirror tm = type((String)entry.getValue());
                if (tm != null && tm.getKind() != TypeKind.ERROR) {
                    if (name != null) {
                        TypeParameterElement tpe = typeVar(tm, name);
                        if (tpe != null) {
                            return tpe.getSimpleName().toString();
                        }
                    }
                    tm = resolveCapturedType(tm);
                    String value = tm != null ? Utilities.getTypeName(cInfo, tm, true).toString() : null;
                    if (value != null) {
                        param2hints.put(param, TYPE);
                        if (containsDeclaredType(tm)) {
                            param2types.put(param, tm);
                        }
                        return value;
                    }
                }
            } else if (TYPE_VAR.equals(entry.getKey())) {
                name = (String) entry.getValue();
            } else if (ITERABLE_ELEMENT_TYPE.equals(entry.getKey())) {
                TypeMirror tm = iterableElementType(param.getInsertTextOffset() + 1);
                if (tm != null && tm.getKind() != TypeKind.ERROR) {
                    tm = resolveCapturedType(tm);
                    String value = tm != null ? Utilities.getTypeName(cInfo, tm, true).toString() : null;
                    if (value != null) {
                        param2hints.put(param, ITERABLE_ELEMENT_TYPE);
                        if (containsDeclaredType(tm)) {
                            param2types.put(param, tm);
                        }
                        return value;
                    }
                }
            } else if (LEFT_SIDE_TYPE.equals(entry.getKey())) {
                TypeMirror tm = assignmentSideType(param.getInsertTextOffset() + 1, true);
                if (tm != null && tm.getKind() != TypeKind.ERROR) {
                    tm = resolveCapturedType(tm);
                    String value = tm != null ? Utilities.getTypeName(cInfo, tm, true).toString() : null;
                    if (value != null) {
                        param2hints.put(param, LEFT_SIDE_TYPE);
                        if (containsDeclaredType(tm)) {
                            param2types.put(param, tm);
                        }
                        return value;
                    }
                }
            } else if (RIGHT_SIDE_TYPE.equals(entry.getKey())) {
                TypeMirror tm = assignmentSideType(param.getInsertTextOffset() + 1, false);
                if (tm != null && tm.getKind() != TypeKind.ERROR) {
                    tm = resolveCapturedType(tm);
                    String value = tm != null ? Utilities.getTypeName(cInfo, tm, true).toString() : null;
                    if (value != null) {
                        param2hints.put(param, RIGHT_SIDE_TYPE);
                        if (containsDeclaredType(tm)) {
                            param2types.put(param, tm);
                        }
                        return value;
                    }
                }
            } else if (CAST.equals(entry.getKey())) {
                TypeMirror tm = cast(param.getInsertTextOffset() + 1);
                if (tm == null) {
                    param2hints.put(param, CAST);
                    param2types.remove(param);
                    return ""; //NOI18N
                } else if (tm.getKind() != TypeKind.ERROR) {
                    tm = resolveCapturedType(tm);
                    String value = tm != null ? Utilities.getTypeName(cInfo, tm, true).toString() : null;
                    if (value != null) {
                        param2hints.put(param, CAST);
                        if (containsDeclaredType(tm)) {
                            param2types.put(param, tm); //NOI18N
                        }                        return "(" + value + ")"; //NOI18N
                    }
                }
            } else if (NEW_VAR_NAME.equals(entry.getKey())) {
                param2hints.put(param, NEW_VAR_NAME);
                Object value = entry.getValue();
                if (!(value instanceof String) || "true".equals(value)) {
                    value = null;
                }
                return newVarName(param.getInsertTextOffset() + 1, (String)value);
            } else if (CURRENT_CLASS_NAME.equals(entry.getKey())) {
                param2hints.put(param, CURRENT_CLASS_NAME);
                return owningClassName(false);
            } else if (CURRENT_CLASS_FULLY_QUALIFIED_NAME.equals(entry.getKey())) {
                param2hints.put(param, CURRENT_CLASS_FULLY_QUALIFIED_NAME);
                return owningClassName(true);
            } else if (CURRENT_PACKAGE_NAME.equals(entry.getKey())) {
                param2hints.put(param, CURRENT_PACKAGE_NAME);
                return owningPackageName();
            } else if (CURRENT_METHOD_NAME.equals(entry.getKey())) {
                param2hints.put(param, CURRENT_METHOD_NAME);
                return owningMethodName();
            } else if (NAMED.equals(entry.getKey())) {
                name = param.getName();
            } else if (UNCAUGHT_EXCEPTION_TYPE.equals(entry.getKey())) {
                TypeMirror tm = uncaughtExceptionType(param.getInsertTextOffset() + 1);
                if (tm != null && tm.getKind() != TypeKind.ERROR) {
                    tm = resolveCapturedType(tm);
                    String value = tm != null ? Utilities.getTypeName(cInfo, tm, true).toString() : null;
                    if (value != null) {
                        param2hints.put(param, UNCAUGHT_EXCEPTION_TYPE);
                        if (containsDeclaredType(tm)) {
                            param2types.put(param, tm);
                        }
                        return value;
                    }
                }
            }
        }
        return name;
    }
    
    private VariableElement instanceOf(String typeName, String name) {
        try {
            if (cInfo != null) {
                TypeMirror type = type(typeName);
                VariableElement closest = null;
                int distance = Integer.MAX_VALUE;
                if (type != null) {
                    Types types = cInfo.getTypes();
                    for (Element e : locals) {
                        if (e instanceof VariableElement && !ERROR.contentEquals(e.getSimpleName())
                                && e.asType().getKind() != TypeKind.ERROR && types.isAssignable(e.asType(), type)) {
                            if (name == null) {
                                return (VariableElement)e;
                            }
                            int d = ElementHeaders.getDistance(e.getSimpleName().toString().toLowerCase(), name.toLowerCase());
                            if (isSameType(e.asType(), type, types)) {
                                d -= 1000;
                            }
                            if (d < distance) {
                                distance = d;
                                closest = (VariableElement)e;
                            }
                        }
                    }
                }
                return closest;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private VariableElement staticInstanceOf(String typeName, String name) {
        try {
            if (cInfo != null) {
                final Trees trees = cInfo.getTrees();
                TypeMirror type = type(typeName);
                VariableElement closest = null;
                int distance = Integer.MAX_VALUE;
                if (type != null) {
                    final Types types = cInfo.getTypes();
                    if (type.getKind() == TypeKind.DECLARED) {
                        final DeclaredType dType = (DeclaredType)type;
                        final TypeElement element = (TypeElement)dType.asElement();
                        final boolean isStatic = element.getKind().isClass() || element.getKind().isInterface();
                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                            @Override
                            public boolean accept(Element e, TypeMirror t) {
                                return e.getKind().isField() && !ERROR.contentEquals(e.getSimpleName()) && !CLASS.contentEquals(e.getSimpleName()) &&
                                        (!isStatic || e.getModifiers().contains(Modifier.STATIC)) &&
                                        trees.isAccessible(scope, e, (DeclaredType)t) &&
                                        (e.getKind().isField() && e.asType().getKind() != TypeKind.ERROR && types.isAssignable(e.asType(), dType)
                                        || e.getKind() == ElementKind.METHOD && ((ExecutableElement)e).getReturnType().getKind() != TypeKind.ERROR && types.isAssignable(((ExecutableElement)e).getReturnType(), dType));
                            }
                        };
                        for (Element ee : cInfo.getElementUtilities().getMembers(dType, acceptor)) {
                            if (name == null) {
                                return (VariableElement)ee;
                            }
                            int d = ElementHeaders.getDistance(ee.getSimpleName().toString().toLowerCase(), name.toLowerCase());
                            if (ee.getKind().isField() && isSameType(((VariableElement)ee).asType(), dType, types) || ee.getKind() == ElementKind.METHOD && isSameType(((ExecutableElement)ee).getReturnType(), dType, types)) {
                                d -= 1000;
                            }
                            if (d < distance) {
                                distance = d;
                                closest = (VariableElement)ee;
                            }
                        }
                    }
                }
                return closest;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private TypeParameterElement typeVar(TypeMirror type, String name) {
        try {
            if (cInfo != null) {
                TypeParameterElement closest = null;
                int distance = Integer.MAX_VALUE;
                if (type != null) {
                    Types types = cInfo.getTypes();
                    for (Element e : typeVars) {
                        if (e instanceof TypeParameterElement && !ERROR.contentEquals(e.getSimpleName()) && types.isAssignable(e.asType(), type)) {
                            int d = ElementHeaders.getDistance(e.getSimpleName().toString().toLowerCase(), name.toLowerCase());
                            if (isSameType(e.asType(), type, types)) {
                                d -= 1000;
                            }
                            if (d < distance) {
                                distance = d;
                                closest = (TypeParameterElement)e;
                            }
                        }
                    }
                }
                return closest;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String valueOf(String typeName) {
        try {
            if (cInfo != null) {
                TypeMirror type = type(typeName);
                if (type != null) {
                    switch(type.getKind()) {
                        case ARRAY:
                        case DECLARED:
                        case TYPEVAR:
                        case WILDCARD:
                            return null;
                        case BOOLEAN:
                            return TRUE;
                        case BYTE:
                        case CHAR:
                        case INT:
                        case LONG:
                        case SHORT:
                        case DOUBLE:
                        case FLOAT:
                            return ZERO;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private VariableElement array() {
        if (cInfo != null) {
            for (Element e : locals) {
                if (e instanceof VariableElement && !ERROR.contentEquals(e.getSimpleName()) && e.asType().getKind() == TypeKind.ARRAY) {
                    return (VariableElement)e;
                }
            }
        }
        return null;
    }

    private VariableElement iterable() {
        if (cInfo != null) {
            TypeElement iterableTE = cInfo.getElements().getTypeElement("java.lang.Iterable"); //NOI18N
            if (iterableTE != null) {
                TypeMirror iterableType = cInfo.getTypes().getDeclaredType(iterableTE);
                for (Element e : locals) {
                    if (e instanceof VariableElement && !ERROR.contentEquals(e.getSimpleName()) && (e.asType().getKind() == TypeKind.ARRAY || cInfo.getTypes().isAssignable(e.asType(), iterableType))) {
                        return (VariableElement)e;
                    }
                }
            }
        }
        return null;
    }

    private TypeMirror type(String typeName) {
        try {
            typeName = typeName.trim();
            if (cInfo != null && typeName.length() > 0) {
                SourcePositions[] sourcePositions = new SourcePositions[1];
                TreeUtilities tu = cInfo.getTreeUtilities();
                StatementTree stmt = tu.parseStatement("{" + typeName + " a;}", sourcePositions); //NOI18N
                if (!Utilities.containErrors(stmt) && stmt.getKind() == Tree.Kind.BLOCK) {
                    List<? extends StatementTree> stmts = ((BlockTree)stmt).getStatements();
                    if (!stmts.isEmpty()) {
                        StatementTree var = stmts.get(0);
                        if (var.getKind() == Tree.Kind.VARIABLE) {
                            tu.attributeTree(stmt, scope);
                            TypeMirror ret = cInfo.getTrees().getTypeMirror(new TreePath(treePath, ((VariableTree)var).getType()));
                            if (ret != null)
                                return ret;
                        }
                    }
                }
                return cInfo.getTreeUtilities().parseType(typeName, enclClass);
            }
        } catch (Exception e) {            
        }
        return null;
    }
    
    private TypeMirror iterableElementType(int caretOffset) {
        try {            
            if (cInfo != null) {
                SourcePositions[] sourcePositions = new SourcePositions[1];
                TreeUtilities tu = cInfo.getTreeUtilities();
                StatementTree stmt = tu.parseStatement("{" + request.getInsertText() + "}", sourcePositions); //NOI18N
                if (Utilities.containErrors(stmt)) {
                    return null;
                }
                TreePath path = tu.pathFor(new TreePath(treePath, stmt), caretOffset + 1, sourcePositions[0]);
                TreePath loop = tu.getPathElementOfKind(Tree.Kind.ENHANCED_FOR_LOOP, path);
                if (loop != null) {
                    tu.attributeTree(stmt, scope);
                    TypeMirror type = cInfo.getTrees().getTypeMirror(new TreePath(loop, ((EnhancedForLoopTree)loop.getLeaf()).getExpression()));
                    switch (type.getKind()) {
                        case ARRAY:
                            type = ((ArrayType)type).getComponentType();
                            return type;
                        case DECLARED:
                            DeclaredType dt = findIterableType(type);
                            if (dt != null) {
                                Iterator<? extends TypeMirror> types = dt.getTypeArguments().iterator();
                                if (types.hasNext()) {
                                    return types.next();
                                }
                            }
                            return cInfo.getElements().getTypeElement("java.lang.Object").asType(); //NOI18N
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private TypeMirror assignmentSideType(int caretOffset, boolean left) {
        try {
            if (cInfo != null) {
                SourcePositions[] sourcePositions = new SourcePositions[1];
                TreeUtilities tu = cInfo.getTreeUtilities();
                StatementTree stmt = tu.parseStatement("{" + request.getInsertText() + "}", sourcePositions); //NOI18N
                if (Utilities.containErrors(stmt)) {
                    return null;
                }
                TreePath path = tu.pathFor(new TreePath(treePath, stmt), caretOffset + 1, sourcePositions[0]);
                TreePath tree = tu.getPathElementOfKind(EnumSet.of(Tree.Kind.ASSIGNMENT, Tree.Kind.VARIABLE), path);
                if (tree == null) {
                    return null;
                }
                tu.attributeTree(stmt, scope);
                TypeMirror tm;
                if (tree.getLeaf().getKind() == Tree.Kind.ASSIGNMENT) {
                    AssignmentTree as = (AssignmentTree)tree.getLeaf();
                    TreePath type = new TreePath(tree, left ? as.getVariable() : as.getExpression());
                    tm = cInfo.getTrees().getTypeMirror(type);
                } else {
                    VariableTree vd = (VariableTree)tree.getLeaf();
                    TreePath type = new TreePath(tree, left ? vd.getType() : vd.getInitializer());
                    tm = cInfo.getTrees().getTypeMirror(type);
                }
                if (tm != null && tm.getKind() == TypeKind.ERROR) {
                    tm = cInfo.getTrees().getOriginalType((ErrorType)tm);
                }
                if (tm.getKind() == TypeKind.NONE) {
                    tm = cInfo.getElements().getTypeElement("java.lang.Object").asType(); //NOI18N
                }
                return tm;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private TypeMirror cast(int caretOffset) {
        try {
            if (cInfo != null) {
                SourcePositions[] sourcePositions = new SourcePositions[1];
                TreeUtilities tu = cInfo.getTreeUtilities();
                StatementTree stmt = tu.parseStatement("{" + request.getInsertText() + "}", sourcePositions); //NOI18N
                if (Utilities.containErrors(stmt)) {
                    return null;
                }
                TreePath path = tu.pathFor(new TreePath(treePath, stmt), caretOffset + 1, sourcePositions[0]);
                TreePath tree = tu.getPathElementOfKind(EnumSet.of(Tree.Kind.ASSIGNMENT, Tree.Kind.VARIABLE), path);
                if (tree == null) {
                    return null;
                }
                tu.attributeTree(stmt, scope);
                if (tree.getLeaf().getKind() == Tree.Kind.ASSIGNMENT) {
                    AssignmentTree as = (AssignmentTree)tree.getLeaf();
                    TypeMirror left = cInfo.getTrees().getTypeMirror(new TreePath(tree, as.getVariable()));
                    if (left == null) {
                        return null;
                    }
                    TreePath exp = new TreePath(tree, as.getExpression());
                    if (exp.getLeaf() instanceof TypeCastTree) {
                        exp = new TreePath(exp, ((TypeCastTree)exp.getLeaf()).getExpression());
                    }
                    TypeMirror right = cInfo.getTrees().getTypeMirror(exp);
                    if (right == null) {
                        return null;
                    }
                    if (right.getKind() == TypeKind.ERROR) {
                        right = cInfo.getTrees().getOriginalType((ErrorType)right);
                    }
                    if (cInfo.getTypes().isAssignable(right, left)) {
                        return null;
                    }
                    return left;
                }
                VariableTree vd = (VariableTree)tree.getLeaf();
                TypeMirror left = cInfo.getTrees().getTypeMirror(new TreePath(tree, vd.getType()));
                if (left == null) {
                    return null;
                }
                TreePath exp = new TreePath(tree, vd.getInitializer());
                if (exp.getLeaf() instanceof TypeCastTree) {
                    exp = new TreePath(exp, ((TypeCastTree)exp.getLeaf()).getExpression());
                }
                TypeMirror right = cInfo.getTrees().getTypeMirror(exp);
                if (right == null) {
                    return null;
                }
                if (right.getKind() == TypeKind.ERROR) {
                    right = cInfo.getTrees().getOriginalType((ErrorType)right);
                }
                if (cInfo.getTypes().isAssignable(right, left) || !cInfo.getTypeUtilities().isCastable(right, left)) {
                    return null;
                }
                return left;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private String newVarName(int caretOffset, String suggestedName) {
        try {
            if (cInfo != null) {
                SourcePositions[] sourcePositions = new SourcePositions[1];
                TreeUtilities tu = cInfo.getTreeUtilities();
                StatementTree stmt = tu.parseStatement("{" + request.getInsertText() + "}", sourcePositions); //NOI18N
                if (Utilities.containErrors(stmt)) {
                    return null;
                }
                TreePath path = tu.pathFor(new TreePath(treePath, stmt), caretOffset + 1, sourcePositions[0]);
                TreePath decl = tu.getPathElementOfKind(Tree.Kind.VARIABLE, path);
                if (decl != null) {
                    final Scope s = tu.attributeTreeTo(stmt, scope, decl.getLeaf());
                    TypeMirror type = cInfo.getTrees().getTypeMirror(decl);
                    final Element element = cInfo.getTrees().getElement(decl);
                    final ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                        @Override
                        public boolean accept(Element e, TypeMirror t) {
                            switch(e.getKind()) {
                                case EXCEPTION_PARAMETER:
                                case LOCAL_VARIABLE:
                                case RESOURCE_VARIABLE:
                                case PARAMETER:
                                    return element != e;
                                default:
                                    return false;
                            }
                        }
                    };
                    Iterable<? extends Element> loc = new Iterable<Element>() {
                        @Override
                        public Iterator<Element> iterator() {
                            return new Iterator<Element>() {
                                private Iterator<? extends Element> localsIt = locals.iterator();
                                private Iterator<? extends Element> localVarsIt;
                                @Override
                                public boolean hasNext() {
                                    if (localsIt != null) {
                                        if (localsIt.hasNext()) {
                                            return true;
                                        }
                                        localsIt = null;
                                        localVarsIt = cInfo.getElementUtilities().getLocalVars(s, acceptor).iterator();
                                    }
                                    return localVarsIt.hasNext();
                                }
                                @Override
                                public Element next() {
                                    return localsIt != null ? localsIt.next() : localVarsIt.next();
                                }
                                @Override
                                public void remove() {
                                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                                }
                            };
                        }
                    };
                    String name = Utilities.varNameSuggestion(decl);
                    Iterator<String> names = Utilities.varNamesSuggestions(type, element.getKind(), ((VariableTree)decl.getLeaf()).getModifiers().getFlags(), name != null ? name : suggestedName, null, cInfo.getTypes(), cInfo.getElements(), loc, CodeStyle.getDefault(request.getComponent().getDocument())).iterator();
                    if (names.hasNext()) {
                        return names.next();
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String owningClassName(boolean fqn) {
        try {
            if (cInfo != null) {
                TreePath path = treePath;
                while ((path = Utilities.getPathElementOfKind (TreeUtilities.CLASS_TREE_KINDS, path)) != null) {
                    Element element = cInfo.getTrees().getElement(path);
                    if (element != null && (element.getKind().isClass() || element.getKind().isInterface())) {
                        Name name = fqn ? ((TypeElement) element).getQualifiedName() : ((TypeElement) element).getSimpleName();
                        if (name != null) {
                            return name.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String owningPackageName() {
        try {
            if (cInfo != null) {
                ExpressionTree packageName = treePath.getCompilationUnit().getPackageName();
                String result = packageName != null ? packageName.toString() : null;
                if (result != null && !result.equals(ERROR))
                    return result;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private String owningMethodName() {
        try {
            if (cInfo != null) {
                TreePath path = treePath;
                while ((path = Utilities.getPathElementOfKind (Tree.Kind.METHOD, path)) != null) {
                    MethodTree tree = (MethodTree) path.getLeaf();
                    String result = tree.getName().toString();
                    if (result.length() > 0)
                        return result;
                    path = path.getParentPath();
                }
                return null;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private TypeMirror uncaughtExceptionType(int caretOffset) {
        try {
            if (cInfo != null) {
                SourcePositions[] sourcePositions = new SourcePositions[1];
                TreeUtilities tu = cInfo.getTreeUtilities();
                StatementTree stmt = tu.parseStatement("{" + request.getInsertText() + "}", sourcePositions); //NOI18N
                if (Utilities.containErrors(stmt)) {
                    return null;
                }
                TreePath path = tu.pathFor(new TreePath(treePath, stmt), caretOffset + 1, sourcePositions[0]);
                path = tu.getPathElementOfKind(Tree.Kind.TRY, path);
                if (path != null && ((TryTree)path.getLeaf()).getBlock() != null) {
                    tu.attributeTree(stmt, scope);
                    Iterator<? extends TypeMirror> excs = tu.getUncaughtExceptions(new TreePath(path, ((TryTree)path.getLeaf()).getBlock())).iterator();
                    if (excs.hasNext()) {
                        return excs.next();
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private boolean containsDeclaredType(TypeMirror type) {
        switch(type.getKind()) {
        case ARRAY:
            return containsDeclaredType(((ArrayType)type).getComponentType());
        case DECLARED:
            return true;
        default:
            return false;
        }
    }

    private DeclaredType findIterableType(TypeMirror type) {
        if (type == null || type.getKind() != TypeKind.DECLARED) {
            return null;
        }
        TypeElement te = (TypeElement)((DeclaredType)type).asElement();
        if ("java.lang.Iterable".contentEquals(te.getQualifiedName())) { //NOI18N
            return (DeclaredType)type;
        }
        for (TypeMirror tm : cInfo.getTypes().directSupertypes(type)) {
            DeclaredType dt = findIterableType(tm);
            if (dt != null) {
                return dt;
            }
        }
        return null;
    }

    private boolean isSameType(TypeMirror t1, TypeMirror t2, Types types) {
        if (types.isSameType(t1, t2)) {
            return true;
        }
        if (t1.getKind().isPrimitive() && types.isSameType(types.boxedClass((PrimitiveType)t1).asType(), t2)) {
            return true;
        }
        return t2.getKind().isPrimitive() && types.isSameType(t1, types.boxedClass((PrimitiveType)t1).asType());
    }
    
    private TypeMirror resolveCapturedType(TypeMirror type) {
        if (type.getKind() == TypeKind.TYPEVAR) {
            WildcardType wildcard = SourceUtils.resolveCapturedType(type);
            if (wildcard != null) {
                return wildcard.getExtendsBound();
            }
        }
        return type;
    }
    
    private void initParsing(final boolean localsNeeded) {
        if (cInfo == null) {
            final JTextComponent c = request.getComponent();
            caretOffset = c.getSelectionStart();
            final Document doc = c.getDocument();
            final FileObject fo = NbEditorUtilities.getFileObject(doc);
            if (fo != null) {
                final AtomicBoolean cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (cInfo != null || cancel.get()) {
                                return;
                            }
                            CompilationController controller = (CompilationController) JavaSourceUtil.createControllerHandle(fo, caretOffset, null).getCompilationController();
                            controller.toPhase(JavaSource.Phase.RESOLVED);
                            final TreeUtilities tu = controller.getTreeUtilities();
                            int embeddedCaret = controller.getSnapshot().getEmbeddedOffset(caretOffset);
                            if (embeddedCaret == -1) {
                                return;
                            }
                            treePath = tu.pathFor(embeddedCaret);
                            scope = tu.scopeFor(embeddedCaret);
                            enclClass = scope.getEnclosingClass();
                            final boolean isStatic = enclClass != null ? tu.isStaticContext(scope) : false;
                            if (enclClass == null) {
                                CompilationUnitTree cut = treePath.getCompilationUnit();
                                Iterator<? extends Tree> it = cut.getTypeDecls().iterator();
                                if (it.hasNext()) {
                                    enclClass = (TypeElement)controller.getTrees().getElement(TreePath.getPath(cut, it.next()));
                                }
                            }
                            locals = new ArrayList<>();
                            typeVars = new ArrayList<>();
                            if (localsNeeded) {
                                final Trees trees = controller.getTrees();
                                final SourcePositions sp = trees.getSourcePositions();
                                final Collection<? extends Element> illegalForwardRefs = SourceUtils.getForwardReferences(treePath, embeddedCaret, sp, trees);
                                final Collection<CharSequence> illegalForwardRefNames = new HashSet<>(illegalForwardRefs.size());
                                for (Element element : illegalForwardRefs) {
                                    illegalForwardRefNames.add(element.getSimpleName());
                                }
                                final ExecutableElement method = scope.getEnclosingMethod();
                                ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                    @Override
                                    public boolean accept(Element e, TypeMirror t) {
                                        switch (e.getKind()) {
                                        case TYPE_PARAMETER:
                                            return true;
                                        case LOCAL_VARIABLE:
                                        case RESOURCE_VARIABLE:
                                        case EXCEPTION_PARAMETER:
                                        case PARAMETER:
                                            return (method == null || method == e.getEnclosingElement() || e.getModifiers().contains(Modifier.FINAL)) &&
                                                    !illegalForwardRefNames.contains(e.getSimpleName());
                                        case FIELD:
                                            if (e.getSimpleName().contentEquals("this")) { //NOI18N
                                                return !isStatic && e.asType().getKind() == TypeKind.DECLARED && ((DeclaredType)e.asType()).asElement() == enclClass;
                                            }
                                            if (e.getSimpleName().contentEquals("super")) { //NOI18N
                                                return false;
                                            }
                                            if (illegalForwardRefNames.contains(e.getSimpleName())) {
                                                return false;
                                            }
                                        default:
                                            return (!isStatic || e.getModifiers().contains(Modifier.STATIC)) && tu.isAccessible(scope, e, (DeclaredType)t);
                                        }
                                    }
                                };
                                for (Element element : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor)) {
                                    switch(element.getKind()) {
                                        case TYPE_PARAMETER:
                                            typeVars.add(element);
                                            break;
                                        default:
                                            locals.add(element);
                                    }
                                }
                            }
                            cInfo = controller;
                        } catch(IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }                        
                    }
                }, NbBundle.getMessage(JavaCodeTemplateProcessor.class, "JCT-init"), cancel, false); //NOI18N
            }
        }
    }
    
    public static final class Factory implements CodeTemplateProcessorFactory {
        
        @Override
        public CodeTemplateProcessor createProcessor(CodeTemplateInsertRequest request) {
            return new JavaCodeTemplateProcessor(request); 
        }        
    }
}
