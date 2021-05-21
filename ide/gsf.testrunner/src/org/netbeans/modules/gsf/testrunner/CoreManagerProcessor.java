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

package org.netbeans.modules.gsf.testrunner;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.gsf.testrunner.api.CoreManager;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Theofanis Oikonomou
 */
@ServiceProvider(service=Processor.class)
@SupportedAnnotationTypes("org.netbeans.modules.gsf.testrunner.api.CoreManager.Registration")
public class CoreManagerProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {

        for (Element e : roundEnv.getElementsAnnotatedWith(CoreManager.Registration.class)) {
            CoreManager.Registration registration = e.getAnnotation(CoreManager.Registration.class);            
            if(registration == null) {
                continue;
            }
            LayerBuilder.File f = layer(e).instanceFile("Services", null);
            f.stringvalue("instanceOf", CoreManager.class.getName());
            f.bundlevalue("displayName", registration.projectType().concat("_").concat(registration.testingFramework()));
            f.write();
        }

        return true;
    }
    
}
