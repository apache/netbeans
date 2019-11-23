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

package org.netbeans.core.ide;

import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * processor for {@link ServiceTabNodeRegistration} annotation.
 * @author Jaroslav Tulach
 */
@NbBundle.Messages({
    "PlainResolver.Name=Text Files",
    "PlainResolver.FileChooserName=Text Files",
    "ResourceFiles=Resource Files"
})
@MIMEResolver.ExtensionRegistration(
    mimeType="text/plain",
    position=141,
    displayName="#PlainResolver.Name",
    extension={ "TXT", "txt" },
    showInFileChooser={"#PlainResolver.FileChooserName", "#ResourceFiles"}
)
@ServiceProvider(service=Processor.class)
public class ServiceTabProcessor extends LayerGeneratingProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ServicesTabNodeRegistration.class.getCanonicalName());
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(ServicesTabNodeRegistration.class)) {
            ServicesTabNodeRegistration reg = e.getAnnotation(ServicesTabNodeRegistration.class);
            File f = layer(e).
                instanceFile("UI/Runtime", null, Node.class).
                stringvalue("iconResource", reg.iconResource()).
                stringvalue("name", reg.name()).
                methodvalue("instanceCreate", "org.openide.nodes.NodeOp", "factory").
                bundlevalue("displayName", reg.displayName()).
                instanceAttribute("original", Node.class);
            if (!"".equals(reg.shortDescription())) {
                f.bundlevalue("shortDescription", reg.shortDescription());
            }
            f.position(reg.position());
            f.write();
        }
        return true;
    }

}
