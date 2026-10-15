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
package org.netbeans.modules.lsp.client.debugger;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.lsp.client.debugger.api.RegisterDAPDebugger;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
@SupportedAnnotationTypes("org.netbeans.modules.lsp.client.debugger.api.RegisterDAPDebugger")
public class RegisterDAPDebuggerProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for (Element el : roundEnv.getElementsAnnotatedWith(RegisterDAPDebugger.class)) {
            LayerBuilder builder = layer(el);

            for (String mimeType : el.getAnnotation(RegisterDAPDebugger.class).mimeType()) {
                builder.file("Editors/" + mimeType + "/breakpoints.instance")
                       .stringvalue("instanceOf", "org.netbeans.modules.lsp.client.debugger.api.RegisterDAPBreakpoints")
                       .methodvalue("instanceCreate", "org.netbeans.modules.lsp.client.debugger.api.RegisterDAPBreakpoints", "newInstance")
                       .write()
                       .file("Editors/" + mimeType + "/GlyphGutterActions/org-netbeans-modules-debugger-ui-actions-ToggleBreakpointAction.shadow")
                       .stringvalue("originalFile", "Actions/Debug/org-netbeans-modules-debugger-ui-actions-ToggleBreakpointAction.instance")
                       .intvalue("position", 500)
                       .write();
            }
        }
        return true;
    }

}
