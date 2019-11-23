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
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class AnnotationProcessors {
    private AnnotationProcessors() {
    }

    @Hint(displayName="#DN_AnnotationProcessors.overridingGetSupportedAnnotations",
          description="#DESC_AnnotationProcessors.overridingGetSupportedAnnotations",
          category="rules15")
    @Messages({/*"DN_AnnotationProcessors.overridingGetSupportedAnnotations=AbstractProcessor.getSupportedAnnotations() is overridden",
               "DESC_AnnotationProcessors.overridingGetSupportedAnnotations=Overriding Processor.getSupportedAnnotations() may lead to " +
                                                                           "unnecessary classloading during development, and may prevent important optimalizations. " +
                                                                           "consider using @javax.annotation.processing.SupportedAnnotationTypes",*/
               "ERR_AnnotationProcessors.overridingGetSupportedAnnotations=AbstractProcessor.getSupportedAnnotationTypes() overridden, may cause performance problems during development"})
    @TriggerTreeKind(Kind.CLASS)
    public static ErrorDescription oGSA(HintContext ctx) {
        Element clazz = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (clazz == null || !clazz.getKind().isClass()) return null;

        TypeElement ap = ctx.getInfo().getElements().getTypeElement("javax.annotation.processing.AbstractProcessor");

        if (ap == null) return null;

        Types types = ctx.getInfo().getTypes();

        if (!types.isSubtype(types.erasure(clazz.asType()), types.erasure(ap.asType()))) return null;

        for (ExecutableElement ee : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
            if (ee.getSimpleName().contentEquals("getSupportedAnnotationTypes") && ee.getParameters().isEmpty()) {
                Tree t = ctx.getInfo().getTrees().getTree(ee);

                if (t != null) {
                    return ErrorDescriptionFactory.forName(ctx, t, Bundle.ERR_AnnotationProcessors_overridingGetSupportedAnnotations());
                }
            }
        }

        return null;
    }
    
    private static final String PROCESSOR_TYPE = Processor.class.getName();
    private static final String ABSTRACT_PROCESSOR_TYPE = AbstractProcessor.class.getName();
    private static final String SUPPORTED_SOURCE_TYPE = SupportedSourceVersion.class.getName();
    private static final String SOURCE_VERSION_TYPE = SourceVersion.class.getName();

    private static final String METHOD_SUPPORTED_SOURCE_VERSION = "getSupportedSourceVersion"; // NOI18N
    
    
    /**
     * Warns on incorrect @{@link SupportedSourceVersion} annotation. The hint triggers in
     * following cases, if the class does <b>not</b> override {@link javax.annotation.processing.Processor#getSupportedSourceVersion()}.
     * <ul>
     * <li>Class derived from AbstractProcessor with no annotation at all: defaults to 1.6, which
     * is almost certainly wrong.
     * <li>Declares {@code @SupportedSourceVersion} earlier than the project's source level.
     * </ul>
     * Offers a hint to declare a {@code @SupportedSourceVersion} or to override the {@code getSupportedSourceVersion}
     * to return {@link SourceVersion#latest()}.
     * @param ctx
     * @return 
     */
    @Hint(
        category = "rules15",
        displayName = "#DN_AnnoProcessor_ObsoleteSupportedSource",
        description = "#DESC_AnnoProcessor_ObsoleteSupportedSource",
        id = "obsoleteAnnotationSupportedSource",
        suppressWarnings = "ObsoleteAnnotationSupportedSource"
    )
    @TriggerPattern(
        value = "$modifiers$ class $name extends $superClass$ implements $superInterfaces$ { $body$; }", 
            constraints = @ConstraintVariableType(
                variable = "$superInterfaces",
                type = "javax.annotation.processing.Processor"
        )
    )
    @NbBundle.Messages({
        "DN_AnnoProcessor_ObsoleteSupportedSource=Missing or obsolete @SupportedSourceVersion",
        "HINT_AnnoProcessor_DeclaredSourceObsolete=Annoration declares older source version than the project source level.",
        "HINT_AnnoProcessor_NoSupportedSource=No @SupportedSourceVersion is declared, the default (1.6) is older than project source level."
    })
    public static ErrorDescription annotatedByObsoleteSource(HintContext ctx) {
        CompilationInfo info = ctx.getInfo();
        ProcessorHintSupport helper = new ProcessorHintSupport(ctx.getInfo(), ctx.getPath());
        if (!helper.initialize()) {
            return null;
        }
        // not an AbstractProcessor; or overrides the getSupported method.
        if (!helper.canOverrideAbstract(true)) {
            return null;
        }
        SupportedSourceVersion ssv = helper.getProcessor().getAnnotation(SupportedSourceVersion.class);
        SourceVersion current = info.getSourceVersion();
        if (ssv != null) {
            SourceVersion declared = ssv.value();
            if (declared.compareTo(current) >= 0) {
                // OK
                return null;
            }
            TreePath rewriteAt = helper.findSupportedAnnotation();
            if (rewriteAt == null) {
                return null;
            }
            
            return ErrorDescriptionFactory.forTree(ctx, rewriteAt, 
                    Bundle.HINT_AnnoProcessor_DeclaredSourceObsolete(), 
                    // suggest to generate latest()
                    new OverrideReturnLatest(info, helper.getProcessorPath(), false).toEditorFix(),
                    new OverrideReturnLatest(info, helper.getProcessorPath(), true).toEditorFix(),
                    // suggest change to the current version
                    changeToCurrentSource(ctx, helper, info.getSourceVersion())
            );
        } else {
            TreePath path = helper.getProcessorPath();
            return ErrorDescriptionFactory.forTree(ctx, path, 
                    Bundle.HINT_AnnoProcessor_NoSupportedSource(), 
                    new OverrideReturnLatest(info, path, false).toEditorFix(),
                    new OverrideReturnLatest(info, path, true).toEditorFix(),
                    new DeclareCurrentSourceFix(info, path, info.getSourceVersion()).toEditorFix()
            );
        }
    }
    
    /**
     * Code factored out from various hint branches.
     */
    public static class ProcessorHintSupport {
        private final CompilationInfo info;
        private final TreePath processorPath;
        
        private TypeElement baseProcessor;
        private TypeMirror baseProcessorType;
        private TypeElement abstractProcessor;
        private TypeMirror abstractProcessorType;
        private TypeElement supportedSource;
        private TypeMirror supportedSourceType;

        private TypeElement processor;
        
        private ExecutableElement abstractGetSupported;

        private ExecutableElement overridenGetSupported;
        
        public ProcessorHintSupport(CompilationInfo info, TreePath path) {
            this.info = info;
            this.processorPath = path;
        }
        
        public ExecutableElement findOverridenSupportedSource() {
            Element e = info.getElementUtilities().getImplementationOf(abstractGetSupported, processor);
            if (e == null || e.getEnclosingElement() == abstractProcessor || e.getKind() != ElementKind.METHOD) {
                return null;
            }
            return (ExecutableElement)e;
        }
        
        /**
         * Checks if the annotation types are reachable, initializes
         * types and elements for the API types.
         * @return false if the code is not in good shape.
         */
        public boolean initialize() {
            Element e = info.getTrees().getElement(processorPath);
            if (e == null || e.getKind() != ElementKind.CLASS) {
                return false;
            }
            processor = (TypeElement)e;
            if (processorPath.getLeaf().getKind() != Tree.Kind.CLASS) {
                return false;
            }

            abstractProcessor = info.getElements().getTypeElement(ABSTRACT_PROCESSOR_TYPE);
            if (!Utilities.isValidElement(e)) {
                return false;
            }
            abstractProcessorType = abstractProcessor.asType();
            baseProcessor = info.getElements().getTypeElement(PROCESSOR_TYPE);
            if (!Utilities.isValidElement(e)) {
                return false;
            }
            baseProcessorType = baseProcessor.asType();
            supportedSource = info.getElements().getTypeElement(SUPPORTED_SOURCE_TYPE);
            if (!Utilities.isValidElement(supportedSource)) {
                return false;
            }
            supportedSourceType = supportedSource.asType();
            if (!Utilities.isValidType(supportedSourceType)) {
                return false;
            }
            ExecutableElement ee = findGetSupported(abstractProcessor);
            if (ee == null) {
                return false;
            }
            abstractGetSupported = ee;
            
            return info.getTypes().isSubtype(processor.asType(), abstractProcessor.getSuperclass());
        }
        
        public ExecutableElement findGetSupported(Element clazz) {
            return ElementFilter.methodsIn(
                    clazz.getEnclosedElements()).
                    stream().filter(
                            (ExecutableElement x)
                            -> x.getSimpleName().contentEquals(METHOD_SUPPORTED_SOURCE_VERSION)
                    ).
                    findAny().
                    orElse(null);
        }
        
        public boolean canOverrideProcessor(boolean checkOverride) {
            if (!info.getTypes().isSubtype(processor.asType(), baseProcessorType)) {
                return false;
            }
            if (!checkOverride) {
                return true;
            }
            ExecutableElement ee = findGetSupported(baseProcessor);
            if (ee == null) {
                return false;
            }
            Element e = info.getElementUtilities().getImplementationOf(ee, processor);
            if (e == null) {
                return true;
            }
            if (!Utilities.isValidElement(e) || e.getKind() != ElementKind.METHOD) {
                return false;
            }
            overridenGetSupported = (ExecutableElement)e;
            return e.getEnclosingElement() == baseProcessor || 
                e.getEnclosingElement() == abstractProcessor;
        }
        
        /**
         * Determines if the class derives from AbstractProcessor.
         * @param checkOverride checks that getSupported* method is overriden
         * @return true if derived and method is NOT overriden
         */
        public boolean canOverrideAbstract(boolean checkOverride) {
            if (!info.getTypes().isSubtype(processor.asType(), abstractProcessorType)) {
                return false;
            }
            if (!checkOverride) {
                return true;
            }
            Element e = info.getElementUtilities().getImplementationOf(abstractGetSupported, processor);
            if (e == null) {
                return true;
            }
            if (!Utilities.isValidElement(e) || e.getKind() != ElementKind.METHOD) {
                return false;
            }
            overridenGetSupported = (ExecutableElement)e;
            return e.getEnclosingElement() == abstractProcessor;
        }

        public TypeElement getProcessor() {
            return processor;
        }
        
        public TreePath findSupportedAnnotation() {
            ClassTree ct = (ClassTree) processorPath.getLeaf();
            TreePath modPath = new TreePath(processorPath, ct.getModifiers());
            for (AnnotationTree at : ct.getModifiers().getAnnotations()) {
                TreePath tp = new TreePath(modPath, at);
                TypeMirror am = info.getTrees().getTypeMirror(tp);
                if (info.getTypes().isSameType(am, supportedSourceType)) {
                    return tp;
                }
            }
            return null;
        }

        public ExecutableElement getOverridenGetSupported() {
            return overridenGetSupported;
        }

        public TreePath getProcessorPath() {
            return processorPath;
        }
        
        @SuppressWarnings("element-type-mismatch")
        public void makeGetSupportedOverride(WorkingCopy wc, SourceVersion projectSource, 
                boolean removeSupportedAnnotation) {
            TreeMaker make = wc.getTreeMaker();
            
            BlockTree body = make.Block(Collections.singletonList(
                    make.Return(
                            projectSource != null ? 
                                make.MemberSelect(
                                        make.QualIdent(SOURCE_VERSION_TYPE),
                                        projectSource.name()
                                ) :
                                make.MethodInvocation(Collections.emptyList(), 
                                        make.MemberSelect(
                                                make.QualIdent(SOURCE_VERSION_TYPE),
                                                "latest" // NOI18N
                                        ),
                                        Collections.emptyList())
                    )
                ), false);
            MethodTree overrideMethod = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC), 
                            // @Override is since 1.5 as well as AnnotationProcessors.
                            // we always implement in subclass; so @Override is desired.
                            Collections.singletonList(
                                    make.Annotation(make.Identifier("Override"), Collections.emptyList())
                            )),
                    METHOD_SUPPORTED_SOURCE_VERSION,
                    make.QualIdent(SOURCE_VERSION_TYPE),
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), body, 
                    null);
        
            ClassTree ct = (ClassTree)getProcessorPath().getLeaf();
            ClassTree nct = make.addClassMember(ct, make.asNew(overrideMethod));

            if (removeSupportedAnnotation) {
                // if the class contains also @SupportSourceLevel declaration, remove it:
                TreePath tp = findSupportedAnnotation();
                if (tp != null) {
                    List<? extends AnnotationTree> annos = new ArrayList<>(nct.getModifiers().getAnnotations());
                    if (annos.remove(tp.getLeaf())) {
                        make.asRemoved(tp.getLeaf());
                        nct = make.Class(
                                make.Modifiers(nct.getModifiers(), annos), 
                                nct.getSimpleName().toString(), 
                                nct.getTypeParameters(), 
                                nct.getExtendsClause(), 
                                nct.getImplementsClause(), 
                                nct.getMembers()
                        );
                    }
                }
            }
            
            wc.rewrite(ct, nct);
        }
    }        
    
    /**
     * Creates a fix that changes the annotation to the current project's source
     * level.
     * @param target the target source version
     * @return 
     */
    @NbBundle.Messages({
        "# {0} - project source level",
        "FIX_AnnoProcessor_UseProjectSourceLevel=Use the project's source level ({0})",
        "# {0} - the target level",
        "FIX_AnnoProcessor_SpecificSourceLevel=Use the source level {0}"
    })
    static Fix changeToCurrentSource(HintContext ctx, ProcessorHintSupport support, SourceVersion target) {
        TreePath rewriteAt = support.findSupportedAnnotation();
        if (rewriteAt == null) {
            return null;
        }
        String msg = ctx.getInfo().getSourceVersion().compareTo(target) == 0 ?
                Bundle.FIX_AnnoProcessor_SpecificSourceLevel(levelToString(target)) :
                Bundle.FIX_AnnoProcessor_UseProjectSourceLevel(levelToString(target));
        return JavaFixUtilities.rewriteFix(ctx, msg, rewriteAt, 
                "@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion." + 
                        target.name() + ")");
    }
    
    public static String levelToString(SourceVersion l) {
        String s = l.name();
        switch (l) {
            case RELEASE_0:
                return "1";
            case RELEASE_1:
                return "1.1";
            case RELEASE_2: case RELEASE_3: case RELEASE_4:
                return "1." + s.charAt(s.length() - 1);
            default:
                int under = s.lastIndexOf('_');
                return s.substring(under + 1);
        }
    }
    
    @NbBundle.Messages({
            "# {0} - project's source level",
            "FIX_AnnoProcessor_OverrideProjectSupported=Override getSupportedSourceVersion() and return project''s source level ({0})",
            "FIX_AnnoProcessor_OverrideLatestSupported=Override getSupportedSourceVersion() and return SourceVersion.latest()"
    })
    public static class OverrideReturnLatest extends JavaFix {
        private final SourceVersion projectSource;
        
        public OverrideReturnLatest(CompilationInfo info, TreePath tp, boolean project) {
            super(info, tp, createSortedText(project));
            if (project) {
                projectSource = info.getSourceVersion();
            } else {
                projectSource = null;
            }
        }
        
        private static String createSortedText(boolean project) {
            return project ?
                    "010:FIX_AnnoProcessor_OverrideProjectSupported" :
                    "000:FIX_AnnoProcessor_OverrideLatestSupported";
        }
        
        @Override
        protected String getText() {
            return projectSource != null ?
                    Bundle.FIX_AnnoProcessor_OverrideProjectSupported(levelToString(projectSource)):
                    Bundle.FIX_AnnoProcessor_OverrideLatestSupported();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath path = ctx.getPath();
            if (path.getLeaf().getKind() != Tree.Kind.CLASS) {
                return;
            }
            ProcessorHintSupport support = new ProcessorHintSupport(ctx.getWorkingCopy(), path);
            if (!support.initialize()) {
                return;
            }
            support.makeGetSupportedOverride(ctx.getWorkingCopy(), projectSource, true);
        }
    }
    
    @NbBundle.Messages({
        "# {0} - project source level",
        "FIX_AnnoProcessor_AddProjectSourceLevel=Declare the project''s source level ({0})",
        "# {0} - the target level",
        "FIX_AnnoProcessor_AddSpecificSourceLevel=Declare the source level {0}"
    })
    private static class DeclareCurrentSourceFix extends JavaFix {
        private final SourceVersion target;
        private final boolean project;
        
        DeclareCurrentSourceFix(CompilationInfo info, TreePath tp, SourceVersion target) {
            super(info, tp);
            this.target = target;
            this.project = target.compareTo(info.getSourceVersion()) == 0;
        }
        
        @Override
        protected String getText() {
            return project ? 
                    Bundle.FIX_AnnoProcessor_AddProjectSourceLevel(levelToString(target)) :
                    Bundle.FIX_AnnoProcessor_AddSpecificSourceLevel(levelToString(target));
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath cpath = ctx.getPath();
            if (cpath.getLeaf().getKind() != Tree.Kind.CLASS) {
                return;
            }
            WorkingCopy wc = ctx.getWorkingCopy();
            TreeMaker make = wc.getTreeMaker();
            ClassTree ct = (ClassTree)cpath.getLeaf();
            ModifiersTree mt = ct.getModifiers();
            ModifiersTree nmt = make.Modifiers(mt, Collections.singletonList(
                    make.Annotation(
                            make.QualIdent(SUPPORTED_SOURCE_TYPE),
                            Collections.singletonList(
                                    make.MemberSelect(
                                            make.QualIdent(SOURCE_VERSION_TYPE), 
                                            wc.getSourceVersion().name()
                                    )
                            )
                    )
            ));
            wc.rewrite(mt, nmt);
        }
    }
}
