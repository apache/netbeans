/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
