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

package org.netbeans.modules.java.hints.spiimpl.processor;

import java.lang.reflect.Array;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractAnnotationValueVisitor6;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**Inspired by https://sezpoz.dev.java.net/.
 *
 * @author lahvac
 */
@SupportedAnnotationTypes("org.netbeans.spi.java.hints.*")
@ServiceProvider(service=Processor.class, position=100)
public class JavaHintsAnnotationProcessor extends LayerGeneratingProcessor {
    
    private static final Logger LOG = Logger.getLogger(JavaHintsAnnotationProcessor.class.getName());
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (!roundEnv.processingOver()) {
            generateTypeList("org.netbeans.spi.java.hints.Hint", roundEnv);
        }

        return false;
    }

    private static final String[] TRIGGERS = new String[] {
        "org.netbeans.spi.java.hints.TriggerTreeKind",
        "org.netbeans.spi.java.hints.TriggerPattern",
        "org.netbeans.spi.java.hints.TriggerPatterns",
    };

    private static final String[] OPTIONS = new String[] {
        "org.netbeans.spi.java.hints.BooleanOption"
    };

    private void generateTypeList(String annotationName, RoundEnvironment roundEnv) throws LayerGenerationException {
        TypeElement hint = processingEnv.getElementUtils().getTypeElement(annotationName);

        if (hint == null) return ;
        
        for (Element annotated : roundEnv.getElementsAnnotatedWith(hint)) {
            if (!verifyHintAnnotationAcceptable(annotated)) continue;
            if (!annotated.getKind().isClass() && !annotated.getKind().isInterface()) {
                if (annotated.getKind() != ElementKind.METHOD) {
                    //the compiler should have already warned about this
                    continue;
                }

                annotated = annotated.getEnclosingElement();
            } else {
                if (!annotated.getKind().isClass()) {
                    //the compiler should have already warned about this
                    continue;
                }
            }

            if (!annotated.getKind().isClass()) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Internal error - cannot find class containing the hint", annotated);
                continue;
            }

            TypeElement clazz = (TypeElement) annotated;
            String classFolder = "org-netbeans-modules-java-hints/code-hints/" + getFQN(clazz).replace('.', '-') + ".class";

            {
                LayerBuilder builder = layer(clazz);
                File clazzFolder = builder.folder(classFolder);
                
                for (AnnotationMirror am : clazz.getAnnotationMirrors()) {
                    dumpAnnotation(builder, clazzFolder, clazz, am, true);
                }
                
                clazzFolder.write();
            }

            for (ExecutableElement ee : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
                if (!ee.getAnnotationMirrors().isEmpty()) {
                    LayerBuilder builder = layer(ee);
                    File methodFolder = builder.folder(classFolder + "/" + ee.getSimpleName() + ".method");

                    for (AnnotationMirror am : ee.getAnnotationMirrors()) {
                        dumpAnnotation(builder, methodFolder, ee, am, true);
                    }

                    methodFolder.write();
                }
            }

            for (VariableElement var : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
                if (!var.getAnnotationMirrors().isEmpty()) {
                    LayerBuilder builder = layer(var);
                    File fieldFolder = builder.folder(classFolder + "/" + var.getSimpleName() + ".field");

                    for (AnnotationMirror am : var.getAnnotationMirrors()) {
                        dumpAnnotation(builder, fieldFolder, var, am, true);
                    }

                    if (var.getConstantValue() instanceof String) {
                        fieldFolder.stringvalue("constantValue", (String) var.getConstantValue());
                    }

                    fieldFolder.write();
                }
            }
            
            new ElementScanner6<Void, Void>() {
                @Override public Void scan(Element e, Void p) {
                    AnnotationMirror hintMirror = findAnnotation(e.getAnnotationMirrors(), "org.netbeans.spi.java.hints.Hint");
            
                    if (hintMirror != null) {
                        String qualifiedName;
                        switch (e.getKind()) {
                            case METHOD: case CONSTRUCTOR:
                                qualifiedName = e.getEnclosingElement().asType().toString() + "." + e.getSimpleName().toString() + e.asType().toString();
                                break;
                            case FIELD: case ENUM_CONSTANT:
                                qualifiedName = e.getEnclosingElement().asType().toString() + "." + e.getSimpleName().toString();
                                break;
                            case ANNOTATION_TYPE: case CLASS:
                            case ENUM: case INTERFACE:
                            default:
                                qualifiedName = e.asType().toString();
                                break;
                        }
                        
                        try {
                            File keywordsFile = layer(e)
                                               .file("OptionsDialog/Keywords/".concat(qualifiedName))
                                               .stringvalue("location", "Editor")
                                               .bundlevalue("tabTitle", "org.netbeans.modules.options.editor.Bundle", "CTL_Hints_DisplayName");

                            String displayName = getAttributeValue(hintMirror, "displayName", String.class);

                            if (displayName != null)
                                keywordsFile = keywordsFile.bundlevalue("keywords-1", displayName);

                            String description = getAttributeValue(hintMirror, "description", String.class);

                            if (description != null)
                                keywordsFile = keywordsFile.bundlevalue("keywords-2", description);

                            int i = 3;

                            for (String sw : getAttributeValue(hintMirror, "suppressWarnings", String[].class)) {
                                keywordsFile = keywordsFile.stringvalue("keywords-" + i++, sw);
                            }

                            keywordsFile.write();
                        } catch (LayerGenerationException ex) {
                            JavaHintsAnnotationProcessor.<RuntimeException>rethrowAsRuntime(ex);
                        }
                    }

                    return super.scan(e, p);
                }
            }.scan(annotated, null);
        }

        for (String ann : TRIGGERS) {
            TypeElement annRes = processingEnv.getElementUtils().getTypeElement(ann);

            if (annRes == null) continue;

            for (ExecutableElement method : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(annRes))) {
                verifyHintMethod(method);
                verifyTriggerAnnotations(method);
            }
        }

        for (String ann : OPTIONS) {
            TypeElement annRes = processingEnv.getElementUtils().getTypeElement(ann);

            if (annRes == null) continue;

            for (VariableElement var : ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(annRes))) {
                verifyOptionField(var);
            }
        }

    }

    private void dumpAnnotation(LayerBuilder builder, File folder, Element errElement, AnnotationMirror annotation, boolean topLevel) {
        String fqn = getFQN(((TypeElement) annotation.getAnnotationType().asElement())).replace('.', '-');
        if (topLevel && !fqn.startsWith("org-netbeans-spi-java-hints")) return ;
        final File   annotationFolder = builder.folder(folder.getPath() + "/" + fqn + ".annotation");

        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : annotation.getElementValues().entrySet()) {
            final String attrName = e.getKey().getSimpleName().toString();
            e.getValue().accept(new DumpAnnotationValue(builder, annotationFolder, attrName, errElement, annotation, e.getValue()), null);
        }

        annotationFolder.write();
    }

    private String getFQN(TypeElement clazz) {
        return processingEnv.getElementUtils().getBinaryName(clazz).toString();
    }

    static final String ERR_RETURN_TYPE = "The return type must be either org.netbeans.spi.editor.hints.ErrorDescription or java.util.List<org.netbeans.spi.editor.hints.ErrorDescription>";
    static final String ERR_PARAMETERS = "The method must have exactly one parameter of type org.netbeans.spi.java.hints.HintContext";
    static final String ERR_MUST_BE_STATIC = "The method must be static";
    static final String ERR_OPTION_TYPE = "The option field must be of type java.lang.String";
    static final String ERR_OPTION_MUST_BE_STATIC_FINAL = "The option field must be static final";
    static final String WARN_BUNDLE_KEY_NOT_FOUND = "Bundle key %s not found";

    private static AnnotationMirror findAnnotation(Iterable<? extends AnnotationMirror> annotations, String annotationFQN) {
        for (AnnotationMirror am : annotations) {
            if (((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotationFQN)) {
                return am;
            }
        }

        return null;
    }

    private <T> T getAttributeValue(AnnotationMirror annotation, String attribute, Class<T> clazz) {
        if (clazz.isArray()) {
            Iterable<?> attributes = getAttributeValueInternal(annotation, attribute, Iterable.class);
            
            Collection<Object> coll = new ArrayList<Object>();
            Class<?> componentType = clazz.getComponentType();

            for (Object attr : attributes) {
                if (attr instanceof AnnotationValue) {
                    attr = ((AnnotationValue) attr).getValue();
                }
                
                if (componentType.isAssignableFrom(attr.getClass())) {
                    coll.add(componentType.cast(attr));
                }
            }

            return clazz.cast(coll.toArray((Object[]) Array.newInstance(clazz.getComponentType(), 0)));
        } else {
            return getAttributeValueInternal(annotation, attribute, clazz);
        }
    }

    private <T> T getAttributeValueInternal(AnnotationMirror annotation, String attribute, Class<T> clazz) {
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : processingEnv.getElementUtils().getElementValuesWithDefaults(annotation).entrySet()) {
            if (e.getKey().getSimpleName().contentEquals(attribute)) {
                Object value = e.getValue().getValue();

                if (clazz.isAssignableFrom(value.getClass())) {
                    return clazz.cast(value);
                }

                return null;
            }
        }
        
        return null;
    }

    private AnnotationValue getAttributeValueDescription(AnnotationMirror annotation, String attribute) {
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : processingEnv.getElementUtils().getElementValuesWithDefaults(annotation).entrySet()) {
            if (e.getKey().getSimpleName().contentEquals(attribute)) {
                return e.getValue();
            }
        }

        return null;
    }
    
    private boolean verifyHintAnnotationAcceptable(Element hint) {
        AnnotationMirror hintMirror = findAnnotation(hint.getAnnotationMirrors(), "org.netbeans.spi.java.hints.Hint");

        if (hintMirror == null) return false;

        String id = getAttributeValue(hintMirror, "id", String.class);

        if (id == null || id.isEmpty()) {
            switch (hint.getKind()) {
                case CLASS:
                case METHOD:
                    break; //OK
                default:
                    //compiler should have already warned about this
                    return false;
            }
        }

        TypeMirror customizerProviderType = getAttributeValue(hintMirror, "customizerProvider", TypeMirror.class);

        if (customizerProviderType != null) {
            Element customizerProvider = processingEnv.getTypeUtils().asElement(customizerProviderType);

            if (customizerProvider != null) {
                if (customizerProvider.getKind() != ElementKind.CLASS) {
                    TypeElement customizerProviderInterface = processingEnv.getElementUtils().getTypeElement("org.netbeans.spi.java.hints.CustomizerProvider");

                    if (customizerProviderInterface != null && !customizerProviderInterface.equals(customizerProvider)) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, "Customizer provider must be a concrete class", hint, hintMirror, getAttributeValueDescription(hintMirror, "customizerProvider"));
                    }
                } else {
                    TypeElement customizerProviderClazz = (TypeElement) customizerProvider;

                    if (!customizerProviderClazz.getModifiers().contains(Modifier.PUBLIC)) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, "Customizer provider must be public", hint, hintMirror, getAttributeValueDescription(hintMirror, "customizerProvider"));
                    }

                    if (   customizerProviderClazz.getEnclosingElement().getKind() != ElementKind.PACKAGE
                        && !customizerProviderClazz.getModifiers().contains(Modifier.STATIC)) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, "Customizer provider must be non-static innerclass", hint, hintMirror, getAttributeValueDescription(hintMirror, "customizerProvider"));
                    }

                    boolean foundDefaultConstructor = false;

                    for (ExecutableElement ee : ElementFilter.constructorsIn(customizerProviderClazz.getEnclosedElements())) {
                        if (ee.getParameters().isEmpty()) {
                            foundDefaultConstructor = true;
                            if (!ee.getModifiers().contains(Modifier.PUBLIC)) {
                                processingEnv.getMessager().printMessage(Kind.ERROR, "Customizer provider must provide a public default constructor", hint, hintMirror, getAttributeValueDescription(hintMirror, "customizerProvider"));
                            }
                            break;
                        }
                    }

                    if (!foundDefaultConstructor) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, "Customizer provider must provide a public default constructor", hint, hintMirror, getAttributeValueDescription(hintMirror, "customizerProvider"));
                    }
                }
            }
        }

        return true;
    }

    private boolean verifyHintMethod(ExecutableElement method) {
        StringBuilder error = new StringBuilder();
        Elements elements = processingEnv.getElementUtils();
        TypeElement errDesc = elements.getTypeElement("org.netbeans.spi.editor.hints.ErrorDescription");
        TypeElement jlIterable = elements.getTypeElement("java.lang.Iterable");
        TypeElement hintCtx = elements.getTypeElement("org.netbeans.spi.java.hints.HintContext");

        if (errDesc == null || jlIterable == null || hintCtx == null) {
            return true;
        }

        Types types = processingEnv.getTypeUtils();
        TypeMirror errDescType = errDesc.asType(); //no type params, no need to erasure
        TypeMirror jlIterableErrDesc = types.getDeclaredType(jlIterable, errDescType);
        TypeMirror ret = method.getReturnType();

        if (!types.isSameType(ret, errDescType) && !types.isAssignable(ret, jlIterableErrDesc)) {
            error.append(ERR_RETURN_TYPE);
            error.append("\n");
        }

        if (method.getParameters().size() != 1 || !types.isSameType(method.getParameters().get(0).asType(), hintCtx.asType())) {
            error.append(ERR_PARAMETERS);
            error.append("\n");
        }

        if (!method.getModifiers().contains(Modifier.STATIC)) {
            error.append(ERR_MUST_BE_STATIC);
            error.append("\n");
        }

        if (error.length() == 0) {
            return true;
        }

        if (error.charAt(error.length() - 1) == '\n') {
            error.delete(error.length() - 1, error.length());
        }

        processingEnv.getMessager().printMessage(Kind.ERROR, error.toString(), method);

        return false;
    }

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$[a-zA-Z0-9_]+");
    private boolean verifyTriggerAnnotations(ExecutableElement method) {
        List<AnnotationMirror> patternAnnotations = new ArrayList<AnnotationMirror>();
        AnnotationMirror am = findAnnotation(method.getAnnotationMirrors(), "org.netbeans.spi.java.hints.TriggerPattern");

        if (am != null) {
            patternAnnotations.add(am);
        }

        am = findAnnotation(method.getAnnotationMirrors(), "org.netbeans.spi.java.hints.TriggerPatterns");

        if (am != null) {
            patternAnnotations.addAll(Arrays.asList(getAttributeValue(am, "value", AnnotationMirror[].class)));
        }

        for (AnnotationMirror patternDescription : patternAnnotations) {
            String pattern = getAttributeValue(patternDescription, "value", String.class);

            if (pattern == null) continue;

            Set<String> variables = new HashSet<String>();
            Matcher m = VARIABLE_PATTERN.matcher(pattern);

            while (m.find()) {
                variables.add(m.group(0));
            }

            for (AnnotationMirror constraint : getAttributeValue(patternDescription, "constraints", AnnotationMirror[].class)) {
                String variable = getAttributeValue(constraint, "variable", String.class);
                String type = getAttributeValue(constraint, "type", String.class);

                if (variable == null || type == null) continue;

                if (!variables.contains(variable)) {
                    processingEnv.getMessager().printMessage(Kind.WARNING, "Variable " + variable + " not used in the pattern", method, constraint, getAttributeValueDescription(constraint, "variable"));
                }
            }
        }

        return false;
    }

    private boolean verifyOptionField(VariableElement field) {
        StringBuilder error = new StringBuilder();
        Elements elements = processingEnv.getElementUtils();
        TypeElement jlString = elements.getTypeElement("java.lang.String");

        if (jlString == null) {
            return true;
        }

        Types types = processingEnv.getTypeUtils();
        TypeMirror jlStringType = jlString.asType(); //no type params, no need to erasure

        if (!types.isSameType(field.asType(), jlStringType)) {
            error.append(ERR_RETURN_TYPE);
            error.append("\n");
        }

        if (!field.getModifiers().contains(Modifier.STATIC) || !field.getModifiers().contains(Modifier.FINAL)) {
            error.append(ERR_OPTION_MUST_BE_STATIC_FINAL);
            error.append("\n");
        }

        Object key = field.getConstantValue();

        if (key == null) {
            error.append("Option field not a compile-time constant");
            error.append("\n");
        }

        if (error.length() == 0) {
            return true;
        }

        if (error.charAt(error.length() - 1) == '\n') {
            error.delete(error.length() - 1, error.length());
        }

        processingEnv.getMessager().printMessage(Kind.ERROR, error.toString(), field);

        return false;
    }

    private class DumpAnnotationValue extends AbstractAnnotationValueVisitor6<Void, Void> {

        private final LayerBuilder builder;
        private final File annotationFolder;
        private final String attrName;
        private final Element errElement;
        private final AnnotationMirror errAnnotationMirror;
        private final AnnotationValue errAnnotationValue;

        public DumpAnnotationValue(LayerBuilder builder, File annotationFolder, String attrName, Element errElement, AnnotationMirror errAnnotationMirror, AnnotationValue errAnnotationValue) {
            this.builder = builder;
            this.annotationFolder = annotationFolder;
            this.attrName = attrName;
            this.errElement = errElement;
            this.errAnnotationMirror = errAnnotationMirror;
            this.errAnnotationValue = errAnnotationValue;
        }

        public Void visitBoolean(boolean b, Void p) {
            annotationFolder.boolvalue(attrName, b);
            return null;
        }

        public Void visitByte(byte b, Void p) {
            annotationFolder.bytevalue(attrName, b);
            return null;
        }

        public Void visitChar(char c, Void p) {
            annotationFolder.charvalue(attrName, c);
            return null;
        }

        public Void visitDouble(double d, Void p) {
            annotationFolder.doublevalue(attrName, d);
            return null;
        }

        public Void visitFloat(float f, Void p) {
            annotationFolder.floatvalue(attrName, f);
            return null;
        }

        public Void visitInt(int i, Void p) {
            annotationFolder.intvalue(attrName, i);
            return null;
        }

        public Void visitLong(long i, Void p) {
            annotationFolder.longvalue(attrName, i);
            return null;
        }

        public Void visitShort(short s, Void p) {
            annotationFolder.shortvalue(attrName, s);
            return null;
        }

        public Void visitString(String s, Void p) {
            if ("displayName".equals(attrName) || "description".equals(attrName) || "tooltip".equals(attrName)) {
                try {
                    annotationFolder.bundlevalue(attrName, s);
                } catch (LayerGenerationException ex) {
                   processingEnv.getMessager().printMessage(Kind.ERROR, ex.getLocalizedMessage(), errElement, errAnnotationMirror, errAnnotationValue);
                }
            } else {
                annotationFolder.stringvalue(attrName, s);
            }
            return null;
        }

        public Void visitType(TypeMirror t, Void p) {
            annotationFolder.stringvalue(attrName, getFQN(((TypeElement) ((DeclaredType) t).asElement())));
            return null;
        }

        public Void visitEnumConstant(VariableElement c, Void p) {
            TypeElement owner = (TypeElement) c.getEnclosingElement();
            annotationFolder.stringvalue(attrName, getFQN(owner) + "." + c.getSimpleName());
            return null;
        }

        public Void visitAnnotation(AnnotationMirror a, Void p) {
            File f = builder.folder(annotationFolder.getPath() + "/" + attrName);
            
            dumpAnnotation(builder, f, errElement, a, false);

            f.write();
            return null;
        }

        public Void visitArray(List<? extends AnnotationValue> vals, Void p) {
            File arr = builder.folder(annotationFolder.getPath() + "/" + attrName);
            int c = 0;

            for (AnnotationValue av : vals) {
                av.accept(new DumpAnnotationValue(builder, arr, "item" + c, errElement, errAnnotationMirror, av), null);
                c++;
            }

            arr.write();

            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void rethrowAsRuntime(Throwable t) throws T {
        throw (T) t;
    }

}
