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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.sendopts;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
final class OptionAnnotationProcessorImpl extends LayerGeneratingProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<String>();
        set.add(Arg.class.getName());
        return set;
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        PrimitiveType boolType = processingEnv.getTypeUtils().getPrimitiveType(TypeKind.BOOLEAN);
        TypeMirror stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        ArrayType stringArray = processingEnv.getTypeUtils().getArrayType(stringType);

        Set<TypeElement> processors = new HashSet<TypeElement>();
        for (Element e : roundEnv.getElementsAnnotatedWith(Arg.class)) {
            if (e.getModifiers().contains(Modifier.STATIC)) {
                throw new LayerGenerationException("@Arg can be applied only to non-static fields", e);
            }
            if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                throw new LayerGenerationException("@Arg can be applied only to public fields", e);
            }
            if (e.getModifiers().contains(Modifier.FINAL)) {
                throw new LayerGenerationException("@Arg can be applied only to non-final fields", e);
            }
            Arg arg = e.getAnnotation(Arg.class);
            if (arg.longName().isEmpty() && arg.shortName() == Option.NO_SHORT_NAME) {
                throw new LayerGenerationException("At least one of longName or shortName attributes needs to be non-empty", e);
            }
            if (arg.implicit() && !e.asType().equals(stringArray)) {
                throw new LayerGenerationException("implicit @Arg can only be applied to String[] fields", e);
            }
            
            processors.add(TypeElement.class.cast(e.getEnclosingElement()));
        }
        
        for (TypeElement te : processors) {
            int cnt = 1;
            final String typeName = processingEnv.getElementUtils().getBinaryName(te).toString().replace('.', '-');
            File f = layer(te).file("Services/OptionProcessors/" + typeName + ".instance");
            f.methodvalue("instanceCreate", DefaultProcessor.class.getName(), "create");
            f.stringvalue("instanceOf", OptionProcessor.class.getName());
            f.stringvalue("class", processingEnv.getElementUtils().getBinaryName(te).toString());
            for (Element e : te.getEnclosedElements()) {
                Arg o = e.getAnnotation(Arg.class);
                if (o == null) {
                    continue;
                }
                Description d = e.getAnnotation(Description.class);

                if (o.shortName() != Option.NO_SHORT_NAME) {
                    f.charvalue(cnt + ".shortName", o.shortName());
                }
                if (!o.longName().isEmpty()) {
                    f.stringvalue(cnt + ".longName", o.longName());
                }
                if (boolType == e.asType()) {
                    f.stringvalue(cnt + ".type", "withoutArgument");
                } else if (stringType == e.asType()) {
                    if (o.defaultValue().equals("\u0000")) {
                        f.stringvalue(cnt + ".type", "requiredArgument");
                    } else {
                        f.stringvalue(cnt + ".type", "optionalArgument");
                    }
                } else {
                    if (!stringArray.equals(e.asType())) {
                        throw new LayerGenerationException("Field type has to be either boolean, String or String[]!", e);
                    }
                    f.stringvalue(cnt + ".type", "additionalArguments");
                }
                if (o.implicit()) {
                    f.boolvalue(cnt + ".implicit", true);
                }
                if (d != null) {
                    writeBundle(f, cnt + ".displayName", d.displayName(), e);
                    writeBundle(f, cnt + ".shortDescription", d.shortDescription(), e);
                }
                cnt++;
            }            
            f.write();
        }
        return true;
    }

    private void writeBundle(File f, String key, String value, Element e) throws LayerGenerationException {
        if (value.isEmpty()) {
            return;
        }
        // test first; note that cannot call bundlevalue on f since its originatingElement is wrong
        layer(e).file("dummy/file").bundlevalue(key, value);
        
        if (value.startsWith("#")) {
            Element referenceElement = e;
            while (referenceElement != null && referenceElement.getKind() != ElementKind.PACKAGE) {
                referenceElement = referenceElement.getEnclosingElement();
            }
            if (referenceElement == null) {
                throw new LayerGenerationException("No reference element to determine package in '" + value + "'", e);
            }
            value = ((PackageElement) referenceElement).getQualifiedName() + ".Bundle" + value;
        }
        
        f.stringvalue(key, value);
    }
}
