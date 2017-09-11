/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.modules;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;
import org.openide.modules.ConstructorDelegate;
import org.openide.modules.PatchFor;
import org.openide.modules.PatchedPublic;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
public class PatchedPublicProcessor extends AbstractProcessor {
    
    private static final Set<String> ANNOTATIONS = new HashSet<String>(
        Arrays.asList(
            PatchedPublic.class.getCanonicalName(),
            PatchFor.class.getCanonicalName(),
            ConstructorDelegate.class.getCanonicalName()
        )
    );

    public @Override Set<String> getSupportedAnnotationTypes() {
        return ANNOTATIONS;
    }

    private List<Element> originatingElements;
    
    /**
     * Map keyed by the original API class, values are superclasses to be injected
     * into the API class.
     */
    private Map<String, String>     superclasses = new HashMap<String, String>();
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        originatingElements = new ArrayList<Element>();
    }
    
    private void flush(RoundEnvironment roundEnv) {
        if (!originatingElements.isEmpty()) {
            try (OutputStream os = processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "", "META-INF/.bytecodePatched",
                    originatingElements.toArray(new Element[originatingElements.size()])).openOutputStream()) {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                for (Map.Entry<String, String> exEntry : superclasses.entrySet()) {
                    String api = exEntry.getKey();
                    String sup = exEntry.getValue();
                    
                    bw.append("extend.").append(api).append("=").append(sup);
                    bw.newLine();
                }
                bw.flush();
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR, x.getMessage());
            }
        }
    }
    
    private TypeElement implForElement;
    private Element valueElement;
    private boolean reported;
    
    private TypeElement getImplFor() {
        implForElement = processingEnv.getElementUtils().getTypeElement(IMPL_FOR_NAME);
        if (implForElement == null) {
            if (!reported) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Cannot find @ImplementationFor annotation");
                reported = true;
            }
            return null;
        }
        for (Element e : implForElement.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD && e.getSimpleName().contentEquals("value")) {
                valueElement = e;
                break;
            }
        }
        return implForElement;
    }
    
    private static final String IMPL_FOR_NAME = PatchFor.class.getName();

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            flush(roundEnv);
            return false;
        }
        
        for (Element e : roundEnv.getElementsAnnotatedWith(PatchedPublic.class)) {
            if (e.getAnnotationMirrors().size() > 1) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Cannot currently mix @PatchedPublic with other annotations", e);
                continue;
            }
            if (e.getModifiers().contains(Modifier.PUBLIC)) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "@PatchedPublic cannot be applied to what is already public", e);
                continue;
            }
            originatingElements.add(e);
        }
        
        CLAZZ: for (Element e : roundEnv.getElementsAnnotatedWith(PatchFor.class)) {
            List<? extends AnnotationMirror> mirrors = e.getAnnotationMirrors();
            String apiName = null;
            TypeElement target = null;
            
            for (AnnotationMirror m : mirrors) {
                Element me = m.getAnnotationType().asElement();
                if (me != getImplFor()) {
                    continue;
                }
                AnnotationValue val = m.getElementValues().get(valueElement);
                if (!(val.getValue() instanceof DeclaredType)) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "Value for @PatchFor must be a valid class", e);
                    continue CLAZZ;
                }
                Element x = ((DeclaredType)val.getValue()).asElement();
                if (!(x instanceof TypeElement)) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "Value for @PatchFor must be a valid class", e);
                    continue CLAZZ;
                }
                target = (TypeElement)x;
                if (target.getKind() != ElementKind.CLASS) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "@PatchFor can be only applied on classes", e);
                    continue CLAZZ;
                }
                apiName = processingEnv.getElementUtils().getBinaryName(target).toString();
                break;
            }
            if (target == null) {
                throw new IllegalStateException();
            }
            TypeElement t = (TypeElement)e;
            
            boolean defaultCtorFound = false;
            for (Element el : t.getEnclosedElements()) {
                if (el.getKind() != ElementKind.CONSTRUCTOR) {
                    continue;
                }
                if (((ExecutableElement)el).getParameters().isEmpty()) {
                    defaultCtorFound = true;
                    break;
                }
            }
            /*
            if (!defaultCtorFound) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Class " + t.getQualifiedName().toString() + " has no default constructor", e);
                continue CLAZZ;
            }
            */
            String superName = processingEnv.getElementUtils().getBinaryName(t).toString();
            
            TypeMirror patchSuperClass = t.getSuperclass();
            TypeMirror targetSuperClass = target.getSuperclass();
            
            if (!processingEnv.getTypeUtils().isSameType(patchSuperClass, targetSuperClass)) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "API class and the substitue differ in their superclasses", e);
                continue CLAZZ;
            }
            superclasses.put(apiName, superName);
            processingEnv.getMessager().printMessage(Kind.NOTE, "Adding injection of " + superName + " as a superclass of API " + apiName);
            
            originatingElements.add(e);
        }
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
