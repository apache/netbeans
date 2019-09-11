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

package org.netbeans.modules.groovy.editor.api.parser;

import groovyjarjarasm.asm.Opcodes;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.ResolveVisitor;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTRoot;
import org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
// this requires also a Java Indexer to be enabled for groovy mimetype
// see layer.xml JavaIndexer.shadow
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider.class)
public class GroovyVirtualSourceProvider implements VirtualSourceProvider {

    @Override
    public Set<String> getSupportedExtensions() {
        return Collections.singleton("groovy"); // NOI18N

    }

    @Override
    public void translate(Iterable<File> files, File sourceRoot, Result result) {
        JavaStubGenerator generator = new JavaStubGenerator();
        FileObject rootFO = FileUtil.toFileObject(sourceRoot);
        Iterator<File> it = files.iterator();
        while (it.hasNext()) {
            File file = FileUtil.normalizeFile(it.next());
            List<ClassNode> classNodes = getClassNodes(file);
            if (classNodes.isEmpty()) {
                // source is probably broken and there is no AST
                // let's generate empty Java stub with simple name equal to file name
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    String pkg = FileUtil.getRelativePath(rootFO, fo.getParent());
                    if (pkg != null) {
                        pkg = pkg.replace('/', '.');
                        StringBuilder sb = new StringBuilder();
                        if (!pkg.equals("")) { // NOI18N
                            sb.append("package ").append(pkg).append(";"); // NOI18N
                        }
                        String name = fo.getName();
                        sb.append("public class ").append(name).append("{}"); // NOI18N
                        result.add(file, pkg, name, sb.toString());
                    }
                }
            } else {
                for (ClassNode classNode : classNodes) {
                    try {
                        CharSequence javaStub = generator.generateClass(classNode);
                        String pkgName = classNode.getPackageName();
                        if (pkgName == null) {
                            pkgName = ""; // NOI18N
                        }
                        result.add(file, pkgName, classNode.getNameWithoutPackage(), javaStub);
                    } catch (FileNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    static List<ClassNode> getClassNodes(File file) {
        final List<ClassNode> resultList = new ArrayList<ClassNode>();
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            Source source = Source.create(fo);
            // Check is here brecause of issue #213967
            if (GroovyLanguage.GROOVY_MIME_TYPE.equals(source.getMimeType())) {
                try {
                    // FIXME can we move this out of task (?)
                    ParserManager.parse(Collections.singleton(source), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            Parser.Result parseResult = resultIterator.getParserResult();
                            if (parseResult != null) {
                                GroovyParserResult result = ASTUtils.getParseResult(parseResult);

                                ASTRoot astRootElement = result.getRootElement();
                                if (astRootElement != null) {
                                    ModuleNode moduleNode = astRootElement.getModuleNode();
                                    if (moduleNode != null) {
                                        resultList.addAll(moduleNode.getClasses());
                                    }
                                }
                            }
                        }
                    });
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        return resultList;
    }

    @SuppressWarnings("unchecked")
    static final class JavaStubGenerator {

        private boolean java5 = false;
        private boolean requireSuperResolved = false;
        private List<String> toCompile = new ArrayList<String>();

        private JavaStubGenerator(final boolean requireSuperResolved, final boolean java5) {
            this.requireSuperResolved = requireSuperResolved;
            this.java5 = java5;
        }

        public JavaStubGenerator() {
            this(false, true);
        }

        public CharSequence generateClass(ClassNode classNode) throws FileNotFoundException {
            // Only attempt to render our self if our super-class is resolved, else wait for it
            if (requireSuperResolved && !classNode.getSuperClass().isResolved()) {
                return null;
            }

            String fileName = classNode.getName().replace('.', '/');
            toCompile.add(fileName);

            StringWriter sw = new StringWriter();
            PrintWriter out = new PrintWriter(sw);

            try {
                String packageName = classNode.getPackageName();
                if (packageName != null) {
                    out.println("package " + packageName + ";\n");
                }

                genImports(classNode, out);

                boolean isInterface = classNode.isInterface();
                boolean isEnum = (classNode.getModifiers() & Opcodes.ACC_ENUM) != 0;
                printModifiers(out, classNode.getModifiers() & ~(isInterface ? Opcodes.ACC_ABSTRACT : 0));

                if (isInterface) {
                    out.print("interface ");
                } else if (isEnum) {
                    out.print("enum ");
                } else {
                    out.print("class ");
                }
                out.println(classNode.getNameWithoutPackage());
                writeGenericsBounds(out, classNode, true);

                ClassNode superClass = classNode.getUnresolvedSuperClass(false);

                if (!isInterface && !isEnum) {
                    out.print("  extends ");
                    printType(superClass, out);
                }

                ClassNode[] interfaces = classNode.getInterfaces();
                if (interfaces != null && interfaces.length > 0) {
                    if (isInterface) {
                        out.println("  extends");
                    } else {
                        out.println("  implements");
                    }
                    for (int i = 0; i < interfaces.length - 1; ++i) {
                        out.print("    ");
                        printType(interfaces[i], out);
                        out.print(",");
                    }
                    out.print("    ");
                    printType(interfaces[interfaces.length - 1], out);
                }
                out.println(" {");

                genFields(classNode, out, isEnum);
                genMethods(classNode, out, isEnum);
                genProps(classNode, out);

                out.println("}");
            } finally {
                try {
                    out.close();
                } catch (Exception e) {
                    // ignore
                }
                try {
                    sw.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            return sw.toString();
        }

        private void genMethods(ClassNode classNode, PrintWriter out, boolean isEnum) {
            if (!isEnum) {
                getConstructors(classNode, out);
            }
            List<MethodNode> methods = classNode.getMethods();
            if (methods != null) {
                for (MethodNode methodNode : methods) {
                    if (isEnum && methodNode.isSynthetic()) {
                        // skip values() method and valueOf(String)
                        String name = methodNode.getName();
                        Parameter[] params = methodNode.getParameters();
                        if (name.equals("values") && params.length == 0) {
                            continue;
                        }
                        if (name.equals("valueOf") &&
                                params.length == 1 &&
                                params[0].getType().equals(ClassHelper.STRING_TYPE)) {
                            continue;
                        }
                    }
                    genMethod(classNode, methodNode, out);
                }
            }

            // <netbeans>
            List<PropertyNode> properties = classNode.getProperties();
            for (PropertyNode propertyNode : properties) {
                if (!propertyNode.isSynthetic()) {
                    String name = propertyNode.getName();
                    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                    MethodNode getter = classNode.getGetterMethod("get" + name); // NOI18N
                    if (getter != null) {
                        genMethod(classNode, getter, out, false);
                    }
                    MethodNode setter = classNode.getSetterMethod("set" + name); // NOI18N
                    if (setter != null) {
                        genMethod(classNode, setter, out, false);
                    }
                    MethodNode isMethod = classNode.getDeclaredMethod("is" + name, new Parameter[0]); // NOI18N
                    if (isMethod != null) {
                        genMethod(classNode, isMethod, out, false);
                    }
                }
            }
            // </netbeans>
        }

        private void getConstructors(ClassNode classNode, PrintWriter out) {
            List<ConstructorNode> constrs = classNode.getDeclaredConstructors();
            if (constrs != null) {
                for (ConstructorNode constrNode : constrs) {
                    genConstructor(classNode, constrNode, out);
                }
            }
        }

        private void genFields(ClassNode classNode, PrintWriter out, boolean isEnum) {
            List<FieldNode> fields = classNode.getFields();
            if (fields == null) {
                return;
            }
            ArrayList<FieldNode> enumFields   = new ArrayList<FieldNode>(fields.size());
            ArrayList<FieldNode> normalFields = new ArrayList<FieldNode>(fields.size());
            
            for (FieldNode fieldNode : fields) {
                boolean isEnumField = (fieldNode.getModifiers() & Opcodes.ACC_ENUM) != 0;
                boolean isSynthetic = (fieldNode.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0;
                if (isEnumField) {
                    enumFields.add(fieldNode);
                } else if (!isSynthetic) {
                    normalFields.add(fieldNode);
                }
            }
            genEnumFields(enumFields, out);
            for (FieldNode fieldNode : normalFields) {
                genField(fieldNode, out);
            }
        }

        private void genProps(ClassNode classNode, PrintWriter out) {
            List<PropertyNode> props = classNode.getProperties();
            if (props != null) {
                for (PropertyNode propNode : props) {
                    genProp(propNode, out);
                }
            }
        }

        private void genProp(PropertyNode propNode, PrintWriter out) {
            String name = propNode.getName().substring(0, 1).toUpperCase() + propNode.getName().substring(1);

            String getterName = "get" + name;

            boolean skipGetter = false;
            List<MethodNode> getterCandidates = propNode.getField().getOwner().getMethods(getterName);
            if (getterCandidates != null) {
                for (MethodNode method : getterCandidates) {
                    if (method.getParameters().length == 0) {
                        skipGetter = true;
                    }
                }
            }
            if (!skipGetter) {
                printModifiers(out, propNode.getModifiers());

                printType(propNode.getType(), out);
                out.print(" ");
                out.print(getterName);
                out.print("() { ");

                printReturn(out, propNode.getType());

                out.println(" }");
            }

            String setterName = "set" + name;

            boolean skipSetter = false;
            List<MethodNode> setterCandidates = propNode.getField().getOwner().getMethods(setterName);
            if (setterCandidates != null) {
                for (MethodNode method : setterCandidates) {
                    if (method.getParameters().length == 1) {
                        skipSetter = true;
                    }
                }
            }
            if (!skipSetter) {
                printModifiers(out, propNode.getModifiers());
                out.print("void ");
                out.print(setterName);
                out.print("(");
                printType(propNode.getType(), out);
                out.println(" value) {}");
            }
        }

        private void genEnumFields(List fields, PrintWriter out) {
            if (fields.isEmpty()) {
                return;
            }
            boolean first = true;
            
            for (Iterator iterator = fields.iterator(); iterator.hasNext();) {
                FieldNode fieldNode = (FieldNode) iterator.next();
                if (!first) {
                    out.print(", ");
                } else {
                    first = false;
                }
                out.print(fieldNode.getName());
            }
            out.println(";");
        }

        private void genField(FieldNode fieldNode, PrintWriter out) {
            // <netbeans>
            if (fieldNode.isSynthetic() || "metaClass".equals(fieldNode.getName())) { // NOI18N
                return;
            }
            // </netbeans>
            if ((fieldNode.getModifiers() & Opcodes.ACC_PRIVATE) != 0) {
                return;
            }
            printModifiers(out, fieldNode.getModifiers());

            printType(fieldNode.getType(), out);

            out.print(" ");
            out.print(fieldNode.getName());
            out.println(";");
        }

        private ConstructorCallExpression getConstructorCallExpression(
                ConstructorNode constructorNode) {
            Statement code = constructorNode.getCode();
            if (!(code instanceof BlockStatement)) {
                return null;
            }
            BlockStatement block = (BlockStatement) code;
            List<Statement> stats = block.getStatements();
            if (stats == null || stats.isEmpty()) {
                return null;
            }
            Statement stat = (Statement) stats.get(0);
            if (!(stat instanceof ExpressionStatement)) {
                return null;
            }
            Expression expr = ((ExpressionStatement) stat).getExpression();
            if (!(expr instanceof ConstructorCallExpression)) {
                return null;
            }
            return (ConstructorCallExpression) expr;
        }

        private void genConstructor(ClassNode clazz, ConstructorNode constructorNode, PrintWriter out) {
            // <netbeans>
            if (constructorNode.isSynthetic()) {
                return;
            }
            // </netbeans>
            // printModifiers(out, constructorNode.getModifiers());

            out.print("public "); // temporary hack

            out.print(clazz.getNameWithoutPackage());

            printParams(constructorNode, out);

            ConstructorCallExpression constrCall = getConstructorCallExpression(constructorNode);
            if (constrCall == null || !constrCall.isSpecialCall()) {
                out.println(" {}");
            } else {
                out.println(" {");

                genSpecialConstructorArgs(out, constructorNode, constrCall);

                out.println("}");
            }
        }

        private Parameter[] selectAccessibleConstructorFromSuper(ConstructorNode node) {
            ClassNode type = node.getDeclaringClass();
            ClassNode superType = type.getSuperClass();

            boolean hadPrivateConstructor = false;
            for (ConstructorNode c : superType.getDeclaredConstructors()) {
                // Only look at things we can actually call
                if (c.isPublic() || c.isProtected()) {
                    return c.getParameters();
                }
            }

            // fall back for parameterless constructor
            if (superType.isPrimaryClassNode()) {
                return Parameter.EMPTY_ARRAY;
            }

            return null;
        }

        private void genSpecialConstructorArgs(PrintWriter out, ConstructorNode node, ConstructorCallExpression constrCall) {
            // Select a constructor from our class, or super-class which is legal to call,
            // then write out an invoke w/nulls using casts to avoid abigous crapo

            Parameter[] params = selectAccessibleConstructorFromSuper(node);
            if (params != null) {
                out.print("super (");

                for (int i = 0; i < params.length; i++) {
                    printDefaultValue(out, params[i].getType());
                    if (i + 1 < params.length) {
                        out.print(", ");
                    }
                }

                out.println(");");
                return;
            }

            // Otherwise try the older method based on the constructor's call expression
            Expression arguments = constrCall.getArguments();

            if (constrCall.isSuperCall()) {
                out.print("super(");
            } else {
                out.print("this(");
            }

            // Else try to render some arguments
            if (arguments instanceof ArgumentListExpression) {
                ArgumentListExpression argumentListExpression = (ArgumentListExpression) arguments;
                List<Expression> args = argumentListExpression.getExpressions();

                for (Expression arg : args) {
                    if (arg instanceof ConstantExpression) {
                        ConstantExpression expression = (ConstantExpression) arg;
                        Object o = expression.getValue();

                        if (o instanceof String) {
                            out.print("(String)null");
                        } else {
                            out.print(expression.getText());
                        }
                    } else {
                        printDefaultValue(out, arg.getType());
                    }

                    if (arg != args.get(args.size() - 1)) {
                        out.print(", ");
                    }
                }
            }

            out.println(");");
        }

        private void genMethod(ClassNode clazz, MethodNode methodNode, PrintWriter out) {
        // <netbeans>
            genMethod(clazz, methodNode, out, true);
        }

        private void genMethod(ClassNode clazz, MethodNode methodNode, PrintWriter out, boolean ignoreSynthetic) {
            String name = methodNode.getName();
            if ((ignoreSynthetic && methodNode.isSynthetic()) || name.startsWith("super$")) { // NOI18N
                return;
            }
        // </netbeans>
            if (methodNode.getName().equals("<clinit>")) {
                return;
            }
            if (!clazz.isInterface()) {
                printModifiers(out, methodNode.getModifiers());
            }
            printType(methodNode.getReturnType(), out);
            out.print(" ");
            out.print(methodNode.getName());

            printParams(methodNode, out);

            ClassNode[] exceptions = methodNode.getExceptions();
            if (exceptions != null && exceptions.length > 0) {
                out.print(" throws ");
                for (int i = 0; i < exceptions.length; i++) {
                    if (i > 0) {
                        out.print(", ");
                    }
                    printType(exceptions[i], out);
                }
            }

            if ((methodNode.getModifiers() & Opcodes.ACC_ABSTRACT) != 0) {
                out.println(";");
            } else {
                out.print(" { ");
                ClassNode retType = methodNode.getReturnType();
                printReturn(out, retType);
                out.println("}");
            }
        }

        private void printReturn(PrintWriter out, ClassNode retType) {
            String retName = retType.getName();
            if (!retName.equals("void")) {
                out.print("return ");

                printDefaultValue(out, retType);

                out.print(";");
            }
        }

        private void printDefaultValue(PrintWriter out, ClassNode type) {
            if (type.redirect() != ClassHelper.OBJECT_TYPE) {
                out.print("(");
                printType(type, out);
                out.print(")");
            }

            if (ClassHelper.isPrimitiveType(type)) {
                if (type == ClassHelper.boolean_TYPE) {
                    out.print("false");
                } else {
                    out.print("0");
                }
            } else {
                out.print("null");
            }
        }

        private void printType(ClassNode type, PrintWriter out) {
            if (type.isArray()) {
                printType(type.getComponentType(), out);
                out.print("[]");
            } else {
                writeGenericsBounds(out, type, false);
            }
        }

        private void printTypeName(ClassNode type, PrintWriter out) {
            if (ClassHelper.isPrimitiveType(type)) {
                if (type == ClassHelper.boolean_TYPE) {
                    out.print("boolean");
                } else if (type == ClassHelper.char_TYPE) {
                    out.print("char");
                } else if (type == ClassHelper.int_TYPE) {
                    out.print("int");
                } else if (type == ClassHelper.short_TYPE) {
                    out.print("short");
                } else if (type == ClassHelper.long_TYPE) {
                    out.print("long");
                } else if (type == ClassHelper.float_TYPE) {
                    out.print("float");
                } else if (type == ClassHelper.double_TYPE) {
                    out.print("double");
                } else if (type == ClassHelper.byte_TYPE) {
                    out.print("byte");
                } else {
                    out.print("void");
                }
            } else {
                out.print(type.redirect().getName().replace('$', '.'));
            }
        }

        private void writeGenericsBounds(PrintWriter out, ClassNode type, boolean skipName) {
            if (!skipName) {
                printTypeName(type, out);
            }
            if (java5 && !type.isGenericsPlaceHolder()) {
                writeGenericsBounds(out, type.getGenericsTypes());
            }
        }

        private void writeGenericsBounds(PrintWriter out, GenericsType[] genericsTypes) {
            if (genericsTypes == null || genericsTypes.length == 0) {
                return;
            }
            out.print('<');
            for (int i = 0; i < genericsTypes.length; i++) {
                if (i != 0) {
                    out.print(", ");
                }
                writeGenericsBounds(out, genericsTypes[i]);
            }
            out.print('>');
        }

        private void writeGenericsBounds(PrintWriter out, GenericsType genericsType) {
            if (genericsType.isPlaceholder()) {
                out.print(genericsType.getName());
            } else {
                printTypeName(genericsType.getType(), out);
                ClassNode[] upperBounds = genericsType.getUpperBounds();
                ClassNode lowerBound = genericsType.getLowerBound();
                if (upperBounds != null) {
                    out.print(" extends ");
                    for (int i = 0; i < upperBounds.length; i++) {
                        printType(upperBounds[i], out);
                        if (i + 1 < upperBounds.length) {
                            out.print(" & ");
                        }
                    }
                } else if (lowerBound != null) {
                    out.print(" super ");
                    printType(lowerBound, out);
                }
            }
        }

        private void printParams(MethodNode methodNode, PrintWriter out) {
            out.print("(");
            Parameter[] parameters = methodNode.getParameters();

            if (parameters != null && parameters.length != 0) {
                for (int i = 0; i != parameters.length; ++i) {
                    printType(parameters[i].getType(), out);

                    out.print(" ");
                    out.print(parameters[i].getName());

                    if (i + 1 < parameters.length) {
                        out.print(", ");
                    }
                }
            }

            out.print(")");
        }

        private void printModifiers(PrintWriter out, int modifiers) {
            if ((modifiers & Opcodes.ACC_PUBLIC) != 0) {
                out.print("public ");
            }
            if ((modifiers & Opcodes.ACC_PROTECTED) != 0) {
                out.print("protected ");
            }
            if ((modifiers & Opcodes.ACC_PRIVATE) != 0) {
                out.print("private ");
            }
            if ((modifiers & Opcodes.ACC_STATIC) != 0) {
                out.print("static ");
            }
            if ((modifiers & Opcodes.ACC_SYNCHRONIZED) != 0) {
                out.print("synchronized ");
            }
            if ((modifiers & Opcodes.ACC_ABSTRACT) != 0) {
                out.print("abstract ");
            }
        }

        private void genImports(ClassNode classNode, PrintWriter out) {
            Set<String> imports = new HashSet<>();

            //
            // HACK: Add the default imports... since things like Closure and GroovyObject seem to parse out w/o fully qualified classnames.
            //
            imports.addAll(Arrays.asList(ResolveVisitor.DEFAULT_IMPORTS));

            // FIXME by using star imports in generated class
            // we could cause namespace collision
            ModuleNode moduleNode = classNode.getModule();
            for (ImportNode importNode : moduleNode.getImports()) {
                if (importNode.isStar()) {
                    imports.add(importNode.getPackageName() + ".");
                }
            }

            for (ImportNode imp : moduleNode.getImports()) {
                String name = imp.getType().getName();
                int lastDot = name.lastIndexOf('.');
                if (lastDot != -1) {
                    imports.add(name.substring(0, lastDot + 1));
                }
            }

            for (String imp : imports) {
                out.print("import ");
                out.print(imp);
                out.println("*;");
            }
            out.println();
        }

    }

    @Override
    public boolean index() {
        return false;
    }

}
