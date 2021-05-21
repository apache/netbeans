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
package org.netbeans.modules.parsing.impl;

import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=Processor.class)
public class EmbeddingProviderRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(EmbeddingProvider.Registration.class.getCanonicalName());
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for (Element e :  roundEnv.getElementsAnnotatedWith(EmbeddingProvider.Registration.class)) {
            if (!e.getKind().isClass()) {
                throw new LayerGenerationException("Annotated Element has to be a class.", e);  //NOI18N
            }
            final EmbeddingProvider.Registration reg = e.getAnnotation(EmbeddingProvider.Registration.class);
            String mimeType = reg.mimeType();
            if (mimeType == null) {
                throw new LayerGenerationException("Mime type has to be given.", e);  //NOI18N
            } else if (!mimeType.isEmpty()) {
                mimeType =  '/' + mimeType; //NOI18N
            }
            String targetMimeType = reg.targetMimeType();
            if (targetMimeType == null || targetMimeType.isEmpty()) {
                throw new LayerGenerationException("Target mime type has to be given.", e);  //NOI18N
            }
            layer(e).
                instanceFile("Editors" + mimeType, null, null).    //NOI18N
                stringvalue("instanceOf", TaskFactory.class.getName()).         ///NOI18N
                methodvalue("instanceCreate", EmbeddingProviderFactory.class.getName(), "create").         //NOI18N
                stringvalue(EmbeddingProviderFactory.ATTR_TARGET_MIME_TYPE, targetMimeType).
                instanceAttribute(EmbeddingProviderFactory.ATTR_PROVIDER, EmbeddingProvider.class).
                write();
        }
        return true;
    }

}
