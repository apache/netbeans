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

package org.netbeans.modules.lsp.bridge;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.lsp.bridge.RegisterLSPServices;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
public final class RegisterLSPServicesProcessor extends LayerGeneratingProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hash = new HashSet<String>();
        hash.add(RegisterLSPServices.class.getCanonicalName());
        return hash;
    }

    @Override
    protected boolean handleProcess(
        Set<? extends TypeElement> annotations, RoundEnvironment roundEnv
    ) throws LayerGenerationException {
        for (Element e : roundEnv.getElementsAnnotatedWith(RegisterLSPServices.class)) {
            RegisterLSPServices services = (RegisterLSPServices) e.getAnnotation(RegisterLSPServices.class);
            if (services == null) {
                continue;
            }
            LayerBuilder builder = layer(e);
            for (String mimeType : services.mimeTypes()) {
                File provider = builder.file("Editors/" + mimeType + "/org-netbeans-modules-lsp-client-bridge-BridgingLanguageServerProvider.instance");
                provider.stringvalue("instanceOf", "org.netbeans.modules.lsp.client.spi.LanguageServerProvider");
                provider.write();
                File breadcrumbs = builder.file("Editors/" + mimeType + "/SideBar/breadcrumbs.instance");
                breadcrumbs.stringvalue("location", "South");
                breadcrumbs.intvalue("position", 5237);
                breadcrumbs.boolvalue("scrollable", false);
                breadcrumbs.methodvalue("instanceCreate", "org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController", "createSideBarFactory");
                breadcrumbs.write();
            }
        }
        return true;
    }

}
