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

package org.netbeans.modules.php.api.ui.options;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.php.api.util.UiUtils.PhpOptionsPanelRegistration;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author S. Aubrecht
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes("org.netbeans.modules.php.api.util.UiUtils.PhpOptionsPanelRegistration")
public class PhpOptionsPanelControllerProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(PhpOptionsPanelRegistration.class)) {
            PhpOptionsPanelRegistration registration = element.getAnnotation(PhpOptionsPanelRegistration.class);
            if (registration.id().isEmpty()) {
                throw new LayerGenerationException("Registration id cannot be empty", element);
            }
            File file = layer(element)
                    .instanceFile(FrameworksOptionsPanelController.FRAMEWORKS_AND_TOOLS_OPTIONS_PATH, registration.id(), registration, null)
                    .methodvalue("instanceCreate", AdvancedOption.class.getName(), "createSubCategory") // NOI18N
                    .instanceAttribute("controller", OptionsPanelController.class) // NOI18N
                    .bundlevalue("displayName", registration.displayName()) // NOI18N
                    .position(registration.position());
            keywords(element, registration.keywords(), registration.keywordsCategory(), registration, file);
            file.write();
        }

        return true;
    }

    private void keywords(Element element, String keywords, String keywordsCategory, Annotation r, File file) throws LayerGenerationException {
        if (keywords.length() > 0) {
            if (keywordsCategory.length() == 0) {
                throw new LayerGenerationException("Must specify both keywords and keywordsCategory", element, processingEnv, r, "keywordsCategory");
            }
            file.bundlevalue("keywords", keywords, r, "keywords") // NOI18N
                    .bundlevalue("keywordsCategory", keywordsCategory, r, "keywordsCategory"); // NOI18N
        } else if (keywordsCategory.length() > 0) {
            throw new LayerGenerationException("Must specify both keywords and keywordsCategory", element, processingEnv, r, "keywords");
        }
    }

}
