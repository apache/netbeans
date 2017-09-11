/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
