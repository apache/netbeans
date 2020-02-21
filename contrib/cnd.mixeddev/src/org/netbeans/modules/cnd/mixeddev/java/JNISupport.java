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

package org.netbeans.modules.cnd.mixeddev.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.*;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils.Converter;
import static org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport.createMethodInfo;
import static org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport.renderQualifiedName;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaClassInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaFieldInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaParameterInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaTypeInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.jni.JNIClass;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 *
 */
public final class JNISupport {
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.mixeddev.java"); //NOI18N
    
    /**
     * Checks if class in java file at the given offset has JNI methods
     * @param doc
     * @param caret
     * @return true if it is a class and it has JNI methods, false otherwise
     */
    public static boolean isJNIClass(Document doc, int caret) {
        return isJNIClass(doc, caret, false);
    }
    
    /**
     * Checks if class in java file at the given offset has JNI methods
     * @param doc
     * @param caret
     * @param immediately - whether to execute that operation immediately or it can wait a bit
     * @return true if it is a class and it has JNI methods, false otherwise
     */
    public static boolean isJNIClass(Document doc, int caret, boolean immediately) {
        Boolean res = JavaContextSupport.resolveContext(doc, new IsJNIClassTask(caret), immediately);
        return res != null ? res : false;
    }
    
    /**
     * 
     * @param doc
     * @param caret
     * @return JNI class or null if there caret is not denoting JNI class
     */
    public static JNIClass getJNIClass(Document doc, int caret) {
        return getJNIClass(doc, caret, false);
    }
    
    /**
     * 
     * @param doc
     * @param caret
     * @param immediately - whether to execute that operation immediately or it can wait a bit
     * @return JNI class or null if there caret is not denoting JNI class
     */
    public static JNIClass getJNIClass(Document doc, int caret, boolean immediately) {
        return JavaContextSupport.resolveContext(doc, new GetJNIClassTask(caret), immediately);
    }

    /**
     * Finds JNI Classes in java file.
     * @param fObj
     * @param immediately - whether to execute that operation immediately or it can wait a bit
     * @return qualified names of all classes with JNI methods in fObj
     */
    public static List<String> getJNIClassNames(FileObject fObj, boolean immediately) {
        return JavaContextSupport.resolveContext(fObj, new GetJavaJNIClassesNamesTask(), immediately);
    }    
    
    public static List<String> getJNIClassNames(FileObject fObj) {
        return getJNIClassNames(fObj, false);
    }    
    
    public static List<String> getJNIClassNames(Document doc, boolean immediately) {
        return JavaContextSupport.resolveContext(doc, new GetJavaJNIClassesNamesTask(), immediately);
    }    
    
    public static List<String> getJNIClassNames(Document doc) {
        return getJNIClassNames(doc, false);
    }    
    
    /**
     * Finds JNI method in java file at the given offset.
     * @param fObj
     * @param offset
     * @param immediately - whether to execute that operation immediately or it can wait a bit
     * @return method if there is one or null
     */
    public static JavaMethodInfo getJNIMethod(FileObject fObj, int offset, boolean immediately) {
        return JavaContextSupport.resolveContext(fObj, new ResolveJNIMethodTask(offset), immediately);
    }    
    
    public static JavaMethodInfo getJNIMethod(FileObject fObj, int offset) {
        return getJNIMethod(fObj, offset, false);
    }    
    
    public static JavaMethodInfo getJNIMethod(Document doc, int offset, boolean immediately) {
        return JavaContextSupport.resolveContext(doc, new ResolveJNIMethodTask(offset), immediately);
    }
    
    public static JavaMethodInfo getJNIMethod(Document doc, int offset) {
        return getJNIMethod(doc, offset, false);
    }
    
    /**
     * 
     * @param methodInfo
     * @return possible C++ signatures for a JNI method
     */
    public static String[] getCppMethodSignatures(JavaMethodInfo methodInfo) {
        String exception = getExceptionalMethodCppSignature(renderQualifiedName(methodInfo.getQualifiedName()));
        if (exception != null) {
            return new String[]{exception};
        } else {
            if (methodInfo.isOverloaded()) {
                // Ambiguity! Search only with long signature
                return new String[]{getCppSignature(methodInfo, true)};
            }
            // Method is not overloaded. Search with the short signature first, then with the long one
            return new String[]{getCppSignature(methodInfo, false), getCppSignature(methodInfo, true)};
        }
    }
    
    /**
     * Returns cpp signature using only Java Qualified Name.
     * @param qualifiedName
     * @return cpp signature
     * 
     * @deprecated because it is impossible to find out completely correct signature
     *              using only qualified name. So it is a hack which must be removed 
     *              as soon as possible
     */
    @Deprecated
    public static String getCppMethodSignature(String qualifiedName) {
        String exception = getExceptionalMethodCppSignature(qualifiedName);
        return exception != null ? exception : JNI_QN_PREFIX.concat(escape(qualifiedName));
    }
    
    /**
     * 
     * @param javaType
     * @return type signature
     */
    public static String getJNISignature(JavaTypeInfo javaType) {
        if (javaType != null) {
            if (javaType.getArrayDepth() > 0) {
                return "[" + JNISupport.getJNISignature( // NOI18N
                    new JavaTypeInfo(javaType.getName(), javaType.getQualifiedName(), javaType.getArrayDepth() - 1)
                );
            } else {
                String typeName = javaType.getText().toString();
                if (javaToSignatures.containsKey(typeName)) {
                    return javaToSignatures.get(typeName);
                }
                return "L"  // NOI18N
                    + renderQualifiedName(javaType.getQualifiedName())
                    + ";"; // NOI18N
            }
        }
        // consider no type as void type
        return javaToSignatures.get("void"); // NOI18N
    }
    
    /**
     * 
     * @param methodInfo
     * @return type signature
     */
    public static String getJNISignature(JavaMethodInfo methodInfo) {
        StringBuilder signature = new StringBuilder();
        signature.append("("); // NOI18N
        for (JavaParameterInfo param : methodInfo.getParameters()) {
            signature.append(JNISupport.getJNISignature(param.getType()));
        }
        signature.append(")"); // NOI18N
        signature.append(JNISupport.getJNISignature(methodInfo.getReturnType()));
        return signature.toString();
    }
    
    /**
     * 
     * @param javaClass
     * @return class signature (which is its qualified name)
     */
    public static String getJNISignature(JavaClassInfo javaClass) {
        return javaClass.getQualifiedName().toString();
    }
    
    /**
     * 
     * @param javaField
     * @return field signature (which is its type signature)
     */
    public static String getJNISignature(JavaFieldInfo javaField) {
        return getJNISignature(javaField.getType());
    }
    
    /**
     * 
     * @param javahFObj
     * @param sourceRoot
     * @param javaClass
     * @param destination
     * @param sourceCP
     * @param compileCP
     * @return file object of freshly generated header file or null
     */
    public static FileObject generateJNIHeader(FileObject javahFObj, FileObject sourceRoot, FileObject javaClass, String destination, ClassPath sourceCP, ClassPath compileCP) {
        File javah = FileUtil.toFile(javahFObj); //NOI18N
        String className = FileUtil.getRelativePath(sourceRoot, javaClass);
        if (className.endsWith(".java")) { // NOI18N
            className = className.substring(0, className.length() - 5).replace('/', '.').replace('\\', '.');
        }

        File workingDir = new File(FileUtil.toFile(sourceRoot.getParent()), "build/classes"); // NOI18N
        List<String> args = new ArrayList<String>();
        args.add("-o"); // NOI18N
        args.add(destination);
        String argCP = "";
        boolean needed = false;
        if (sourceCP != null) {
            String source = sourceCP.toString();
            if (!source.isEmpty()) {
                needed = true;
                argCP = argCP.concat(source + File.pathSeparator);
            }
        }
        if (compileCP != null) {
            String compile = compileCP.toString();
            if (!compile.isEmpty()) {
                needed = true;
                argCP = argCP.concat(compile);
            }
        }
        if (needed) {
            args.add("-classpath"); // NOI18N
            args.add(argCP);
        }
        args.add(className);

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(ExecutionEnvironmentFactory.getLocal());
        npb.setWorkingDirectory(workingDir.getAbsolutePath());
        npb.setExecutable(javah.getAbsolutePath());
        npb.setArguments(args.toArray(new String[args.size()]));
        ProcessUtils.ExitStatus javahStatus = ProcessUtils.execute(npb);

        if (!javahStatus.isOK()) {
            LOG.log(Level.WARNING, "javah failed {0}; args={1}", new Object[]{javahStatus, args});
            return null;
        }
        
        File destFile = new File(destination);
        
        return destFile.isAbsolute() ?
            FileUtil.toFileObject(destFile) 
            : FileUtil.toFileObject(new File(workingDir, destination));
    }
    
//<editor-fold defaultstate="collapsed" desc="Implementation">    
    private static final String JNI_QN_PREFIX = "Java_"; // NOI18N
    
    private static final String JNI_PARAMS_SIGNATURE_PREFIX = "__"; // NOI18N
    
    private static final String JNI_JNIENV = "JNIEnv";  // NOI18N
    
    private static final String JNI_JOBJECT = "jobject";    // NOI18N
    
    private static final String JNI_JCLASS = "jclass";    // NOI18N
    
    private static final List<String> JNI_REGULAR_IMPLICIT_PARAMS = Arrays.asList(JNI_JNIENV + POINTER, JNI_JOBJECT);
    
    private static final List<String> JNI_STATIC_IMPLICIT_PARAMS = Arrays.asList(JNI_JNIENV + POINTER, JNI_JCLASS);
    
    // "."
    private static final String JNI_DOT = "_"; // NOI18N
    
    // "/"
    private static final String JNI_SLASH = "_"; // NOI18N 
    
    // "_"
    private static final String JNI_UNDERSCORE = "_1"; // NOI18N 
    
    // ";"
    private static final String JNI_SEMICOLON = "_2"; // NOI18N 
    
    // "["
    private static final String JNI_LBRACKET = "_3"; // NOI18N 
    
    private static final Map<String, String> javaToCppTypes = createMapping(
        Pair.of("boolean", "jboolean"), // NOI18N 
        Pair.of("byte", "jbyte"), // NOI18N
        Pair.of("char", "jchar"), // NOI18N
        Pair.of("short", "jshort"), // NOI18N
        Pair.of("int", "jint"), // NOI18N
        Pair.of("long", "jlong"), // NOI18N
        Pair.of("float", "jfloat"), // NOI18N
        Pair.of("double", "jdouble"), // NOI18N
        Pair.of("void", "void"), // NOI18N
        Pair.of("String", "jstring"), // NOI18N
        Pair.of("boolean[]", "jbooleanArray"), // NOI18N
        Pair.of("byte[]", "jbyteArray"), // NOI18N
        Pair.of("char[]", "jcharArray"), // NOI18N
        Pair.of("short[]", "jshortArray"), // NOI18N
        Pair.of("int[]", "jintArray"), // NOI18N
        Pair.of("long[]", "jlongArray"), // NOI18N
        Pair.of("float[]", "jfloatArray"), // NOI18N
        Pair.of("double[]", "jdoubleArray") // NOI18N
    );
    
    private static final Map<String, String> javaToSignatures = createMapping(
        Pair.of("boolean", "Z"), // NOI18N
        Pair.of("byte", "B"), // NOI18N
        Pair.of("char", "C"), // NOI18N
        Pair.of("short", "S"), // NOI18N
        Pair.of("int", "I"), // NOI18N
        Pair.of("long", "J"), // NOI18N
        Pair.of("float", "F"), // NOI18N
        Pair.of("double", "D"), // NOI18N
        Pair.of("void", "V") // NOI18N
    );
    
    private static final Map<String, String> javaExceptionalMethodSignatures = createMapping(
        Pair.of("java/lang/Object/hashCode", "JVM_IHashCode"), // NOI18N
        Pair.of("java/lang/Object/clone", "JVM_Clone"), // NOI18N
        Pair.of("java/lang/Object/notify", "JVM_MonitorNotify"), // NOI18N
        Pair.of("java/lang/Object/notifyAll", "JVM_MonitorNotifyAll"), // NOI18N
        Pair.of("java/lang/Object/wait", "JVM_MonitorWait") // NOI18N
    );
    
    private static String getExceptionalMethodCppSignature(CharSequence qualName) {
        return javaExceptionalMethodSignatures.get(qualName.toString());
    }
    
    private static String getCppSignature(JavaMethodInfo methodInfo, boolean full) {
        if (methodInfo == null) {
            return null;
        }
        StringBuilder signature = new StringBuilder();
        
        // Add method name
        signature.append(JNI_QN_PREFIX).append(escape(renderQualifiedName(methodInfo.getQualifiedName())));
        
        if (full) {
            signature.append(JNI_PARAMS_SIGNATURE_PREFIX);
            for (JavaParameterInfo param : methodInfo.getParameters()) {
                signature.append(escape(JNISupport.getJNISignature(param.getType())));
            }
        }
        
        // Add parameters
        List<String> cppTypes = new ArrayList<String>();
        cppTypes.addAll(methodInfo.isStatic() ? JNI_STATIC_IMPLICIT_PARAMS : JNI_REGULAR_IMPLICIT_PARAMS);
        cppTypes.addAll(transform(methodInfo.getParameters(), JavaParameterToCppTypeConverter.INSTANCE));        
        signature.append(LPAREN).append(stringize(cppTypes, COMMA)).append(RPAREN);
        
        return signature.toString();
    }
    
    private static String escape(CharSequence text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);
            if (needEscape(chr)) {
                sb.append(escape(chr));
            } else {
                sb.append(chr);
            }
        }
        return sb.toString();
    }
    
    private static boolean needEscape(char codepoint) {
        boolean validCodePoint = (codepoint >= '0' && codepoint <= '9')  // NOI18N
            || (codepoint >= 'a' && codepoint <= 'z')  // NOI18N
            || (codepoint >= 'A' && codepoint <= 'Z'); // NOI18N
        return !validCodePoint;
    }
    
    private static String escape(char chr) {
        assert needEscape(chr);
        switch (chr) {
            case '/': // NOI18N
                return JNI_SLASH;
            case '_': // NOI18N
                return JNI_UNDERSCORE;
            case ';': // NOI18N
                return JNI_SEMICOLON;
            case '[': // NOI18N
                return JNI_LBRACKET;
            case '.': // NOI18N
                return JNI_DOT;
            default:
                String hex = Integer.toHexString(chr);
                return "_0" + repeat("0", 4 - hex.length()) + hex; // NOI18N
        }
    }
    
    private static String getCppType(JavaTypeInfo javaType) {
        String typeName = javaType.getText().toString();
        if (javaToCppTypes.containsKey(typeName)) {
            return javaToCppTypes.get(typeName);
        }
        return javaType.getArrayDepth() > 0 ? "jobjectArray" : "jobject"; // NOI18N
    }
    
    private static boolean isJNIMethod(MethodTree mtd) {
        return mtd.getModifiers().getFlags().contains(Modifier.NATIVE);
    }
    
    private static boolean hasJniMethods(ClassTree clsTree) {
        for (Tree memberTree : clsTree.getMembers()) {
            if (memberTree.getKind() == Tree.Kind.METHOD) {
                if (isJNIMethod((MethodTree) memberTree)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static List<MethodTree> getJniMethods(ClassTree clsTree) {
        List<MethodTree> methods = new ArrayList<>();
        for (Tree memberTree : clsTree.getMembers()) {
            if (memberTree.getKind() == Tree.Kind.METHOD) {
                if (isJNIMethod((MethodTree) memberTree)) {
                    methods.add((MethodTree) memberTree);
                }
            }
        }
        return methods;
    }
    
    private final static class JavaParameterToCppTypeConverter implements Converter<JavaParameterInfo, String> {
        
        public static final JavaParameterToCppTypeConverter INSTANCE = new JavaParameterToCppTypeConverter();

        @Override
        public String convert(JavaParameterInfo from) {
            return JavaTypeToCppTypeConverter.INSTANCE.convert(from.getType());
        }
    }
    
    private final static class JavaTypeToCppTypeConverter implements Converter<JavaTypeInfo, String> {
        
        public static final JavaTypeToCppTypeConverter INSTANCE = new JavaTypeToCppTypeConverter();
        
        @Override
        public String convert(JavaTypeInfo from) {
            return getCppType(from);
        }
    }
    
    private final static class MethodTreeToJavaMethodInfoConverter implements Converter<MethodTree, JavaMethodInfo> {
        
        private final CompilationController controller;

        public MethodTreeToJavaMethodInfoConverter(CompilationController controller) {
            this.controller = controller;
        }

        @Override
        public JavaMethodInfo convert(MethodTree from) {
            return JavaContextSupport.createMethodInfo(
                controller, 
                controller.getTrees().getPath(controller.getCompilationUnit(), from)
            );
        }
    }
    
    private static final class ResolveJNIMethodTask extends AbstractResolveJavaContextTask<JavaMethodInfo> {
        
        public ResolveJNIMethodTask(int offset) {
            super(offset);
        }

        @Override
        protected void resolve(CompilationController controller, TreePath tp) {
            if (tp.getLeaf() != null && tp.getLeaf().getKind() == Tree.Kind.METHOD) {
                if (((MethodTree) tp.getLeaf()).getModifiers().getFlags().contains(Modifier.NATIVE)) {
                    result = validateMethodInfo(createMethodInfo(controller, tp));
                }
            }
        }
        
        private JavaMethodInfo validateMethodInfo(JavaMethodInfo mtdInfo) {
            for (JavaParameterInfo param : mtdInfo.getParameters()) {
                if (param == null || param.getName() == null) {
                    return null;
                }
            }
            if (mtdInfo.getReturnType() == null || mtdInfo.getReturnType().getName() == null) {
                return null;
            }
            return mtdInfo;
        }
    }
    
    private static final class IsJNIClassTask extends AbstractResolveJavaContextTask<Boolean> {
        
        public IsJNIClassTask(int offset) {
            super(offset);
        }

        @Override
        protected void resolve(CompilationController controller, TreePath tp) {
            if (tp.getLeaf() != null && tp.getLeaf().getKind() == Tree.Kind.CLASS) {
                result = hasJniMethods((ClassTree) tp.getLeaf());
            }
        }
    }
    
    private static final class GetJNIClassTask extends AbstractResolveJavaContextTask<JNIClass> {
        
        public GetJNIClassTask(int offset) {
            super(offset);
        }

        @Override
        protected void resolve(CompilationController controller, TreePath tp) {
            if (tp.getLeaf() != null && tp.getLeaf().getKind() == Tree.Kind.CLASS) {
                ClassTree clsTree = (ClassTree) tp.getLeaf();
                if (hasJniMethods(clsTree)) {
                    List<MethodTree> jniMethods = getJniMethods(clsTree);
                    result = new JNIClass(
                        JavaContextSupport.createClassInfo(controller, tp),
                        MixedDevUtils.transform(jniMethods, new MethodTreeToJavaMethodInfoConverter(controller))
                    );
                }
            }
        }
    }
    
    private static final class GetJavaJNIClassesNamesTask implements ResolveJavaContextTask<List<String>> {
        
        private final List<String> result = new ArrayList<String>();
        
        @Override
        public boolean hasResult() {
            return true;
        }
        
        @Override
        public List<String> getResult() {
            return Collections.unmodifiableList(result);
        }
        
        @Override
        public void cancel() {
            // Do nothing
        }
        
        @Override
        public void run(CompilationController controller) throws Exception {
            if (controller == null || controller.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                return;
            }
            final CompilationUnitTree compilationUnit = controller.getCompilationUnit();
            if (compilationUnit == null) {
                return;
            }
            List<? extends Tree> topLevelDecls = compilationUnit.getTypeDecls();
            if (topLevelDecls != null && !topLevelDecls.isEmpty()) {
                for (Tree topLevelDecl : topLevelDecls) {
                    if (topLevelDecl.getKind() == Tree.Kind.CLASS) {
                        if (hasJniMethods((ClassTree) topLevelDecl)) {
                            result.add(JavaContextSupport.renderQualifiedName(JavaContextSupport.getQualifiedName(controller, topLevelDecl)));
                        }
                    }
                }
            }
        }
    }
    
    private JNISupport() {
        throw new AssertionError("Not instantiable!"); // NOI18N
    }
//</editor-fold>
}
