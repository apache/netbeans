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
package org.netbeans.modules.parsing.impl.indexing;

import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link LayerGeneratingProcessor} to generate {@link BinaryIndexer} registrations.
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @author Tomas Zezula
 */
@ServiceProvider(service=Processor.class)
public final class IndexerRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ConstrainedBinaryIndexer.Registration.class.getCanonicalName());
    }
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for (Element e : roundEnv.getElementsAnnotatedWith(ConstrainedBinaryIndexer.Registration.class)) {
            assert e.getKind().isClass();
            final ConstrainedBinaryIndexer.Registration reg = e.getAnnotation(ConstrainedBinaryIndexer.Registration.class);
            final Elements elements = processingEnv.getElementUtils();
            final Types types = processingEnv.getTypeUtils();
            final TypeElement binIndexerType = elements.getTypeElement(ConstrainedBinaryIndexer.class.getName());
            if (types.isSubtype(((TypeElement)e).asType(), binIndexerType.asType())) {
                final String indexerName = reg.indexerName();
                if (indexerName == null) {
                    throw new LayerGenerationException("Indexer name has to be given.", e); //NOI18N
                }
                final File f = layer(e).instanceFile("Editors", null, null);    //NOI18N
                f.stringvalue("instanceClass", BinaryIndexerFactory.class.getName());   //NOI18N
                f.methodvalue("instanceCreate", ConstrainedBinaryIndexer.class.getName(), "create");    //NOI18N
                f.instanceAttribute("delegate", ConstrainedBinaryIndexer.class);
                if (reg.requiredResource().length > 0) {
                    f.stringvalue("requiredResource", list(reg.requiredResource(), e)); //NOI18N
                }
                if (reg.mimeType().length > 0) {
                    f.stringvalue("mimeType", list(reg.mimeType(), e)); //NOI18N
                }
                if (reg.namePattern().length() > 0) {
                    f.stringvalue("namePattern", reg.namePattern()); //NOI18N
                }
                f.stringvalue("name", indexerName);         //NOI18N
                f.intvalue("version", reg.indexVersion());  //NOI18N
                f.write();
            } else {
                throw new LayerGenerationException("Annoated element is not a  subclass of BinaryIndexer.",e); //NOI18N
            }
        }
        return true;
    }

    private static String list(String[] arr, Element e) throws LayerGenerationException {
        if (arr.length == 1) {
            return arr[0];
        }
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            if (s.indexOf(',') >= 0) {  //NOI18N
                throw new LayerGenerationException("',' is not allowed in the text", e);    //NOI18N
            }
            sb.append(s).append(",");   //NOI18N
        }
        return sb.substring(0, sb.length() - 1);
    }
}
