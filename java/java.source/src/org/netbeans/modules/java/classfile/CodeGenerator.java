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

package org.netbeans.modules.java.classfile;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.DirectiveTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.classfile.Attribute;
import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.Code_attribute;
import com.sun.tools.classfile.ConstantPoolException;
import com.sun.tools.classfile.Instruction;
import com.sun.tools.classfile.Method;
import com.sun.tools.classfile.Opcode;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javap.ClassWriter;
import com.sun.tools.javap.CodeWriter;
import com.sun.tools.javap.ConstantWriter;
import com.sun.tools.javap.Context;
import com.sun.tools.javap.Messages;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractElementVisitor9;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import com.sun.tools.classfile.SourceFile_attribute;
import java.nio.charset.StandardCharsets;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;
import org.netbeans.modules.java.source.query.CommentSet.RelativePosition;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class CodeGenerator {

    private static final Logger LOG = Logger.getLogger(CodeGenerator.class.getName());
    private static final Set<ElementKind> UNUSABLE_KINDS = EnumSet.of(ElementKind.PACKAGE);
    private static final byte VERSION = 2;
    private static final String HASH_ATTRIBUTE_NAME = "origin-hash";    //NOI18N
    private static final String DISABLE_ERRORS = "disable-java-errors"; //NOI18N
    static final String CLASSFILE_ROOT = "classfile-root";              //NOI18N
    static final String CLASSFILE_BINNAME = "classfile-binaryName";     //NOI18N
    static final String CLASSFILE_SOURCEFILE = "classfile-sourcefile";    //NOI18N

    public static FileObject generateCode(final ClasspathInfo cpInfo, final ElementHandle<? extends Element> toOpenHandle) {
      return generateCode(cpInfo, toOpenHandle, null);
    }

    public static FileObject generateCode(final ClasspathInfo cpInfo, final ElementHandle<? extends Element> toOpenHandle, boolean[] trySourceAttr) {
        if (UNUSABLE_KINDS.contains(toOpenHandle.getKind())) {
          return null;
        }

        try {
            FileObject file = FileUtil.createMemoryFileSystem().getRoot().createData(toOpenHandle.getKind() == ElementKind.MODULE ? "module-info.java" : "test.java");  //NOI18N
            OutputStream out = file.getOutputStream();

            try {
                FileUtil.copy(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), out); //NOI18N
            } finally {
                out.close();
            }

            JavaSource js = JavaSource.create(cpInfo, file);
            final FileObject[] result = new FileObject[1];
            final boolean[] sourceGenerated = new boolean[1];
            final URL[] classfileRoot = new URL[1];
            final String[] binaryName = new String[1];

            ModificationResult r = js.runModificationTask(new Task<WorkingCopy>() {
                @Override
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(Phase.ELEMENTS_RESOLVED);

                    String binName;
                    FileObject resource;
                    FileObject root;
                    Element toOpen = toOpenHandle.resolve(wc);

                    if (toOpen != null && toOpen.getKind() == ElementKind.MODULE) {
                        binName = "module-info"; //NOI18N
                        JavaFileObject jfo = ((Symbol.ModuleSymbol)toOpen).module_info.classfile;
                        resource = jfo != null ? URLMapper.findFileObject(jfo.toUri().toURL()) : null;
                        if (resource == null) {
                            LOG.info("Cannot find resource for module: " + ((ModuleElement)toOpen).getQualifiedName()); //NOI18N
                            return;
                        }
                        root = resource.getParent();
                    } else {

                        final ClassPath cp = ClassPathSupport.createProxyClassPath(
                                cpInfo.getClassPath(PathKind.BOOT),
                                cpInfo.getClassPath(PathKind.COMPILE),
                                cpInfo.getClassPath(PathKind.SOURCE));
                        final TypeElement te = toOpen != null ? wc.getElementUtilities().outermostTypeElement(toOpen) : null;
                        if (trySourceAttr != null) {
                            String name = SourceUtils.findSourceFileName(toOpen);
                            if (name != null) {
                                FileObject found = SourceUtils.getFile(toOpenHandle, cpInfo, name);
                                if (found != null) {
                                    result[0] = found;
                                    trySourceAttr[0] = true;
                                    return;
                                }
                            }
                        }

                        if (te == null) {
                            LOG.info("Cannot resolve element: " + toOpenHandle.toString() + " on classpath: " + cp.toString()); //NOI18N
                            return;
                        }

                        binName = te.getQualifiedName().toString().replace('.', '/');  //NOI18N
                        final String resourceName = binName + ".class";  //NOI18N
                        resource = cp.findResource(resourceName);
                        if (resource == null) {
                            LOG.info("Cannot find resource: " + resourceName +" on classpath: " + cp.toString()); //NOI18N
                            return ;
                        }

                        root = cp.findOwnerRoot(resource);
                        if (root == null) {
                            LOG.info("Cannot find owner of: " + FileUtil.getFileDisplayName(resource) +" on classpath: " + cp.toString()); //NOI18N
                            return ;
                        }
                        toOpen = te;
                    }

                    classfileRoot[0] = root.toURL();
                    binaryName[0] = binName;
                    final File  sourceRoot = new File (JavaIndex.getIndex(root.toURL()),"gensrc");     //NOI18N
                    final FileObject sourceRootFO = FileUtil.createFolder(sourceRoot);
                    if (sourceRootFO == null) {
                        LOG.info("Cannot create folder: " + sourceRoot); //NOI18N
                        return ;
                    }

                    final String path = binName + ".java";   //NOI18N
                    final FileObject source = sourceRootFO.getFileObject(path);

                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update(VERSION);
                    byte[] hashBytes = md.digest(resource.asBytes());
                    StringBuilder hashBuilder = new StringBuilder();

                    for (byte b : hashBytes) {
                        hashBuilder.append(String.format("%02X", b));
                    }

                    String hash = hashBuilder.toString();

                    if (source != null) {
                        result[0] = source;

                        String existingHash = (String) source.getAttribute(HASH_ATTRIBUTE_NAME);

                        if (hash.equals(existingHash)) {
                            LOG.fine(FileUtil.getFileDisplayName(source) + " is up to date, reusing from cache.");  //NOI18N
                            return;
                        }
                    }
                    final String[] betterName = { null };
                    final CompilationUnitTree cut = generateCode(wc, toOpen, betterName);
                    wc.rewrite(wc.getCompilationUnit(), cut);
                    if (source == null) {
                        result[0] = FileUtil.createData(sourceRootFO, path);
                        LOG.fine(FileUtil.getFileDisplayName(result[0]) + " does not exist, creating.");  //NOI18N
                    } else {
                        LOG.fine(FileUtil.getFileDisplayName(source) + " is not up to date, regenerating.");  //NOI18N
                    }

                    result[0].setAttribute(HASH_ATTRIBUTE_NAME, hash);
                    if (betterName[0] != null) {
                        result[0].setAttribute(CLASSFILE_SOURCEFILE, betterName[0]);
                    }

                    sourceGenerated[0] = true;
                }
            });

            if (sourceGenerated[0]) {
                final File resultFile = FileUtil.toFile(result[0]);
                if (resultFile != null && !resultFile.canWrite()) {
                    resultFile.setWritable(true);
                }
                out = result[0].getOutputStream();
                try {
                    FileUtil.copy(new ByteArrayInputStream(r.getResultingSource(file).getBytes(StandardCharsets.UTF_8)), out);
                } finally {
                    out.close();
                }
                if (resultFile != null) {
                    resultFile.setReadOnly();
                    result[0].setAttribute(DISABLE_ERRORS, true);
                    result[0].setAttribute(CLASSFILE_ROOT, classfileRoot[0]);
                    result[0].setAttribute(CLASSFILE_BINNAME, binaryName[0]);
                }
            }

            return result[0];
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    static CompilationUnitTree generateCode(WorkingCopy wc, Element te, String[] name) {
        TreeMaker make = wc.getTreeMaker();
        final TreeBuilder b = new TreeBuilder(make, wc);
        Tree clazz = b.visit(te);
        CompilationUnitTree cut = make.CompilationUnit(
                te.getKind() == ElementKind.MODULE ? null : make.Identifier(((PackageElement) te.getEnclosingElement()).getQualifiedName()),
                Collections.<ImportTree>emptyList(),
                Collections.singletonList(clazz),
                wc.getCompilationUnit().getSourceFile());

        name[0] = b.sourceFileName;
        return cut;
    }

    private static final class TreeBuilder extends AbstractElementVisitor9<Tree, Void> {

        private final TreeMaker make;
        private final WorkingCopy wc;
        private ClassFile cf;
        private Map<String, Method> sig2Method;
        String sourceFileName;

        public TreeBuilder(TreeMaker make, WorkingCopy wc) {
            this.make = make;
            this.wc = wc;
        }

        @Override
        public Tree visitPackage(PackageElement e, Void p) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Tree visitModule(ModuleElement e, Void p) {
            ModifiersTree mods = make.Modifiers(e.getModifiers());
            ModuleTree.ModuleKind kind = wc.getElementUtilities().isOpen(e) ? ModuleTree.ModuleKind.OPEN : ModuleTree.ModuleKind.STRONG;
            List<DirectiveTree> directives = new LinkedList<>();
            for (ModuleElement.Directive directive : e.getDirectives()) {
                switch(directive.getKind()) {
                    case EXPORTS:
                        ModuleElement.ExportsDirective expDirective = (ModuleElement.ExportsDirective)directive;
                        directives.add(make.Exports(make.QualIdent(expDirective.getPackage()), constructModuleList(expDirective.getTargetModules())));
                        break;
                    case OPENS:
                        ModuleElement.OpensDirective opensDirective = (ModuleElement.OpensDirective)directive;
                        directives.add(make.Opens(make.QualIdent(opensDirective.getPackage()), constructModuleList(opensDirective.getTargetModules())));
                        break;
                    case PROVIDES:
                        ModuleElement.ProvidesDirective provDirective = (ModuleElement.ProvidesDirective)directive;
                        directives.add(make.Provides(make.QualIdent(provDirective.getService()), constructTypeList(provDirective.getImplementations())));
                        break;
                    case REQUIRES:
                        ModuleElement.RequiresDirective reqDirective = (ModuleElement.RequiresDirective)directive;
                        directives.add(make.Requires(reqDirective.isTransitive(), reqDirective.isStatic(), make.QualIdent(reqDirective.getDependency().getQualifiedName().toString())));
                        break;
                    case USES:
                        ModuleElement.UsesDirective usesDirective = (ModuleElement.UsesDirective)directive;
                        directives.add(make.Uses(make.QualIdent(usesDirective.getService())));
                        break;
                }
            }
            return addDeprecated(e, make.Module(mods, kind, make.QualIdent(e.getQualifiedName().toString()), directives));
        }

        @Override
        public Tree visitType(TypeElement e, Void p) {
            ClassFile oldCf = cf;
            Map<String, Method> oldMethods = sig2Method;

            cf = null;
            sig2Method = new HashMap<String, Method>();

            try {
                try {
                    JavaFileObject classfile = ((ClassSymbol) e).classfile;

                    if (classfile != null && classfile.getKind() == Kind.CLASS) {
                        InputStream in = classfile.openInputStream();

                        try {
                            cf = ClassFile.read(in);
                            Attribute sfaRaw = cf.getAttribute(Attribute.SourceFile);
                            if (sfaRaw instanceof SourceFile_attribute) {
                                SourceFile_attribute sfa = (SourceFile_attribute) sfaRaw;
                                sourceFileName = sfa.getSourceFile(cf.constant_pool);
                            }
                            for (Method m : cf.methods) {
                                sig2Method.put(cf.constant_pool.getUTF8Value(m.name_index) + ":" + cf.constant_pool.getUTF8Value(m.descriptor.index), m);
                            }
                        } finally {
                            in.close();
                        }
                    }
                } catch (ConstantPoolException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

                List<Tree> members = new LinkedList<Tree>();

                for (Element m : e.getEnclosedElements()) {
                    if (m.getKind() == ElementKind.RECORD_COMPONENT) {
                        continue; // TODO update to 'extend AbstractElementVisitor14'; visiting record components causes UnknownElementException
                    }
                    Tree member = visit(m);

                    if (member != null)
                        members.add(member);
                }

                ModifiersTree mods = computeMods(e);

                switch (e.getKind()) {
                    case CLASS:
                        return addDeprecated(e, make.Class(mods, e.getSimpleName(), constructTypeParams(e.getTypeParameters()), computeSuper(e.getSuperclass()), computeSuper(e.getInterfaces()), members));
                    case INTERFACE:
                        return addDeprecated(e, make.Interface(mods, e.getSimpleName(), constructTypeParams(e.getTypeParameters()), computeSuper(e.getInterfaces()), members));
                    case ENUM:
                        return addDeprecated(e, make.Enum(mods, e.getSimpleName(), computeSuper(e.getInterfaces()), members));
                    case RECORD:
                        // TODO generates final class atm
                        return addDeprecated(e, make.Class(mods, e.getSimpleName(), constructTypeParams(e.getTypeParameters()), null, computeSuper(e.getInterfaces()), members));
//                        return addDeprecated(e, make.Record(mods, e.getSimpleName(), computeSuper(e.getInterfaces()), members));
                    case ANNOTATION_TYPE:
                        return addDeprecated(e, make.AnnotationType(mods, e.getSimpleName(), members));
                    default:
                        throw new UnsupportedOperationException();
                }
            } finally {
                cf = oldCf;
                sig2Method = oldMethods;
            }
        }

        private ModifiersTree computeMods(Element e) {
            Set<Modifier> implicitModifiers = IMPLICIT_MODIFIERS.get(Arrays.asList(e.getKind()));

            if (implicitModifiers == null) {
                implicitModifiers = IMPLICIT_MODIFIERS.get(Arrays.asList(e.getKind(), e.getEnclosingElement().getKind()));
            }

            Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

            modifiers.addAll(e.getModifiers());

            if (implicitModifiers != null) {
                modifiers.removeAll(implicitModifiers);
            }

            List<AnnotationTree> annotations = new LinkedList<AnnotationTree>();

            for (AnnotationMirror m : e.getAnnotationMirrors()) {
                annotations.add(computeAnnotationTree(m));
            }

            return make.Modifiers(modifiers, annotations);
        }

        private <T extends Tree> T addDeprecated(Element e, T orig)  {
            if (!wc.getElements().isDeprecated(e) || true) return orig;

            for (AnnotationMirror am : e.getAnnotationMirrors()) {
                if (((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().contentEquals("java.lang.Deprecated")) {
                    return orig; //do not add the artificial @deprecated javadoc when there is a @Deprecated annotation
                }
            }

            Comment javadoc = Comment.create(Comment.Style.JAVADOC, "@deprecated");

            wc.getTreeMaker().addComment(orig, javadoc, true);

            return orig;
        }

        private AnnotationTree computeAnnotationTree(AnnotationMirror am) {
            List<ExpressionTree> params = new LinkedList<ExpressionTree>();

            for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                ExpressionTree val = createTreeForAnnotationValue(make, entry.getValue());

                if (val == null) {
                    LOG.log(Level.WARNING, "Cannot create annotation for: {0}", entry.getValue());
                    continue;
                }

                ExpressionTree vt = make.Assignment(make.Identifier(entry.getKey().getSimpleName()), val);

                params.add(vt);
            }

            return make.Annotation(make.Type(am.getAnnotationType()), params);
        }

        private Tree computeSuper(TypeMirror superClass) {
            if (superClass == null) return null;
            if (superClass.getKind() == TypeKind.NONE) return null; //for j.l.Object
            TypeElement jlObject = wc.getElements().getTypeElement("java.lang.Object"); //NOI18N
            if (jlObject != null && wc.getTypes().isSameType(superClass, jlObject.asType())) return null; //for extends j.l.Object

            return make.Type(superClass);
        }

        private List<Tree> computeSuper(List<? extends TypeMirror> superTypes) {
            List<Tree> sup = new LinkedList<Tree>();

            if (superTypes != null) {
                for (TypeMirror tm : superTypes) {
                    sup.add(make.Type(tm));
                }
            }

            return sup;
        }

        private List<? extends TypeParameterTree> constructTypeParams(List<? extends TypeParameterElement> params) {
            List<TypeParameterTree> result = new LinkedList<TypeParameterTree>();

            for (TypeParameterElement e : params) {
                result.add((TypeParameterTree) visit(e));
            }

            return result;
        }

        private List<? extends ExpressionTree> constructModuleList(List<? extends ModuleElement> params) {
            List<ExpressionTree> result = new LinkedList<>();

            if (params != null) {
                for (ModuleElement e : params) {
                    result.add(make.QualIdent(e.getQualifiedName().toString()));
                }
            }

            return result;
        }

        private List<? extends ExpressionTree> constructTypeList(List<? extends TypeElement> params) {
            List<ExpressionTree> result = new LinkedList<>();

            for (TypeElement e : params) {
                result.add(make.QualIdent(e));
            }

            return result;
        }

        @Override
        public Tree visitVariable(VariableElement e, Void p) {
            if (e.getKind() == ElementKind.ENUM_CONSTANT) {
                int mods = 1 << 14;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());

                return make.Variable(modifiers,
                                     e.getSimpleName().toString(),
                                     make.Identifier(e.getEnclosingElement().getSimpleName().toString()),
                                     null);
            }

            ModifiersTree mods = computeMods(e);
            LiteralTree init = e.getConstantValue() != null ? make.Literal(e.getConstantValue()) : null;

            return addDeprecated(e, make.Variable(mods, e.getSimpleName(), make.Type(e.asType()), init));
        }

        @Override
        public Tree visitExecutable(ExecutableElement e, Void p) {
            if (e.getKind() == ElementKind.STATIC_INIT || e.getKind() == ElementKind.INSTANCE_INIT) {
                return null; //XXX
            }

            if (wc.getElementUtilities().isSynthetic(e)) {
                return null;
            }

            //special case: <parent>[] values(), <parent> value(String) if <parent> is enum:
            if (e.getEnclosingElement().getKind() == ElementKind.ENUM) {
                if ("values".equals(e.getSimpleName().toString()) && e.getParameters().isEmpty()) {
                    return null;
                }

                if ("valueOf".equals(e.getSimpleName().toString()) && e.getParameters().size() == 1) {
                    TypeMirror param = e.getParameters().get(0).asType();

                    if (param.getKind() == TypeKind.DECLARED && "java.lang.String".equals(((TypeElement) ((DeclaredType) param).asElement()).getQualifiedName().toString())) {
                        return null;
                    }
                }
            }

            ModifiersTree mods = computeMods(e);
            Tree returnValue = e.getReturnType() != null ? make.Type(e.getReturnType()) : null;
            List<VariableTree> parameters = new LinkedList<VariableTree>();

            for (VariableElement param : e.getParameters()) {
                parameters.add((VariableTree) visit(param));
            }

            List<ExpressionTree> throwsList = new LinkedList<ExpressionTree>();

            for (TypeMirror t : e.getThrownTypes()) {
                throwsList.add((ExpressionTree) make.Type(t));
            }

            if (e.getModifiers().contains(Modifier.ABSTRACT) || e.getModifiers().contains(Modifier.NATIVE)) {
                ExpressionTree def = createTreeForAnnotationValue(make, e.getDefaultValue());
                return addDeprecated(e, make.Method(mods, e.getSimpleName(), returnValue, constructTypeParams(e.getTypeParameters()), parameters, throwsList, (BlockTree) null, def));
            } else {
                MethodTree method = make.Method(mods, e.getSimpleName(), returnValue, constructTypeParams(e.getTypeParameters()), parameters, throwsList, "{ }", null);
                String[] signature = SourceUtils.getJVMSignature(ElementHandle.create(e));
                Method m = sig2Method.get(signature[1] + ":" + signature[2]);
                CommentHandler handler = CommentHandlerService.instance(JavaSourceAccessor.getINSTANCE().getJavacTask(wc).getContext());
                CommentSet set = handler.getComments(method.getBody());

                if (m != null) {
                    Attribute code = m.attributes.get(Attribute.Code);

                    if (code instanceof Code_attribute) {
                        Context ctx = new Context();
                        StringWriter decompiled = new StringWriter();
                        PrintWriter w = new PrintWriter(decompiled);
                        ctx.put(PrintWriter.class, w);
                        ctx.put(Messages.class, new Messages() {
                            @Override public String getMessage(String key, Object... args) {
                                return "";
                            }
                            @Override public String getMessage(Locale locale, String key, Object... args) {
                                return "";
                            }
                        });
                        ctx.put(ClassWriter.class, new ClassWriter(ctx) {
                            {
                                setClassFile(cf);
                            }
                        });
                        ctx.put(CodeWriter.class, new ConvenientCodeWriter(ctx));

                        CodeWriter codeWriter = CodeWriter.instance(ctx);

                        codeWriter.writeInstrs((Code_attribute) code);
                        codeWriter.writeExceptionTable((Code_attribute) code);

                        w.println();
                        w.close();

                        set.addComment(RelativePosition.INNER, Comment.create(Style.LINE, "<editor-fold defaultstate=\"collapsed\" desc=\"Compiled Code\">"));
                        set.addComment(RelativePosition.INNER, Comment.create(decompiled.toString()));
                        set.addComment(RelativePosition.INNER, Comment.create(Style.LINE, "</editor-fold>"));
                    }
                }

                if (!set.hasComments()) {
                    set.addComment(RelativePosition.INNER, Comment.create(Style.LINE, "compiled code"));
                }

                return addDeprecated(e, method);
            }
        }

        @Override
        public Tree visitTypeParameter(TypeParameterElement e, Void p) {
            List<ExpressionTree> bounds = new LinkedList<ExpressionTree>();

            for (TypeMirror b : e.getBounds()) {
                bounds.add((ExpressionTree) make.Type(b));
            }

            return make.TypeParameter(e.getSimpleName(), bounds);
        }

        private ExpressionTree createTreeForAnnotationValue(final TreeMaker make, AnnotationValue def) {
            if (def == null) {
                return null;
            }
            return def.accept(new AnnotationValueVisitor<ExpressionTree, Void>() {
                @Override
                public ExpressionTree visit(AnnotationValue av, Void p) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                @Override
                public ExpressionTree visit(AnnotationValue av) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                @Override
                public ExpressionTree visitBoolean(boolean b, Void p) {
                    return make.Literal(b);
                }

                @Override
                public ExpressionTree visitByte(byte b, Void p) {
                    return make.Literal(b);
                }

                @Override
                public ExpressionTree visitChar(char c, Void p) {
                    return make.Literal(c);
                }

                @Override
                public ExpressionTree visitDouble(double d, Void p) {
                    return make.Literal(d);
                }

                @Override
                public ExpressionTree visitFloat(float f, Void p) {
                    return make.Literal(f);
                }

                @Override
                public ExpressionTree visitInt(int i, Void p) {
                    return make.Literal(i);
                }

                @Override
                public ExpressionTree visitLong(long i, Void p) {
                    return make.Literal(i);
                }

                @Override
                public ExpressionTree visitShort(short s, Void p) {
                    return make.Literal(s);
                }

                @Override
                public ExpressionTree visitString(String s, Void p) {
                    return make.Literal(s);
                }

                @Override
                public ExpressionTree visitType(TypeMirror t, Void p) {
                    return make.MemberSelect((ExpressionTree) make.Type(t), "class");   //NOI18N
                }

                @Override
                public ExpressionTree visitEnumConstant(VariableElement c, Void p) {
                    return make.QualIdent(c);
                }

                @Override
                public ExpressionTree visitAnnotation(AnnotationMirror a, Void p) {
                    return computeAnnotationTree(a);
                }

                @Override
                public ExpressionTree visitArray(List<? extends AnnotationValue> vals, Void p) {
                    List<ExpressionTree> values = new LinkedList<ExpressionTree>();

                    for (AnnotationValue v : vals) {
                        ExpressionTree val = createTreeForAnnotationValue(make, v);

                        if (val == null) {
                            LOG.log(Level.WARNING, "Cannot create annotation for: {0}", v);
                            continue;
                        }

                        values.add(val);
                    }

                    return make.NewArray(null, Collections.<ExpressionTree>emptyList(), values);
                }

                @Override
                public ExpressionTree visitUnknown(AnnotationValue av, Void p) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }, null);
        }

    }

    private static final class ConvenientCodeWriter extends CodeWriter {

        private final ConstantWriter constantWriter;

        public ConvenientCodeWriter(Context context) {
            super(context);
            constantWriter = ConstantWriter.instance(context);
        }

        private static final Set<Opcode> INSTRUCTION_WITH_REFERENCE =
                EnumSet.of(Opcode.LDC, Opcode.LDC_W, Opcode.LDC2_W,
                           Opcode.GETSTATIC, Opcode.PUTSTATIC, Opcode.GETFIELD,
                           Opcode.PUTFIELD, Opcode.INVOKEVIRTUAL, Opcode.INVOKESPECIAL,
                           Opcode.INVOKESTATIC, Opcode.INVOKEINTERFACE, Opcode.NEW,
                           Opcode.ANEWARRAY, Opcode.CHECKCAST, Opcode.INSTANCEOF);

        @Override
        public void writeInstr(Instruction instr) {
            super.writeInstr(instr);
        }
    }

    private static final Map<List<ElementKind>, Set<Modifier>> IMPLICIT_MODIFIERS;

    static {
        IMPLICIT_MODIFIERS = new HashMap<List<ElementKind>, Set<Modifier>>();

        IMPLICIT_MODIFIERS.put(Arrays.asList(ElementKind.ENUM), EnumSet.of(Modifier.STATIC, Modifier.ABSTRACT, Modifier.FINAL));
        // TODO implement record support
//        IMPLICIT_MODIFIERS.put(Arrays.asList(ElementKind.RECORD), EnumSet.of(Modifier.STATIC, Modifier.ABSTRACT, Modifier.FINAL));
        IMPLICIT_MODIFIERS.put(Arrays.asList(ElementKind.ANNOTATION_TYPE), EnumSet.of(Modifier.STATIC, Modifier.ABSTRACT));
        IMPLICIT_MODIFIERS.put(Arrays.asList(ElementKind.METHOD, ElementKind.ANNOTATION_TYPE), EnumSet.of(Modifier.ABSTRACT));
        IMPLICIT_MODIFIERS.put(Arrays.asList(ElementKind.METHOD, ElementKind.INTERFACE), EnumSet.of(Modifier.ABSTRACT));
    }

    private CodeGenerator() {
    }
}
