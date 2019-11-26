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
package org.netbeans.modules.htmlui;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import org.netbeans.api.htmlui.OpenHTMLRegistration;
import org.openide.awt.ActionID;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jtulach
 */
@ServiceProvider(service = Processor.class)
public class HTMLViewProcessor extends LayerGeneratingProcessor {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hash = new HashSet<>();
        hash.add(OpenHTMLRegistration.class.getCanonicalName());
        return hash;
    }
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> set, RoundEnvironment re) throws LayerGenerationException {
        for (Element e : re.getElementsAnnotatedWith(OpenHTMLRegistration.class)) {
            OpenHTMLRegistration reg = e.getAnnotation(OpenHTMLRegistration.class);
            if (reg == null || e.getKind() != ElementKind.METHOD) {
                continue;
            }
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                error("Method annotated by @OpenHTMLRegistration needs to be static", e);
            }
            if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                error("Method annotated by @OpenHTMLRegistration needs to be public", e);
            }
            if (!((ExecutableElement)e).getParameters().isEmpty()) {
                error("Method annotated by @OpenHTMLRegistration should have no arguments", e);
            }
            if (!e.getEnclosingElement().getModifiers().contains(Modifier.PUBLIC)) {
                error("Method annotated by @OpenHTMLRegistration needs to be public in a public class", e);
            }
            
            ActionID aid = e.getAnnotation(ActionID.class);
            if (aid != null) {
                final LayerBuilder builder = layer(e);
                LayerBuilder.File actionFile = builder.
                        file("Actions/" + aid.category() + "/" + aid.id().replace('.', '-') + ".instance").
                        methodvalue("instanceCreate", "org.netbeans.modules.htmlui.Pages", "openAction");
                String abs = LayerBuilder.absolutizeResource(e, reg.url());
                try {
                    builder.validateResource(abs, e, reg, null, true);
                } catch (LayerGenerationException ex) {
                    if (System.getProperty("netbeans.home") != null) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Cannot find resource " + abs, e);
                    } else {
                        throw ex;
                    }
                }
                actionFile.stringvalue("url", abs);
                if (!reg.iconBase().isEmpty()) {
                    actionFile.stringvalue("iconBase", reg.iconBase());
                }
                actionFile.stringvalue("method", e.getSimpleName().toString());
                actionFile.stringvalue("class", e.getEnclosingElement().asType().toString());
                String[] techIds = reg.techIds();
                for (int i = 0; i < techIds.length; i++) {
                    actionFile.stringvalue("techId." + i, techIds[i]);
                }
//                actionFile.instanceAttribute("component", TopComponent.class, reg, null);
//                if (reg.preferredID().length() > 0) {
//                    actionFile.stringvalue("preferredID", reg.preferredID());
//                }
                actionFile.bundlevalue("displayName", reg.displayName(), reg, "displayName");
                actionFile.write();
            } else {
                error("@OpenHTMLRegistration needs to be accompanied with @ActionID annotation", e);
            }
            
        }
        return true;
    }

    private void error(final String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}
