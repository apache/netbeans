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
package org.netbeans.modules.versioning.core;

import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.versioning.core.spi.VersioningSystem;
import org.netbeans.modules.versioning.core.spi.VersioningSystem.Registration;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Stupka
 */
@ServiceProvider(service=Processor.class)
public class VCSRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(VersioningSystem.Registration.class.getCanonicalName());
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for (Element e : roundEnv.getElementsAnnotatedWith(VersioningSystem.Registration.class)) {
            Registration r = e.getAnnotation(VersioningSystem.Registration.class);
            if(r == null) {
                continue;
            }
            String s = processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString();
            
            File f = layer(e).instanceFile("Services/VersioningSystem", null, r, null); // NOI18N
            f.methodvalue("instanceCreate", DelegatingVCS.class.getName(), "create");    // NOI18N
            f.stringvalue("instanceOf", VersioningSystem.class.getName()); 
            String[] folderNames = r.metadataFolderNames();
            for (int i = 0; i < folderNames.length; i++) {
                f.stringvalue("metadataFolderName" + i, folderNames[i]);        // NOI18N
            }
            f.instanceAttribute("delegate", VersioningSystem.class);            // NOI18N
            f.bundlevalue("displayName", r.displayName());                      // NOI18N
            f.bundlevalue("menuLabel", r.menuLabel());                          // NOI18N
            f.stringvalue("actionsCategory", r.actionsCategory());              // NOI18N    
            f.boolvalue("isLocalHistory", s.equals("org.netbeans.modules.localhistory.LocalHistoryVCS")); // NOI18N    
            f.write();
        }
        return true;
    }
    
}
