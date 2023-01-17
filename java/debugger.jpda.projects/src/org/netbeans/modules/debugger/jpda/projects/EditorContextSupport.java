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

package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.BytecodeProvider;
import org.netbeans.spi.debugger.jpda.EditorContext.MethodArgument;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.debugger.jpda.Evaluator.Expression;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 * Non-UI support code for EditorContext implementation.
 * 
 * @author Martin Entlicher
 */
public final class EditorContextSupport {
    
    private static final Logger LOG = Logger.getLogger(EditorContextSupport.class.getName());
    
    private static final RequestProcessor scanningProcessor = new RequestProcessor("Debugger Context Scanning", 1);   // NOI18N
    
    private static final PreferredCCParser preferredCCParser = new PreferredCCParser();
    private static final FieldLNCache fieldLNCache = new FieldLNCache();

    private EditorContextSupport() {}
    
    public static TypeElement getTypeElement(CompilationController ci,
                                             String binaryName,
                                             String[] classExcludeNames) {
        ClassScanner cs = new ClassScanner(ci.getTrees(), ci.getElements(),
                                           binaryName, classExcludeNames);
        TypeElement te = cs.scan(ci.getCompilationUnit(), null);
        if (te != null) {
            return te;
        } else {
            return null;
        }
    }
    
    /**
     * Returns line number of given field in given class.
     *
     * @param url the url of file the class is defined in
     * @param className the name of class (or inner class) the field is
     *                  defined in
     * @param fieldName the name of field
     *
     * @return line number or -1
     */
    public static int getFieldLineNumber (
        String url,
        final String className,
        final String fieldName
    ) {
        Integer line = fieldLNCache.getLine(url, className, fieldName);
        if (line != null) {
            return line;
        }
        FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return -1;
        }
        Future<Integer> fi = getFieldLineNumber(file, className, fieldName);
        if (fi == null) {
            return -1;
        }
        try {
            line = fi.get();
            fieldLNCache.putLine(url, className, fieldName, file, line);
            return line;
        } catch (InterruptedException ex) {
            return -1;
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
            return -1;
        }
    }
    
    /**
     * @param fo
     * @param className
     * @param fieldName
     * @return <code>null</code> or Future with the line number
     */
    public static Future<Integer> getFieldLineNumber (
        final FileObject fo,
        final String className,
        final String fieldName
    ) {
        final String url = fo.toURL().toExternalForm();
        Integer line = fieldLNCache.getLine(url, className, fieldName);
        if (line != null) {
            return new DoneFuture<>(line);
        }
        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            return null;
        }
        final int[] result = new int[] {-1};

        try {
            final Future f = parseWhenScanFinishedReallyLazy(fo, new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController ci = retrieveController(resultIterator, fo);
                    if (ci == null) {
                        return;
                    }
                    if (!PreferredCCParser.toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        return;
                    }
                    Elements elms = ci.getElements();
                    TypeElement classElement = EditorContextSupport.getTypeElement(ci, className, null);
                    if (classElement == null) {
                        return ;
                    }
                    LineMap lineMap = ci.getCompilationUnit().getLineMap();
                    if (fieldName == null) {
                        // If no field name is provided, just find the beginning of the class:
                        SourcePositions positions =  ci.getTrees().getSourcePositions();
                        Tree tree = ci.getTrees().getTree(classElement);
                        int pos = (int)positions.getStartPosition(ci.getCompilationUnit(), tree);
                        if (pos == Diagnostic.NOPOS) {
                            LOG.warning(
                                    "No position for tree "+tree+" in "+className);
                            return;
                        }
                        
                        CharSequence text = ci.getSnapshot().getText();
                        int l = text.length();
                        while (pos < l && text.charAt(pos) != '{') {
                            pos++;
                        }
                        result[0] = (int) lineMap.getLineNumber(pos) + 1;
                        fieldLNCache.putLine(url, className, fieldName, fo, result[0]);
                        return ;
                    }
                    List classMemberElements = elms.getAllMembers(classElement);
                    for (Iterator it = classMemberElements.iterator(); it.hasNext(); ) {
                        Element elm = (Element) it.next();
                        if (elm.getKind() == ElementKind.FIELD) {
                            String name = ((VariableElement) elm).getSimpleName().toString();
                            if (name.equals(fieldName)) {
                                SourcePositions positions =  ci.getTrees().getSourcePositions();
                                Tree tree = ci.getTrees().getTree(elm);
                                int pos = (int)positions.getStartPosition(ci.getCompilationUnit(), tree);
                                if (pos == Diagnostic.NOPOS) {
                                    LOG.warning(
                                            "No position for tree "+tree+" of element "+elm+" in "+className);
                                    continue;
                                }
                                result[0] = (int) lineMap.getLineNumber(pos);
                                fieldLNCache.putLine(url, className, fieldName, fo, result[0]);
                                //return elms.getSourcePosition(elm).getLine();
                            }
                        }
                    }
                }
            });
            if (!f.isDone()) {
                return new Future<Integer>() {

                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        return f.cancel(mayInterruptIfRunning);
                    }

                    @Override
                    public boolean isCancelled() {
                        return f.isCancelled();
                    }

                    @Override
                    public boolean isDone() {
                        return f.isDone();
                    }

                    @Override
                    public Integer get() throws InterruptedException, ExecutionException {
                        f.get();
                        return result[0];
                    }

                    @Override
                    public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        f.get(timeout, unit);
                        return result[0];
                    }

                };
            }
        } catch (ParseException pex) {
            Exceptions.printStackTrace(pex);
            return null;
        }
        return new DoneFuture<Integer>(result[0]);
    }
    
    /**
     * Returns line number of given method in given class.
     *
     * @param url the url of file the class is deined in
     * @param className the name of class (or innerclass) the method is
     *                  defined in
     * @param methodName the name of method
     * @param methodSignature the JNI-style signature of the method.
     *        If <code>null</code>, then the first method found is returned.
     *
     * @return line number or -1
     */
    public static int getMethodLineNumber (
        String url,
        final String className,
        final String methodName,
        final String methodSignature
    ) {
        FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return -1;
        }
        Future<int[]> flns = getMethodLineNumbers(file, className, null, methodName, methodSignature);
        if (flns == null) {
            return -1;
        }
        int[] lns;
        try {
            lns = flns.get();
        } catch (InterruptedException ex) {
            return -1;
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
            return -1;
        }
        if (lns.length == 0) {
            return -1;
        } else {
            return lns[0];
        }
    }

    /**
     * @param fo
     * @param className
     * @param classExcludeNames
     * @param methodName
     * @param methodSignature
     * @return <code>null</code> or Future with line numbers
     */
    public static Future<int[]> getMethodLineNumbers(
        final FileObject fo,
        final String className,
        final String[] classExcludeNames,
        final String methodName,
        final String methodSignature
    ) {
        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            return null;
        }
        final List<Integer> result = new ArrayList<Integer>();
        try {
            final Future f = parseWhenScanFinishedReallyLazy(fo, new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController ci = retrieveController(resultIterator, fo);
                    if (ci == null) {
                        return;
                    }
                    if (!PreferredCCParser.toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        return;
                    }
                    TypeElement classElement = EditorContextSupport.getTypeElement(ci, className, classExcludeNames);
                    if (classElement == null) {
                        return ;
                    }
                    LineMap lineMap = ci.getCompilationUnit().getLineMap();
                    List classMemberElements = ci.getElements().getAllMembers(classElement);
                    for (Iterator it = classMemberElements.iterator(); it.hasNext(); ) {
                        Element elm = (Element) it.next();
                        if (elm.getKind() == ElementKind.METHOD || elm.getKind() == ElementKind.CONSTRUCTOR) {
                            String name;
                            if (elm.getKind() == ElementKind.CONSTRUCTOR && !methodName.equals("<init>")) {
                                name = elm.getEnclosingElement().getSimpleName().toString();
                            } else {
                                name = elm.getSimpleName().toString();
                            }
                            if (name.equals(methodName)) {
                                if (methodSignature == null || egualMethodSignatures(methodSignature, createSignature((ExecutableElement) elm, ci.getTypes()))) {
                                    SourcePositions positions =  ci.getTrees().getSourcePositions();
                                    Tree tree = ci.getTrees().getTree(elm);
                                    if (tree == null) {
                                        LOG.warning(
                                                "Null tree for element "+elm+" in "+className);
                                        continue;
                                    }
                                    int pos = (int)positions.getStartPosition(ci.getCompilationUnit(), tree);
                                    if (pos == Diagnostic.NOPOS) {
                                        LOG.warning(
                                                "No position for tree "+tree+" of element "+elm+" in "+className);
                                        continue;
                                    }
                                    { // Find the method name
                                        int origPos = pos;
                                        if (tree.getKind() == Tree.Kind.METHOD) {
                                            MethodTree mt = (MethodTree) tree;
                                            ModifiersTree modt = mt.getModifiers();
                                            if (modt != null) {
                                                List<? extends AnnotationTree> annotations = modt.getAnnotations();
                                                if (annotations != null && annotations.size() > 0) {
                                                    pos = (int) positions.getEndPosition(ci.getCompilationUnit(), annotations.get(annotations.size() - 1));
                                                    if (pos == Diagnostic.NOPOS) {
                                                        LOG.warning(
                                                                "No position for tree "+annotations.get(annotations.size() - 1)+" in "+className);
                                                        continue;
                                                    }
                                                }
                                            }
                                        }
                                        String text = ci.getText();
                                        int l = text.length();
                                        char c = 0;
                                        while (pos < l && (c = text.charAt(pos)) != '(' && c != ')') {
                                            pos++;
                                        }
                                        if (pos >= l) {
                                            // We went somewhere wrong. Re-initialize original values
                                            c = 0;
                                            pos = origPos;
                                        }
                                        if (c == '(') {
                                            pos--;
                                            while (pos > 0 && Character.isWhitespace(text.charAt(pos))) {
                                                pos--;
                                            }
                                        }
                                    }
                                    result.add((int)lineMap.getLineNumber(pos));
                                }
                            }
                        }
                    }
                }
            });
            if (!f.isDone()) {
                return new Future<int[]>() {

                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        return f.cancel(mayInterruptIfRunning);
                    }

                    @Override
                    public boolean isCancelled() {
                        return f.isCancelled();
                    }

                    @Override
                    public boolean isDone() {
                        return f.isDone();
                    }

                    @Override
                    public int[] get() throws InterruptedException, ExecutionException {
                        f.get();
                        return getResultArray();
                    }

                    @Override
                    public int[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        f.get(timeout, unit);
                        return getResultArray();
                    }

                    private int[] getResultArray() {
                        final int[] resultArray = new int[result.size()];
                        for (int i = 0; i < resultArray.length; i++) {
                            resultArray[i] = result.get(i).intValue();
                        }
                        return resultArray;
                    }

                };
            }
        } catch (ParseException pex) {
            Exceptions.printStackTrace(pex);
            return null;
        }
        int[] resultArray = new int[result.size()];
        for (int i = 0; i < resultArray.length; i++) {
            resultArray[i] = result.get(i).intValue();
        }
        return new DoneFuture<int[]>(resultArray);
    }

    private static boolean egualMethodSignatures(String s1, String s2) {
        int i = s1.lastIndexOf(")");
        if (i > 0) {
            s1 = s1.substring(0, i);
        }
        i = s2.lastIndexOf(")");
        if (i > 0) {
            s2 = s2.substring(0, i);
        }
        return s1.equals(s2);
    }

    private static String createSignature(ExecutableElement elm, Types types) {
        StringBuilder signature = new StringBuilder("(");
        for (VariableElement param : elm.getParameters()) {
            TypeMirror pt = param.asType();
            pt = types.erasure(pt);
            String paramType = getTypeBinaryName(pt);
            signature.append(getSignature(paramType));
        }
        signature.append(')');
        String returnType = getTypeBinaryName(types.erasure(elm.getReturnType()));
        signature.append(getSignature(returnType));
        return signature.toString();
    }
    
    private static String getTypeBinaryName(TypeMirror t) {
        if (t instanceof ArrayType) {
            TypeMirror ct = ((ArrayType) t).getComponentType();
            return getTypeBinaryName(ct)+"[]";
        }
        if (t instanceof DeclaredType) {
            return ElementUtilities.getBinaryName((TypeElement) ((DeclaredType) t).asElement());
        }
        return t.toString();
    }

    private static String getSignature(String javaType) {
        if (javaType.equals("boolean")) {
            return "Z";
        } else if (javaType.equals("byte")) {
            return "B";
        } else if (javaType.equals("char")) {
            return "C";
        } else if (javaType.equals("short")) {
            return "S";
        } else if (javaType.equals("int")) {
            return "I";
        } else if (javaType.equals("long")) {
            return "J";
        } else if (javaType.equals("float")) {
            return "F";
        } else if (javaType.equals("double")) {
            return "D";
        } else if (javaType.equals("void")) {
            return "V";
        } else if (javaType.endsWith("[]")) {
            return "["+getSignature(javaType.substring(0, javaType.length() - 2));
        } else {
            return "L"+javaType.replace('.', '/')+";";
        }
    }

    /**
     * @param fo
     * @param className
     * @param classExcludeNames
     * @return <code>null</code> or Future with line number
     */
    public static Future<Integer> getClassLineNumber(
        final FileObject fo,
        final String className,
        final String[] classExcludeNames
    ) {
        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            return null;
        }
        final Integer[] result = new Integer[] { null };
        try {
            final Future f = parseWhenScanFinishedReallyLazy(fo, new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController ci = retrieveController(resultIterator, fo);
                    if (ci == null) {
                        return;
                    }
                    if (!PreferredCCParser.toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        return;
                    }
                    TypeElement classElement = EditorContextSupport.getTypeElement(ci, className, classExcludeNames);
                    if (classElement == null) {
                        return ;
                    }
                    SourcePositions positions =  ci.getTrees().getSourcePositions();
                    Tree tree = ci.getTrees().getTree(classElement);
                    if (tree == null) {
                        LOG.warning(
                                "Null tree for element "+classElement+" in "+className);
                        return;
                    }
                    int pos = (int)positions.getStartPosition(ci.getCompilationUnit(), tree);
                    if (pos == Diagnostic.NOPOS) {
                        LOG.warning(
                                "No position for tree "+tree+" of element "+classElement+" ("+className+")");
                        return;
                    }
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
                        boolean shifted = false;
                        ModifiersTree mtree = ((ClassTree) tree).getModifiers();
                        for (AnnotationTree atree : mtree.getAnnotations()) {
                            int aend = (int) positions.getEndPosition(ci.getCompilationUnit(), atree);
                            if (aend != Diagnostic.NOPOS && pos < aend) {
                                shifted = true;
                                pos = aend + 1;
                            }
                        }
                        if (shifted) {
                            String text = ci.getText();
                            int l = text.length();
                            while (pos < l && Character.isWhitespace(text.charAt(pos))) {
                                pos++;
                            }
                        }
                    }
                    LineMap lineMap = ci.getCompilationUnit().getLineMap();
                    result[0] = (int)lineMap.getLineNumber(pos);
                }
            });
            if (!f.isDone()) {
                return new Future<Integer>() {

                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        return f.cancel(mayInterruptIfRunning);
                    }

                    @Override
                    public boolean isCancelled() {
                        return f.isCancelled();
                    }

                    @Override
                    public boolean isDone() {
                        return f.isDone();
                    }

                    @Override
                    public Integer get() throws InterruptedException, ExecutionException {
                        f.get();
                        return result[0];
                    }

                    @Override
                    public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        f.get(timeout, unit);
                        return result[0];
                    }

                };
            }
        } catch (ParseException pex) {
            Exceptions.printStackTrace(pex);
            return null;
        }
        return new DoneFuture<Integer>(result[0]);
    }

    /**
     * Returns binary class name for given url and line number or null.
     *
     * @param url a url
     * @param lineNumber a line number
     *
     * @return binary class name for given url and line number or null
     */
    public static String getClassName (
        String url,
        final int lineNumber
    ) {
        final FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return null;
        }
        JavaSource js = JavaSource.forFileObject(file);
        if (js == null) {
            return "";
        }
        try {
            final String[] result = new String[] {""};
            ParserManager.parse(Collections.singleton(Source.create(file)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController ci = retrieveController(resultIterator, file);
                    if (ci == null) {
                        return;
                    }
                    if (!PreferredCCParser.toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        return;
                    }
                    LineMap lineMap = ci.getCompilationUnit().getLineMap();
                    int offset;
                    try {
                        offset = (int) lineMap.getStartPosition(lineNumber);
                    } catch (IndexOutOfBoundsException ioobex) {
                        return ;
                    }
                    TreePath p = ci.getTreeUtilities().pathFor(offset);
                    while  (p != null && !TreeUtilities.CLASS_TREE_KINDS.contains(p.getLeaf().getKind())) {
                        p = p.getParentPath();
                    }
                    TypeElement te;
                    if (p != null) {
                        te = (TypeElement) ci.getTrees().getElement(p);
                    } else {
                        Scope scope = ci.getTreeUtilities().scopeFor(offset);
                        te = scope.getEnclosingClass();
                    }
                    if (te != null) {
                        result[0] = ElementUtilities.getBinaryName(te);
                    } else {
                        LOG.warning(
                                "No enclosing class for "+ci.getFileObject()+", offset = "+offset);
                    }
                }
            });
            return result[0];
        } catch (ParseException pex) {
            Exceptions.printStackTrace(pex);
            return "";
        }
        /*
        SourceCookie.Editor sc = (SourceCookie.Editor) dataObject.getCookie
            (SourceCookie.Editor.class);
        if (sc == null) return null;
        StyledDocument sd = null;
        try {
            sd = sc.openDocument ();
        } catch (IOException ex) {
        }
        if (sd == null) return null;
        int offset;
        try {
            offset = NbDocument.findLineOffset (sd, lineNumber - 1);
        } catch (IndexOutOfBoundsException ioobex) {
            return null;
        }
        Element element = sc.findElement (offset);

        if (element == null) return "";
        if (element instanceof ClassElement)
            return getClassName ((ClassElement) element);
        if (element instanceof ConstructorElement)
            return getClassName (((ConstructorElement) element).getDeclaringClass ());
        if (element instanceof FieldElement)
            return getClassName (((FieldElement) element).getDeclaringClass ());
        if (element instanceof InitializerElement)
            return getClassName (((InitializerElement) element).getDeclaringClass());
        return "";
         */
    }

    public static String getClassDeclaredAt(FileObject fo, final int currentOffset) {
        final String[] currentClassPtr = new String[] { null };
        final Future<Void> scanFinished;
        try {
            scanFinished = runWhenScanFinishedReallyLazy(fo, new CancellableTask<CompilationController>() {
                @Override
                public void cancel() {
                }
                @Override
                public void run(CompilationController ci) throws Exception {
                    if (!PreferredCCParser.toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        return;
                    }
                    int offset = currentOffset;
                    //Scope scope = ci.getTreeUtilities().scopeFor(offset);
                    String text = ci.getText();
                    int l = text.length();
                    char c = 0;
                    while (offset < l && (c = text.charAt(offset)) != '{' && c != '}' && c != '\n' && c != '\r') {
                        offset++;
                    }
                    if (offset >= l) {
                        return ;
                    }
                    offset--;
                    TreePath path = ci.getTreeUtilities().pathFor(offset);
                    Tree tree;
                    do {
                        tree = path.getLeaf();
                        if (!TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
                            path = path.getParentPath();
                            if (path == null) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } while (true);
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
                        SourcePositions positions =  ci.getTrees().getSourcePositions();
                        int pos = (int) positions.getStartPosition(ci.getCompilationUnit(), tree);
                        if (pos == Diagnostic.NOPOS) {
                            return ; // We do not know where we are!
                        }
                        if (offset < pos) {
                            return ; // We are before the class declaration!
                        }
                        int hend = getHeaderEnd((ClassTree) tree, positions, ci.getCompilationUnit());
                        if (hend > 0) {
                            pos = hend;
                        }
                        while (pos < l && text.charAt(pos) != '{') {
                            pos++;
                        }
                        if (pos < offset) { // We are after the class declaration!
                            return ;
                        }
                        Element el = ci.getTrees().getElement(ci.getTrees().getPath(ci.getCompilationUnit(), tree));
                        if (el != null && (el.getKind() == ElementKind.CLASS || el.getKind() == ElementKind.INTERFACE)) {
                            currentClassPtr[0] = ElementUtilities.getBinaryName((TypeElement) el);
                        }
                    }
                }

                private int getHeaderEnd(ClassTree classTree, SourcePositions positions, CompilationUnitTree compilationUnit) {
                    int max = -1;
                    int pos = (int) positions.getEndPosition(compilationUnit, classTree.getExtendsClause());
                    if (pos != Diagnostic.NOPOS) {
                        max = Math.max(max, pos);
                    }
                    pos = (int) positions.getEndPosition(compilationUnit, classTree.getModifiers());
                    if (pos != Diagnostic.NOPOS) {
                        max = Math.max(max, pos);
                    }
                    for (Tree t : classTree.getImplementsClause()) {
                        pos = (int) positions.getEndPosition(compilationUnit, t);
                        if (pos != Diagnostic.NOPOS) {
                            max = Math.max(max, pos);
                        }
                    }
                    for (Tree t : classTree.getTypeParameters()) {
                        pos = (int) positions.getEndPosition(compilationUnit, t);
                        if (pos != Diagnostic.NOPOS) {
                            max = Math.max(max, pos);
                        }
                    }
                    return max;
                }
            }, true);
            if (!scanFinished.isDone()) {
                if (java.awt.EventQueue.isDispatchThread()) {
                    // Hack: We should not wait for the scan in AWT!
                    //       Thus we throw IllegalComponentStateException,
                    //       which returns the data upon call to getMessage()
                    throw new java.awt.IllegalComponentStateException() {

                        private void waitScanFinished() {
                            try {
                                scanFinished.get();
                            } catch (InterruptedException iex) {
                            } catch (java.util.concurrent.ExecutionException eex) {
                                Exceptions.printStackTrace(eex);
                            }
                        }

                        @Override
                        public String getMessage() {
                            waitScanFinished();
                            return currentClassPtr[0];
                        }

                    };
                } else {
                    try {
                        scanFinished.get();
                    } catch (InterruptedException iex) {
                        return null;
                    } catch (java.util.concurrent.ExecutionException eex) {
                        Exceptions.printStackTrace(eex);
                        return null;
                    }
                }
            }
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
            return null;
        }
        return currentClassPtr[0];
    }

    /** @return { "method name", "method signature", "enclosing class name" }
     */
    public static String[] getMethodDeclaredAt(FileObject fo, final int currentOffset) {
        final String[] currentMethodPtr = new String[] { null, null, null };
        final Future<Void> scanFinished;
        try {
            scanFinished = runWhenScanFinishedReallyLazy(fo, new CancellableTask<CompilationController>() {
                @Override
                public void cancel() {
                }
                @Override
                public void run(CompilationController ci) throws Exception {
                    if (!PreferredCCParser.toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        return;
                    }
                    int offset = currentOffset;
                    //Scope scope = ci.getTreeUtilities().scopeFor(offset);
                    String text = ci.getText();
                    int l = text.length();
                    char c = 0;
                    while (offset < l && (c = text.charAt(offset)) != '(' && c != ')' && c != '\n' && c != '\r') {
                        offset++;
                    }
                    if (offset >= l) {
                        return ;
                    }
                    if (c == '(') {
                        offset--;
                    }

                    Tree tree = ci.getTreeUtilities().pathFor(offset).getLeaf();
                    if (tree.getKind() == Tree.Kind.METHOD) {
                        Element el = ci.getTrees().getElement(ci.getTrees().getPath(ci.getCompilationUnit(), tree));

                        //Element el = ci.getTrees().getElement(ci.getTreeUtilities().pathFor(offset));
                        if (el != null && (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR)) {
                            currentMethodPtr[0] = el.getSimpleName().toString();
                            if (currentMethodPtr[0].equals("<init>")) {
                                // The constructor name is the class name:
                                currentMethodPtr[0] = el.getEnclosingElement().getSimpleName().toString();
                            }
                            currentMethodPtr[1] = createSignature((ExecutableElement) el, ci.getTypes());
                            Element enclosingClassElement = el;
                            TypeElement te = null; // SourceUtils.getEnclosingTypeElement(el);
                            while (enclosingClassElement != null) {
                                ElementKind kind = enclosingClassElement.getKind();
                                if (kind == ElementKind.CLASS || kind == ElementKind.INTERFACE) {
                                    te = (TypeElement) enclosingClassElement;
                                    break;
                                } else {
                                    enclosingClassElement = enclosingClassElement.getEnclosingElement();
                                }
                            }
                            if (te != null) {
                                currentMethodPtr[2] = ElementUtilities.getBinaryName(te);
                            }
                        }
                    }
                }
            }, true);
            if (!scanFinished.isDone()) {
                if (java.awt.EventQueue.isDispatchThread()) {
                    // Hack: We should not wait for the scan in AWT!
                    //       Thus we throw IllegalComponentStateException,
                    //       which returns the data upon call to getMessage()
                    throw new java.awt.IllegalComponentStateException() {

                        private void waitScanFinished() {
                            try {
                                scanFinished.get();
                            } catch (InterruptedException iex) {
                            } catch (java.util.concurrent.ExecutionException eex) {
                                Exceptions.printStackTrace(eex);
                            }
                        }

                        @Override
                        public String getMessage() {
                            waitScanFinished();
                            return currentMethodPtr[0];
                        }

                        @Override
                        public String getLocalizedMessage() {
                            waitScanFinished();
                            return currentMethodPtr[1];
                        }
                    };
                } else {
                    try {
                        scanFinished.get();
                    } catch (InterruptedException iex) {
                        return null;
                    } catch (java.util.concurrent.ExecutionException eex) {
                        Exceptions.printStackTrace(eex);
                        return null;
                    }
                }
            }
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
            return null;
        }
        if (currentMethodPtr[0] != null) {
            return currentMethodPtr;
        } else {
            return null;
        }
    }
    
    static CompilationController retrieveController(ResultIterator resIt, FileObject fo) throws ParseException {
        Parser.Result res = resIt.getParserResult();
        CompilationController ci = res != null ? CompilationController.get(res) : null;
        if (ci == null) {
            LOG.warning("Unable to get compilation controller " + fo);
        }
        return ci;
    }

    public static EditorContext.Operation[] computeOperations(CompilationController ci,
                                                              int offset,
                                                              int lineNumber,
                                                              EditorContext.BytecodeProvider bytecodeProvider,
                                                              ASTOperationCreationDelegate opCreationDelegate) throws IOException {
        if (!PreferredCCParser.toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {//TODO: ELEMENTS_RESOLVED may be sufficient
            return new EditorContext.Operation[] {};
        }
        // We need the enclosing statement/block
        Tree statementTree = findStatementInScope(ci.getTreeUtilities().pathFor(offset));
        LOG.log(Level.FINE, "Statement tree found at line {0}:\n{1}\n", new Object[]{ lineNumber, statementTree });
        if (statementTree == null) {
            Scope scope = ci.getTreeUtilities().scopeFor(offset);
            Element method = scope.getEnclosingMethod();
            if (method == null) {
                return new EditorContext.Operation[] {};
            }
            statementTree = ci.getTrees().getTree(method);
        }
        if (statementTree == null) { // method not found
            return new EditorContext.Operation[] {};
        }
        CompilationUnitTree cu = ci.getCompilationUnit();
        SourcePositions sp = ci.getTrees().getSourcePositions();
        int statementStart = (int) cu.getLineMap().getLineNumber(sp.getStartPosition(cu, statementTree));
        int statementEnd = (int) cu.getLineMap().getLineNumber(sp.getEndPosition(cu, statementTree));
        ExpressionScanner scanner = new ExpressionScanner(lineNumber, statementStart, statementEnd,
                                                          cu, ci.getTrees().getSourcePositions());
        ExpressionScanner.ExpressionsInfo info = new ExpressionScanner.ExpressionsInfo();
        List<Tree> expTrees = statementTree.accept(scanner, info);

        LOG.log(Level.FINE, "expression trees = {0}", expTrees);
        
        //com.sun.source.tree.ExpressionTree expTree = scanner.getExpressionTree();
        if (expTrees == null || expTrees.isEmpty()) {
            return new EditorContext.Operation[] {};
        }
        int treeStartLine = Integer.MAX_VALUE;
        int treeEndLine = 0;
        for (int i = 0; i < expTrees.size(); i++) {
            Tree tree = expTrees.get(i);
            int start = (int) cu.getLineMap().getLineNumber(
                sp.getStartPosition(cu, tree));
            int end = (int) cu.getLineMap().getLineNumber(
                sp.getEndPosition(cu, tree));
            if (start == Diagnostic.NOPOS || end == Diagnostic.NOPOS) {
                continue;
            }
            if (start < treeStartLine) {
                treeStartLine = start;
            }
            if (end > treeEndLine) {
                treeEndLine = end;
            }
        }
        if (treeStartLine == Integer.MAX_VALUE) {
            return null;
        }
        //t3 = System.nanoTime();
        int[] indexes = bytecodeProvider.indexAtLines(treeStartLine, treeEndLine);
        if (indexes == null) {
            return null;
        }
        Map<Tree, EditorContext.Operation> nodeOperations = new HashMap<Tree, EditorContext.Operation>();
        EditorContext.Operation[] ops = AST2Bytecode.matchSourceTree2Bytecode(
                cu,
                ci,
                expTrees, info, bytecodeProvider.byteCodes(),
                indexes,
                bytecodeProvider.constantPool(),
                opCreationDelegate,
                nodeOperations);
        if (ops != null) {
            assignNextOperations(statementTree, cu, ci,
                                 bytecodeProvider, opCreationDelegate,
                                 expTrees, info, nodeOperations);
        }
        return ops;
    }
    
    private static void assignNextOperations(Tree methodTree,
                                             CompilationUnitTree cu,
                                             CompilationController ci,
                                             EditorContext.BytecodeProvider bytecodeProvider,
                                             ASTOperationCreationDelegate opCreationDelegate,
                                             List<Tree> treeNodes,
                                             ExpressionScanner.ExpressionsInfo info,
                                             Map<Tree, EditorContext.Operation> nodeOperations) {
        int length = treeNodes.size();
        for (int treeIndex = 0; treeIndex < length; treeIndex++) {
            Tree node = treeNodes.get(treeIndex);
            Set<Tree> nextNodes = info.getNextExpressions(node);
            if (nextNodes != null) {
                EditorContext.Operation op = nodeOperations.get(node);
                if (op == null) {
                    for (int backIndex = treeIndex - 1; backIndex >= 0; backIndex--) {
                        node = treeNodes.get(backIndex);
                        op = nodeOperations.get(node);
                        if (op != null) {
                            break;
                        }
                    }
                }
                if (op != null) {
                    for (Tree t : nextNodes) {
                        EditorContext.Operation nextOp = nodeOperations.get(t);
                        if (nextOp == null) {
                            SourcePositions sp = ci.getTrees().getSourcePositions();
                            int treeStartLine =
                                    (int) cu.getLineMap().getLineNumber(
                                        sp.getStartPosition(cu, t));
                            if (treeStartLine == Diagnostic.NOPOS) {
                                continue;
                            }
                            int treeEndLine =
                                    (int) cu.getLineMap().getLineNumber(
                                        sp.getEndPosition(cu, t));
                            if (treeEndLine == Diagnostic.NOPOS) {
                                continue;
                            }
                            ExpressionScanner scanner = new ExpressionScanner(treeStartLine, treeStartLine, treeEndLine,
                                                                              cu, ci.getTrees().getSourcePositions());
                            ExpressionScanner.ExpressionsInfo newInfo = new ExpressionScanner.ExpressionsInfo();
                            List<Tree> newExpTrees = methodTree.accept(scanner, newInfo);
                            if (newExpTrees == null) {
                                continue;
                            }
                            treeStartLine =
                                    (int) cu.getLineMap().getLineNumber(
                                        sp.getStartPosition(cu, newExpTrees.get(0)));
                            treeEndLine =
                                    (int) cu.getLineMap().getLineNumber(
                                        sp.getEndPosition(cu, newExpTrees.get(newExpTrees.size() - 1)));

                            if (treeStartLine == Diagnostic.NOPOS || treeEndLine == Diagnostic.NOPOS) {
                                continue;
                            }
                            int[] indexes = bytecodeProvider.indexAtLines(treeStartLine, treeEndLine);
                            Map<Tree, EditorContext.Operation> newNodeOperations = new HashMap<Tree, EditorContext.Operation>();
                            /*Operation[] newOps = */AST2Bytecode.matchSourceTree2Bytecode(
                                    cu,
                                    ci,
                                    newExpTrees, newInfo, bytecodeProvider.byteCodes(),
                                    indexes,
                                    bytecodeProvider.constantPool(),
                                    opCreationDelegate,
                                    newNodeOperations);
                            nextOp = newNodeOperations.get(t);
                            if (nextOp == null) {
                                // Next operation not found
                                System.err.println("Next operation not found!");
                                continue;
                            }
                        }
                        opCreationDelegate.addNextOperationTo(op, nextOp);
                    }
                }
            }
        }

    }

    public static MethodArgument[] computeMethodArguments(CompilationController ci,
                                                          EditorContext.Operation operation,
                                                          ASTOperationCreationDelegate opCreationDelegate)
                                                            throws IOException {
        EditorContext.MethodArgument args[];
        if (!PreferredCCParser.toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {
            return null;
        }
        int offset = operation.getMethodEndPosition().getOffset();
        Scope scope = ci.getTreeUtilities().scopeFor(offset);
        Element method = scope.getEnclosingMethod();
        if (method == null) {
            return null;
        }
        Tree methodTree = ci.getTrees().getTree(method);
        CompilationUnitTree cu = ci.getCompilationUnit();
        MethodArgumentsScanner scanner =
                new MethodArgumentsScanner(offset, cu, ci.getTrees().getSourcePositions(), true,
                                           opCreationDelegate);
        args = methodTree.accept(scanner, null);
        args = scanner.getArguments();
        return args;
    }

    public static MethodArgument[] computeMethodArguments(CompilationController ci,
                                                          int methodLineNumber,
                                                          int offset,
                                                          ASTOperationCreationDelegate opCreationDelegate)
                                                            throws IOException {
        MethodArgument args[];
        if (!PreferredCCParser.toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {
            return null;
        }
        Scope scope = ci.getTreeUtilities().scopeFor(offset);
        Element clazz = scope.getEnclosingClass();
        if (clazz == null) {
            return null;
        }
        Tree methodTree = ci.getTrees().getTree(clazz);
        CompilationUnitTree cu = ci.getCompilationUnit();
        MethodArgumentsScanner scanner =
                new MethodArgumentsScanner(methodLineNumber, cu, ci.getTrees().getSourcePositions(), false,
                                           opCreationDelegate);
        args = methodTree.accept(scanner, null);
        args = scanner.getArguments();
        return args;
    }

    private static Tree findStatementInScope(TreePath tp) {
        Tree tree = tp.getLeaf();
        Tree.Kind kind = tree.getKind();
        switch (kind) {
            case BLOCK:
            case EXPRESSION_STATEMENT:
            case LAMBDA_EXPRESSION:
            case METHOD:
                return tree;
        }
        tp = tp.getParentPath();
        if (tp == null) {
            return null;
        } else {
            return findStatementInScope(tp);
        }
    }

    /** throws IllegalComponentStateException when can not return the data in AWT. */
    public static String getCurrentElement(FileObject fo, final int currentOffset, final String selectedIdentifier,
                                           final ElementKind kind, final String[] elementSignaturePtr)
                                                throws java.awt.IllegalComponentStateException {

        if (fo == null) {
            return null;
        }
        final String[] currentElementPtr = new String[] { null };
        final Future<Void> scanFinished;
        try {
            scanFinished = runWhenScanFinishedReallyLazy(fo, new CancellableTask<CompilationController>() {
                @Override
                public void cancel() {
                }
                @Override
                public void run(CompilationController ci) throws Exception {
                    if (!PreferredCCParser.toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        return;
                    }
                    Element el = null;
                    if (kind == ElementKind.CLASS) {
                        boolean isMemberClass = false;
                        if (selectedIdentifier != null) {
                            Tree tree = ci.getTreeUtilities().pathFor(currentOffset).getLeaf();
                            if (tree.getKind() == Tree.Kind.MEMBER_SELECT) {
                                MemberSelectTree mst = (MemberSelectTree) tree;
                                el = ci.getTrees().getElement(ci.getTrees().getPath(ci.getCompilationUnit(), mst.getExpression()));
                                if (el != null) {
                                    TypeMirror tm = el.asType();
                                    if (tm.getKind() == TypeKind.DECLARED) {
                                        currentElementPtr[0] = tm.toString();
                                        isMemberClass = true;
                                    }
                                }
                            }
                        }
                        if (!isMemberClass) {
                            TreePath currentPath = ci.getTreeUtilities().pathFor(currentOffset);
                            Tree tree = currentPath.getLeaf();
                            TypeElement te;
                            if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
                                te = (TypeElement) ci.getTrees().getElement(currentPath);
                            } else {
                                Scope scope = ci.getTreeUtilities().scopeFor(currentOffset);
                                te = scope.getEnclosingClass();
                            }
                            if (te != null) {
                                currentElementPtr[0] = ElementUtilities.getBinaryName(te);
                            }
                            el = te;
                        }
                    } else if (kind == ElementKind.METHOD) {
                        Scope scope = ci.getTreeUtilities().scopeFor(currentOffset);
                        el = scope.getEnclosingMethod();
                        if (el != null) {
                            currentElementPtr[0] = el.getSimpleName().toString();
                            if (currentElementPtr[0].equals("<init>")) {
                                // The constructor name is the class name:
                                currentElementPtr[0] = el.getEnclosingElement().getSimpleName().toString();
                            }
                        } else {
                            TreePath path = ci.getTreeUtilities().pathFor(currentOffset);
                            Tree tree = path != null ? path.getLeaf() : null;
                            while (tree != null && !(tree instanceof MethodTree || tree instanceof ClassTree)) {
                                path = path.getParentPath();
                                tree = path != null ? path.getLeaf() : null;
                            }
                            if (tree instanceof MethodTree) {
                                String name = ((MethodTree)tree).getName().toString();
                                if (name.equals("<init>") && scope.getEnclosingClass() != null) {
                                    name = scope.getEnclosingClass().getSimpleName().toString();
                                }
                                currentElementPtr[0] = name;
                            }
                        }
                    } else if (kind == ElementKind.FIELD) {
                        int offset = currentOffset;

                        if (selectedIdentifier == null) {
                            String text = ci.getText();
                            int l = text.length();
                            char c = 0; // Search for the end of the field declaration
                            while (offset < l && (c = text.charAt(offset)) != '\n' && c != '\r' && Character.isWhitespace(c)) {
                                offset++;
                            }
                            if (!Character.isWhitespace(c)) {
                                offset++;
                            }
                        }
                        TreePath tp = ci.getTreeUtilities().pathFor(offset);
                        Tree tree = tp.getLeaf();
                        if (selectedIdentifier == null) {
                            while (tree.getKind() != Tree.Kind.VARIABLE) {
                                tp = tp.getParentPath();
                                if (tp == null) {
                                    break;
                                }
                                tree = tp.getLeaf();
                                if (tree.getKind() == Tree.Kind.METHOD ||
                                    tree.getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
                                    break; // We're inside a method, do not search for fields here.
                                }
                            }
                        }
                        if (tree.getKind() == Tree.Kind.VARIABLE) {
                            el = ci.getTrees().getElement(ci.getTrees().getPath(ci.getCompilationUnit(), tree));
                            if (el != null && (el.getKind() == ElementKind.FIELD || el.getKind() == ElementKind.ENUM_CONSTANT)) {
                                currentElementPtr[0] = ((VariableTree) tree).getName().toString();
                            }
                        } else if (tree.getKind() == Tree.Kind.IDENTIFIER && selectedIdentifier != null) {
                            IdentifierTree it = (IdentifierTree) tree;
                            String fieldName = it.getName().toString();
                            Scope scope = ci.getTreeUtilities().scopeFor(offset);
                            TypeElement te = scope.getEnclosingClass();
                            if (te != null) {
                                List<? extends Element> enclosedElms = te.getEnclosedElements();
                                for (Element elm : enclosedElms) {
                                    if (elm.getKind() == ElementKind.FIELD && elm.getSimpleName().contentEquals(fieldName)) {
                                        currentElementPtr[0] = fieldName;
                                        break;
                                    }
                                }
                            }
                        } else if (tree.getKind() == Tree.Kind.MEMBER_SELECT && selectedIdentifier != null) {
                            MemberSelectTree mst = (MemberSelectTree) tree;
                            String fieldName = mst.getIdentifier().toString();
                            el = ci.getTrees().getElement(ci.getTrees().getPath(ci.getCompilationUnit(), mst.getExpression()));
                            if (el != null && el.asType().getKind() == TypeKind.DECLARED) {
                                List<? extends Element> enclosedElms = ((DeclaredType) el.asType()).asElement().getEnclosedElements();
                                for (Element elm : enclosedElms) {
                                    if (elm.getKind() == ElementKind.FIELD && elm.getSimpleName().contentEquals(fieldName)) {
                                        currentElementPtr[0] = fieldName;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (elementSignaturePtr != null && el instanceof ExecutableElement) {
                        elementSignaturePtr[0] = createSignature((ExecutableElement) el, ci.getTypes());
                    }
                }
            }, true);
            if (!scanFinished.isDone()) {
                if (java.awt.EventQueue.isDispatchThread()) {
                    // Hack: We should not wait for the scan in AWT!
                    //       Thus we throw IllegalComponentStateException,
                    //       which returns the data upon call to getMessage()
                    throw new java.awt.IllegalComponentStateException() {

                        private void waitScanFinished() {
                            try {
                                scanFinished.get();
                            } catch (InterruptedException iex) {
                            } catch (java.util.concurrent.ExecutionException eex) {
                                Exceptions.printStackTrace(eex);
                            }
                        }

                        @Override
                        public String getMessage() {
                            waitScanFinished();
                            return currentElementPtr[0];
                        }

                    };
                } else {
                    try {
                        scanFinished.get();
                    } catch (InterruptedException iex) {
                        return null;
                    } catch (java.util.concurrent.ExecutionException eex) {
                        Exceptions.printStackTrace(eex);
                        return null;
                    }
                }
            }
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
            return null;
        }
        return currentElementPtr[0];
    }

    public static Operation[] getOperations(String url, final int lineNumber,
                                            BytecodeProvider bytecodeProvider,
                                            ASTOperationCreationDelegate opCreationDelegate) {
        return preferredCCParser.getOperations(url, lineNumber,
                                               bytecodeProvider,
                                               opCreationDelegate);
    }
    
    public static MethodArgument[] getArguments(String url,
                                                final EditorContext.Operation operation,
                                                final ASTOperationCreationDelegate opCreationDelegate) {
        return preferredCCParser.getArguments(url, operation, opCreationDelegate);
    }
    
    public static MethodArgument[] getArguments(String url,
                                                final int methodLineNumber,
                                                final ASTOperationCreationDelegate opCreationDelegate) {
        return preferredCCParser.getArguments(url, methodLineNumber, opCreationDelegate);
    }
    
    public static String[] getImports(String url) {
        return preferredCCParser.getImports(url);
    }
    
    public static <R,D> R interpretOrCompileCode(final Expression<Object> expression,
                                                 final String url, final int line,
                                                 final ErrorAwareTreePathScanner<Boolean,D> canInterpret,
                                                 final ErrorAwareTreePathScanner<R,D> interpreter,
                                                 final D context, boolean staticContext,
                                                 final Function<Pair<String, byte[]>, Boolean> compiledClassHandler,
                                                 final SourcePathProvider sp) throws InvalidExpressionException {
        return preferredCCParser.interpretOrCompileCode(expression, url, line,
                                                        canInterpret,
                                                        interpreter,
                                                        context, staticContext,
                                                        compiledClassHandler, sp);
    }


    private abstract static class ScanRunnable <E extends Throwable> implements Runnable {
        
        private Future<Void>[] resultPtr;
        private E[] excPtr;
        private Class<E> exceptionType;

        public ScanRunnable(Class<E> exceptionType) {
            this.exceptionType = exceptionType;
        }

        private void setParam(Future<Void>[] resultPtr, E[] excPtr) {
            this.resultPtr = resultPtr;
            this.excPtr = excPtr;
        }

        @Override
        public final void run() {
            run(resultPtr, excPtr);
        }

        public abstract void run(Future<Void>[] resultPtr, E[] excPtr);

    }

    private static Future<Void> runWhenScanFinishedReallyLazy(final FileObject fo,
                                                              final Task<CompilationController> task,
                                                              final boolean shared) throws IOException {
        return scanReallyLazy(new ScanRunnable<IOException>(IOException.class) {
            @Override
            public void run(Future<Void>[] resultPtr, IOException[] excPtr) {
                JavaSource js = JavaSource.forFileObject(fo);
                if (js == null) {
                    return ;
                }
                try {
                    js.runUserActionTask(task, shared);
                } catch (IOException ex) {
                    synchronized (resultPtr) {
                        excPtr[0] = ex;
                    }
                }
            }
        });
    }

    private static Future<Void> parseWhenScanFinishedReallyLazy(final FileObject fo,
                                                                final UserTask userTask) throws ParseException {
        return scanReallyLazy(new ScanRunnable<ParseException> (ParseException.class) {
            @Override
            public void run(Future<Void>[] resultPtr, ParseException[] excPtr) {
                Collection<Source> sources = Collections.singleton(Source.create(fo));
                try {
                    ParserManager.parse(sources, userTask);
                } catch (ParseException ex) {
                    synchronized (resultPtr) {
                        excPtr[0] = ex;
                    }
                }
            }
        });
    }

    private static <E extends Throwable> Future<Void> scanReallyLazy(ScanRunnable<E> run) throws E {
        final Future<Void>[] resultPtr = new Future[] { null };
        final E[] excPtr = (E[]) java.lang.reflect.Array.newInstance(run.exceptionType, 1);//new E[] { null };
        run.setParam(resultPtr, excPtr);
        final RequestProcessor.Task scanning = scanningProcessor.post(run);
        try {
            scanning.waitFinished(200);
        } catch (InterruptedException ex) {
        }
        synchronized (resultPtr) {
            if (excPtr[0] != null) {
                throw excPtr[0];
            }
            if (resultPtr[0] != null) {
                return resultPtr[0];
            }
        }
        return new Future<Void>() {
            boolean cancelled = false;
            
            private Future<Void> getDelegate() {
                synchronized (resultPtr) {
                    return resultPtr[0];
                }
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return cancelled = scanning.cancel();
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return scanning.isFinished();
            }

            @Override
            public Void get() throws InterruptedException, ExecutionException {
                scanning.waitFinished();
                if (excPtr[0] != null) {
                    throw new ExecutionException(excPtr[0]);
                }
                return null;
            }

            @Override
            public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                long mstimeout = unit.toMillis(timeout);
                if (mstimeout == 0) {
                    if (!scanning.isFinished()) {
                        throw new TimeoutException("Task timeout");
                    }
                } else {
                    long s1 = System.nanoTime();
                    boolean finished = scanning.waitFinished(mstimeout);
                    if (!finished) {
                        throw new TimeoutException("Task timeout");
                    }
                    long s2 = System.nanoTime();
                    timeout -= unit.convert(s2 - s1, TimeUnit.NANOSECONDS);
                    if (timeout < 0) {
                        timeout = 1;
                    }
                }
                if (excPtr[0] != null) {
                    throw new ExecutionException(excPtr[0]);
                }
                return null;
            }
        };
    }

    private static final class DoneFuture<T> implements Future<T> {

        private final T result;

        public DoneFuture(T result) {
            this.result = result;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) { return false; }
        @Override
        public boolean isCancelled() { return false; }
        @Override
        public boolean isDone() { return true; }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return result;
        }
        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return result;
        }
    }

}
