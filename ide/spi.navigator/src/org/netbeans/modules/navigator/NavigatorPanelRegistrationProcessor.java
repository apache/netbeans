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

package org.netbeans.modules.navigator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
public class NavigatorPanelRegistrationProcessor extends LayerGeneratingProcessor {

    @Override public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(NavigatorPanel.Registration.class.getCanonicalName(), NavigatorPanel.Registrations.class.getCanonicalName()));
    }

    @Override protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(NavigatorPanel.Registration.class)) {
            NavigatorPanel.Registration r = e.getAnnotation(NavigatorPanel.Registration.class);
            if (r == null) {
                continue;
            }
            register(e, r);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(NavigatorPanel.Registrations.class)) {
            NavigatorPanel.Registrations rr = e.getAnnotation(NavigatorPanel.Registrations.class);
            if (rr == null) {
                continue;
            }
            for (NavigatorPanel.Registration r : rr.value()) {
                register(e, r);
            }
        }
        return true;
    }

    private void register(Element e, NavigatorPanel.Registration r) throws LayerGenerationException {
        String suffix = layer(e).instanceFile("dummy", null, null, r, null).getPath().substring("dummy".length()); // e.g. /my-Panel.instance
        layer(e).file(ProviderRegistry.PANELS_FOLDER + r.mimeType() + suffix).
                methodvalue("instanceCreate", LazyPanel.class.getName(), "create").
                instanceAttribute("delegate", NavigatorPanel.class, r, null).
                position(r.position()).
                bundlevalue("displayName", r.displayName()).
                write();
    }

}
