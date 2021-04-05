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
package org.netbeans.modules.java.hints.ui;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.jdk.AnnotationProcessors.ProcessorHintSupport;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType = "text/x-java", service = CodeGenerator.Factory.class)
public class AnnoProcessorGenerator implements CodeGenerator.Factory {
    
    @Override
    public List<? extends CodeGenerator> create(Lookup context) {
        ArrayList<CodeGenerator> ret = new ArrayList<>();
        JTextComponent component = context.lookup(JTextComponent.class);
        CompilationController controller = context.lookup(CompilationController.class);
        if (component == null || controller == null) {
            return ret;
        }
        TreePath path = context.lookup(TreePath.class);
        path = controller.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
        if (path == null) {
            return ret;
        }
        try {
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
        } catch (IOException ioe) {
            return ret;
        }

        ProcessorHintSupport supp = new ProcessorHintSupport(controller, path);
        if (!supp.initialize()) {
            return ret;
        }
        if (!supp.canOverrideProcessor(true)) {
            return ret;
        }
        ret.add(new Gen(component, ElementHandle.create(supp.getProcessor())));
        return ret;
    }

    private static class Gen implements CodeGenerator {
        private final ElementHandle<TypeElement> handle;
        private final JTextComponent component;
        
        public Gen(JTextComponent component, ElementHandle<TypeElement> handle) {
            this.component = component;
            this.handle = handle;
        }

        @NbBundle.Messages({
                "GEN_AnnoProcessor_OverrideLatestSupported=Support SourceVersion.latest()"
        })
        @Override
        public String getDisplayName() {
            return Bundle.GEN_AnnoProcessor_OverrideLatestSupported();
        }

        @Override
        public void invoke() {
            JavaSource js = JavaSource.forDocument(component.getDocument());
            if (js == null) {
                return;
            }
            try {
                ModificationResult mr = js.runModificationTask(new Mod(handle));
                GeneratorUtils.guardedCommit(component, mr);
                int span[] = mr.getSpan("methodBodyTag"); // NOI18N
                if(span != null) {
                    component.setSelectionStart(span[0]);
                    component.setSelectionEnd(span[1]);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    private static class Mod implements Task<WorkingCopy> {
        private final ElementHandle<TypeElement> handle;

        public Mod(ElementHandle<TypeElement> handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(WorkingCopy parameter) throws Exception {
            parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            TypeElement resolved = handle.resolve(parameter);
            if (!Utilities.isValidElement(resolved)) {
                return;
            }
            TreePath path = parameter.getTrees().getPath(resolved);
            if (path == null) {
                return;
            }
            
            ProcessorHintSupport supp = new ProcessorHintSupport(parameter, path);
            if (!supp.initialize() || !supp.canOverrideProcessor(true)) {
                return;
            }
            supp.makeGetSupportedOverride(parameter, null, true);
        }
    }
}
