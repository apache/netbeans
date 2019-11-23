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

package org.netbeans.modules.php.api.documentation.registration;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.php.api.documentation.PhpDocumentations;
import org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Tomas Mysik
 */
@SupportedAnnotationTypes("org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider.Registration")
@ServiceProvider(service = Processor.class)
public class PhpDocumentationRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for (Element element : roundEnv.getElementsAnnotatedWith(PhpDocumentationProvider.Registration.class)) {
            layer(element)
                    .instanceFile(PhpDocumentations.DOCUMENTATION_PATH, null, PhpDocumentationProvider.class)
                    .intvalue("position", element.getAnnotation(PhpDocumentationProvider.Registration.class).position()) // NOI18N
                    .write();
        }
        return true;
    }

}
