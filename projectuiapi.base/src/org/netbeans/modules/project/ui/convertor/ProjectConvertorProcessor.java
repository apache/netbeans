/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.project.ui.convertor;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ProjectConvertorProcessor extends LayerGeneratingProcessor {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ProjectConvertor.Registration.class.getCanonicalName());
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for (Element e : roundEnv.getElementsAnnotatedWith(ProjectConvertor.Registration.class)) {
            if (!e.getKind().isClass()) {
                throw new LayerGenerationException("Annotated element is not a class", e);   //NOI18N
            }
            final ProjectConvertor.Registration reg = e.getAnnotation(ProjectConvertor.Registration.class);
            final Elements elements = processingEnv.getElementUtils();
            final Types types = processingEnv.getTypeUtils();
            final TypeElement projectConvertor = elements.getTypeElement(ProjectConvertor.class.getName());
            if (types.isSubtype(((TypeElement)e).asType(), projectConvertor.asType())) {
                final LayerBuilder.File f = layer(e).instanceFile("Services/ProjectConvertors", null, null);    //NOI18N
                f.stringvalue("instanceOf", ProjectConvertorAcceptor.class.getName());   //NOI18N
                f.stringvalue("instanceClass", ProjectConvertorAcceptor.class.getName());   //NOI18N
                f.methodvalue("instanceCreate", ProjectConvertor.Result.class.getName(), "create");    //NOI18N
                final int position = reg.position();
                if (position >= 0) {
                    f.intvalue("position", position);   //NOI18N
                }
                f.instanceAttribute(ProjectConvertorAcceptor.ATTR_DELEGATE, ProjectConvertor.class);
                final String pattern = reg.requiredPattern();
                if (pattern == null || pattern.isEmpty()) {
                    throw new LayerGenerationException(
                        String.format("The %s has to be non empty string.", ProjectConvertorAcceptor.ATTR_PATTERN), //NOI18N
                        e);
                }
                try {
                    Pattern.compile(pattern);
                } catch (PatternSyntaxException ex) {
                    throw new LayerGenerationException(
                        String.format(
                            "The %s is not valid regular expression: %s.",  //NOI18N
                            ProjectConvertorAcceptor.ATTR_PATTERN,
                            ex.getMessage()),
                        e);
                }
                f.stringvalue(ProjectConvertorAcceptor.ATTR_PATTERN, pattern);
                f.write();
            } else {
                throw new LayerGenerationException("Annoated element is not a subclass of ProjectConvertor.",e); //NOI18N
            }
        }
        return true;
    }
}
