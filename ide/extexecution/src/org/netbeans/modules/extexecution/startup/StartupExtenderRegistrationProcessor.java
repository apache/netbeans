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
package org.netbeans.modules.extexecution.startup;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
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
