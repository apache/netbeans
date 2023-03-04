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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.lookup.ServiceProvider;
import static org.netbeans.modules.apisupport.hints.Bundle.*;

/**
 * #191236: {@code ActionRegistration} conversion.
 */
@ServiceProvider(service=Hinter.class)
public class ActionRegistrationHinter implements Hinter {

     public @Override void process(final Context ctx) throws Exception {
        final FileObject file = ctx.file();
        final Object instanceCreate = ctx.instanceAttribute(file);
        if (instanceCreate == null) {
            return;
        }
        if ("method:org.openide.awt.Actions.alwaysEnabled".equals(instanceCreate)) {
            ctx.addStandardAnnotationHint(new Callable<Void>() {
                public @Override Void call() throws Exception {
                    if (!annotationsAvailable(ctx)) {
                        return null;
                    }
                    ctx.findAndModifyDeclaration(file.getAttribute("literal:delegate"), new RegisterAction(ctx, false));
                    return null;
                }
            });
        } else if ("method:org.openide.awt.Actions.checkbox".equals(instanceCreate)) {
            // #193279: no associated annotation available
        } else if ("method:org.openide.awt.Actions.callback".equals(instanceCreate) || "method:org.openide.awt.Actions.context".equals(instanceCreate)) {
            ctx.addHint(Severity.WARNING, ctx.standardAnnotationDescription()/* XXX no fixes yet */);
        } else if ("method:org.openide.windows.TopComponent.openAction".equals(instanceCreate)) {
            // XXX pending #191407: @OpenActionRegistration (w/ @ActionID and @ActionReference)
            // (could also do @Registration but would be a separate Hinter)
            // (@Description probably needed but harder since need to remove method overrides)
        } else if (file.getPath().startsWith("Actions/")) {
            // Old-style eager action of some variety.
            ctx.addStandardAnnotationHint(new Callable<Void>() {
                public @Override Void call() throws Exception {
                    if (!annotationsAvailable(ctx)) {
                        return null;
                    }
                    ctx.findAndModifyDeclaration(instanceCreate, new RegisterAction(ctx, true));
                    return null;
                }
            });
        }
    }

    @Messages("ActionRegistrationHinter.missing_org.openide.awt=You must add a dependency on org.openide.awt (7.27+) before using this fix.")
    private boolean annotationsAvailable(Context ctx) {
        if (ctx.canAccess("org.openide.awt.ActionReferences")) {
            return true;
        } else {
            DialogDisplayer.getDefault().notify(new Message(ActionRegistrationHinter_missing_org_openide_awt(), NotifyDescriptor.WARNING_MESSAGE));
            return false;
        }
    }

    private static class RegisterAction implements Context.ModifyDeclarationTask {

        private final Context ctx;
        private final boolean eager;

        RegisterAction(Context ctx, boolean eager) {
            this.ctx = ctx;
            this.eager = eager;
        }

        public @Override void run(WorkingCopy wc, Element declaration, ModifiersTree modifiers) throws Exception {
            Map<String,Object> params = new HashMap<String,Object>();
            FileObject file = ctx.file();
            params.put("category", file.getParent().getPath().substring("Actions/".length()));
            params.put("id", file.getName().replace('-', '.'));
            ModifiersTree nue = ctx.addAnnotation(wc, modifiers, "org.openide.awt.ActionID", null, params);
            params.clear();
            String displayName = ctx.bundlevalue(file.getAttribute("literal:displayName"), declaration);
            if (displayName == null) {
                // @ActionRegistration requires this attr, even though it is unused for eager actions.
                displayName = "#TODO";
            }
            params.put("displayName", displayName);
            params.put("iconBase", file.getAttribute("iconBase"));
            Object noIconInMenu = file.getAttribute("noIconInMenu");
            if (noIconInMenu instanceof Boolean) {
                params.put("iconInMenu", !((Boolean) noIconInMenu));
            }
            params.put("asynchronous", file.getAttribute("asynchronous"));
            if (eager) {
                params.put("lazy", false);
            } else {
                // XXX specify lazy=true if implements one of the 5 specials even though using Actions.* factory (but probably rare)
            }
            nue = ctx.addAnnotation(wc, nue, "org.openide.awt.ActionRegistration", null, params);
            ctx.delete(file);
            TreeMaker make = wc.getTreeMaker();
            List<AnnotationTree> anns = new ArrayList<AnnotationTree>();
            for (FileObject shadow : NbCollections.iterable(file.getFileSystem().getRoot().getData(true))) {
                if (!shadow.hasExt("shadow")) {
                    continue;
                }
                if (file.getPath().equals(shadow.getAttribute("originalFile"))) {
                    List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
                    arguments.add(make.Assignment(make.Identifier("path"), make.Literal(shadow.getParent().getPath())));
                    String name = shadow.getName();
                    if (!name.equals(file.getName())) {
                        arguments.add(make.Assignment(make.Identifier("name"), make.Literal(name)));
                    }
                    Object pos = shadow.getAttribute("position");
                    if (pos instanceof Integer) {
                        arguments.add(make.Assignment(make.Identifier("position"), make.Literal(pos)));
                    }
                    // XXX maybe look for nearby separators?
                    TypeElement ann = wc.getElements().getTypeElement("org.openide.awt.ActionReference");
                    if (ann == null) {
                        throw new IllegalArgumentException("Could not find ActionReference in classpath");
                    }
                    anns.add(make.Annotation(make.QualIdent(ann), arguments));
                    ctx.delete(shadow);
                }
            }
            if (anns.size() == 1) {
                nue = make.addModifiersAnnotation(nue, anns.get(0));
            } else if (!anns.isEmpty()) {
                TypeElement ann = wc.getElements().getTypeElement("org.openide.awt.ActionReferences");
                if (ann == null) {
                    throw new IllegalArgumentException("Could not find ActionReferences in classpath");
                }
                nue = make.addModifiersAnnotation(nue, make.Annotation(make.QualIdent(ann), Collections.singletonList(make.Assignment(make.Identifier("value"), make.NewArray(null, Collections.<ExpressionTree>emptyList(), anns)))));
            }
            wc.rewrite(modifiers, GeneratorUtilities.get(wc).importFQNs(nue));
        }

    }

}
