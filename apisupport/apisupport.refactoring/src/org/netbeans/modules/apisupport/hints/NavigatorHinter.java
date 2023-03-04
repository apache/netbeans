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

package org.netbeans.modules.apisupport.hints;

import com.sun.source.tree.ModifiersTree;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import static org.netbeans.modules.apisupport.hints.Bundle.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Hinter.class)
public class NavigatorHinter implements Hinter {

    private static final String PANELS_FOLDER = "Navigator/Panels/";
    private static final String REGISTRATION_BINARY = "org.netbeans.spi.navigator.NavigatorPanel$Registration";

    @Messages("NavigatorHinter.missing_dep=You must be using org.netbeans.spi.navigator 1.22+ before using this fix.")
    @Override public void process(final Context ctx) throws Exception {
        final FileObject file = ctx.file();
        if (!file.getPath().startsWith(PANELS_FOLDER)) {
            return;
        }
        final Object instanceCreate = ctx.instanceAttribute(file);
        if (instanceCreate != null) {
            ctx.addStandardAnnotationHint(new Callable<Void>() {
                public @Override Void call() throws Exception {
                    if (!ctx.canAccess(REGISTRATION_BINARY)) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NavigatorHinter_missing_dep(), NotifyDescriptor.WARNING_MESSAGE));
                        return null;
                    }
                    ctx.findAndModifyDeclaration(instanceCreate, new Context.ModifyDeclarationTask() {
                        @Override public void run(WorkingCopy wc, Element declaration, ModifiersTree modifiers) throws Exception {
                            Map<String,Object> params = new LinkedHashMap<String,Object>();
                            params.put("mimeType", file.getParent().getPath().substring(PANELS_FOLDER.length()));
                            params.put("position", file.getAttribute("position"));
                            params.put("displayName", "#TODO");
                            String canon = REGISTRATION_BINARY.replace('$', '.');
                            ModifiersTree nue = ctx.addAnnotation(wc, modifiers, canon, canon + "s", params);
                            ctx.delete(file);
                            wc.rewrite(modifiers, GeneratorUtilities.get(wc).importFQNs(nue));
                        }
                    });
                    return null;
                }
            });
        }
    }

}
