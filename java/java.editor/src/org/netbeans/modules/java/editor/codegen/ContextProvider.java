/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.util.TreePath;
import java.io.IOException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Dusan Balek
 */
public class ContextProvider implements CodeGeneratorContextProvider {

    public void runTaskWithinContext(final Lookup context, final Task task) {
        JTextComponent component = context.lookup(JTextComponent.class);
        if (component != null) {
            try {
                JavaSource js = JavaSource.forDocument(component.getDocument());
                if (js != null) {
                    final int caretOffset = component.getCaretPosition();
                    js.runUserActionTask(new org.netbeans.api.java.source.Task<CompilationController>() {
                        public void run(CompilationController controller) throws Exception {
                            controller.toPhase(JavaSource.Phase.PARSED);
                            TreePath path = controller.getTreeUtilities().pathFor(caretOffset);
                            Lookup newContext = new ProxyLookup(context, Lookups.fixed(controller, path));
                            task.run(newContext);
                        }
                    }, true);
                    return;
                }
            } catch (IOException ioe) {
            }
        }
        task.run(context);
    }
}
