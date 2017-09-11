/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.settings.convertors;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.settings.ConvertAsJavaBean;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.api.settings.FactoryMethod;
import org.netbeans.modules.settings.Env;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/** Processor to hide all the complexities of settings layer registration.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ConvertorProcessor extends LayerGeneratingProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
            ConvertAsProperties.class.getCanonicalName(),
            ConvertAsJavaBean.class.getCanonicalName(),
            FactoryMethod.class.getCanonicalName()
        ));
    }

    @Override
    protected boolean handleProcess(
        Set<? extends TypeElement> annotations,
        RoundEnvironment env
    ) throws LayerGenerationException {
        if (env.processingOver()) {
            return false;
        }

        for (Element e : env.getElementsAnnotatedWith(ConvertAsProperties.class)) {
            ConvertAsProperties reg = e.getAnnotation(ConvertAsProperties.class);

            String convElem = instantiableClassOrMethod(e, true, reg);
            final String dtd = reg.dtd();

            String dtdCode = convertPublicId(dtd);

                /*
            <folder name="xml">
             <folder name="entities">
              <folder name="NetBeans_org_netbeans_modules_settings_xtest">
                <file name="DTD_XML_FooSetting_1_0" url="nbres:/org/netbeans/modules/settings/resources/properties-1_0.dtd">
                    <attr name="hint.originalPublicID" stringvalue="-//NetBeans org.netbeans.modules.settings.xtest//DTD XML FooSetting 1.0//EN"/>
                 */
            layer(e).file("xml/entities" + dtdCode).
                url("nbres:/org/netbeans/modules/settings/resources/properties-1_0.dtd").
                stringvalue("hint.originalPublicID", dtd).write();
       /*
        <folder name="memory">
            <folder name="org">
                <folder name="netbeans">
                    <folder name="modules">
                        <folder name="settings">
                            <folder name="convertors">
                                <file name="FooSetting">
                                    <attr name="settings.providerPath"
                                    stringvalue="xml/lookups/NetBeans_org_netbeans_modules_settings_xtest/DTD_XML_FooSetting_1_0.instance"/>
           */
            layer(e).file("xml/memory/" + convElem.replace('.', '/')).
                stringvalue("settings.providerPath", "xml/lookups/" + dtdCode + ".instance").
                write();

       /*
        <folder name="lookups">
            <folder name="NetBeans_org_netbeans_modules_settings_xtest">
                <file name="DTD_XML_FooSetting_1_0.instance">
                    <attr name="instanceCreate" methodvalue="org.netbeans.api.settings.Factory.create"/>
                    <attr name="settings.convertor" methodvalue="org.netbeans.api.settings.Factory.properties"/>
                    <attr name="settings.instanceClass" stringvalue="org.netbeans.modules.settings.convertors.FooSetting"/>
                    <attr name="settings.instanceOf" stringvalue="org.netbeans.modules.settings.convertors.FooSetting"/>
                </file>
            </folder>
        */
            File f = layer(e).file("xml/lookups" + dtdCode + ".instance").
                methodvalue("instanceCreate", "org.netbeans.api.settings.Factory", "create").
                methodvalue("settings.convertor", "org.netbeans.api.settings.Factory", "properties").
                stringvalue("settings.instanceClass", convElem).
                stringvalue("settings.instanceOf", convElem).
                boolvalue("xmlproperties.preventStoring", !reg.autostore());
            commaSeparated(f, reg.ignoreChanges()).write();
        }


        for (Element e : env.getElementsAnnotatedWith(ConvertAsJavaBean.class)) {
            ConvertAsJavaBean reg = e.getAnnotation(ConvertAsJavaBean.class);
            if (reg == null) {
                continue;
            }
            String convElem = instantiableClassOrMethod(e, false, reg);
            File f = layer(e).file("xml/memory/" + convElem.replace('.', '/'));
            f.stringvalue("settings.providerPath", "xml/lookups/NetBeans/DTD_XML_beans_1_0.instance");
            if (reg.subclasses()) {
                f.boolvalue(Env.EA_SUBCLASSES, true);
            }
            f.write();
        }
        
        for (Element e : env.getElementsAnnotatedWith(FactoryMethod.class)) {
            FactoryMethod m = e.getAnnotation(FactoryMethod.class);
            if (m == null) {
                continue;
            }
            
            boolean found = false;
            for (Element ch : e.getEnclosedElements()) {
                if (ch.getKind() != ElementKind.METHOD) {
                    continue;
                }
                
                if (!m.value().equals(ch.getSimpleName().toString())) {
                    continue;
                }
                
                ExecutableElement ee = (ExecutableElement)ch;
                
                if (ee.getParameters().size() > 0) {
                    throw new LayerGenerationException("Factory method " + m.value() + " must have no parameters", ee);
                }

                if (!ee.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException("Factory method " + m.value() + " has to be static", ee);
                }
                
                if (!processingEnv.getTypeUtils().isSameType(ee.getReturnType(), e.asType())) {
                    throw new LayerGenerationException("Factory method " + m.value() + " must return " + e.getSimpleName(), ee);
                }
                
                found = true;
            }
            
            if (!found) {
                throw new LayerGenerationException("Method named " + m.value() + " was not found in this class", e);
            }
        }
        
        return true;
    }

    /** Copied from FileEntityResolver from o.n.core module.
     */
    @SuppressWarnings("fallthrough")
    private static String convertPublicId (String publicID) {
        char[] arr = publicID.toCharArray ();


        int numberofslashes = 0;
        int state = 0;
        int write = 0;
        OUT: for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];

            switch (state) {
            case 0:
                // initial state
                if (ch == '+' || ch == '-' || ch == 'I' || ch == 'S' || ch == 'O') {
                    // do not write that char
                    continue;
                }
                // switch to regular state
                state = 1;
                // fallthru
            case 1:
                // regular state expecting any character
                if (ch == '/') {
                    state = 2;
                    if (++numberofslashes == 3) {
                        // last part of the ID, exit
                        break OUT;
                    }
                    arr[write++] = '/';
                    continue;
                }
                break;
            case 2:
                // previous character was /
                if (ch == '/') {
                    // ignore second / and write nothing
                    continue;
                }
                state = 1;
                break;
            }

            // write the char into the array
            if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9') {
                arr[write++] = ch;
            } else {
                arr[write++] = '_';
            }
        }

        return new String (arr, 0, write);
    }

    private static File commaSeparated(File f, String[] arr) {
        if (arr.length == 0) {
            return f;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String s : arr) {
            sb.append(sep);
            sb.append(s);
            sep = ",";
        }
        return f.stringvalue("xmlproperties.ignoreChanges", sb.toString());
    }

    private String instantiableClassOrMethod(Element e, boolean checkMethods, Annotation r) throws IllegalArgumentException, LayerGenerationException {
        switch (e.getKind()) {
            case CLASS: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                if (e.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new LayerGenerationException(clazz + " must not be abstract", e, processingEnv, r);
                }
                {
                    boolean hasDefaultCtor = false;
                    for (ExecutableElement constructor : ElementFilter.constructorsIn(e.getEnclosedElements())) {
                        if (constructor.getParameters().isEmpty()) {
                            hasDefaultCtor = true;
                            break;
                        }
                    }
                    if (!hasDefaultCtor) {
                        throw new LayerGenerationException(clazz + " must have a no-argument constructor", e, processingEnv, r);
                    }
                }
                if (checkMethods) {
                    TypeMirror propType;
                    propType = processingEnv.getElementUtils().getTypeElement("java.util.Properties").asType();
                    boolean hasRead = false;
                    boolean hasWrite = false;
                    for (ExecutableElement m : ElementFilter.methodsIn(e.getEnclosedElements())) {
                        if (
                            m.getParameters().size() == 1 && 
                            m.getSimpleName().contentEquals("readProperties") &&
                            m.getParameters().get(0).asType().equals(propType)
                        ) {
                            hasRead = true;
                        }
                        if (
                            m.getParameters().size() == 1 &&
                            m.getSimpleName().contentEquals("writeProperties") &&
                            m.getParameters().get(0).asType().equals(propType) &&
                            m.getReturnType().getKind() == TypeKind.VOID
                        ) {
                            hasWrite = true;
                        }
                    }
                    if (!hasRead) {
                        throw new LayerGenerationException(clazz + " must have proper readProperties method", e, processingEnv, r);
                    }
                    if (!hasWrite) {
                        throw new LayerGenerationException(clazz + " must have proper writeProperties method", e, processingEnv, r);
                    }
                }
                return clazz;
            }
            default:
                throw new LayerGenerationException("Annotated element is not loadable as an instance", e, processingEnv, r);
        }
    }
}
