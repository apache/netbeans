/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.extexecution.startup;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.extexecution.startup.StartupExtender.StartMode;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Petr Hejl
 */
@SupportedAnnotationTypes("org.netbeans.spi.extexecution.startup.StartupExtenderImplementation.Registration")
@ServiceProvider(service = Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class StartupExtenderRegistrationProcessor extends LayerGeneratingProcessor {

    public static final String PATH = "StartupExtender"; // NOI18N

    public static final String DELEGATE_ATTRIBUTE = "delegate"; // NOI18N

    public static final String START_MODE_ATTRIBUTE = "startMode"; // NOI18N

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(
                StartupExtenderImplementation.Registration.class)) {

            StartupExtenderImplementation.Registration annotation = element.getAnnotation(StartupExtenderImplementation.Registration.class);
            if (annotation == null) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            for (StartMode mode : annotation.startMode()) {
                builder.append(mode.name()).append(",");
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
            File f = layer(element).instanceFile(PATH, null)
                    .instanceAttribute(DELEGATE_ATTRIBUTE, StartupExtenderImplementation.class, annotation, null)
                    .stringvalue(START_MODE_ATTRIBUTE, builder.toString())
                    .bundlevalue("displayName", element.getAnnotation(StartupExtenderImplementation.Registration.class).displayName()) // NOI18N
                    .methodvalue("instanceCreate", "org.netbeans.spi.extexecution.startup.StartupExtender", "createProxy") // NOI18N
                    .position(element.getAnnotation(StartupExtenderImplementation.Registration.class).position()); // NOI18N
            f.write();
        }
        return true;
    }

}
