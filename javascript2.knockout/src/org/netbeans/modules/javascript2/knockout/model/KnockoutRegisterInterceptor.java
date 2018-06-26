/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.knockout.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.knockout.index.KnockoutCustomElement;
import org.netbeans.modules.javascript2.knockout.index.KnockoutIndexer;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Roman Svitanic
 */
@FunctionInterceptor.Registration(priority = 400)
public class KnockoutRegisterInterceptor implements FunctionInterceptor {

    private static final Pattern NAME_PATTERN = Pattern.compile("ko\\.components\\.register"); // NOI18N

    @Override
    public Pattern getNamePattern() {
        return NAME_PATTERN;
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String name, JsObject globalObject,
            DeclarationScope scope, ModelElementFactory factory, Collection<FunctionArgument> args) {
        if (!KnockoutIndexer.isScannerThread()) {
            return Collections.emptyList();
        }
        String customElementName = null;
        int functionOffset = -1;
        int nameOffset = -1;
        String fqnOfCustomElement;

        for (FunctionArgument fArgument : args) {
            switch (fArgument.getKind()) {
                case STRING:
                    if (customElementName == null) {
                        // we expect that the first string parameter is the name of the custom element
                        customElementName = (String) fArgument.getValue();
                        nameOffset = fArgument.getOffset();
                    }
                    break;
                case ANONYMOUS_OBJECT:
                    functionOffset = fArgument.getOffset();
                    break;
            }
            if (customElementName != null && functionOffset != -1) {
                // we have probably found the custom component (element) registration
                break;
            }
        }

        if (customElementName != null) {
            // we need to find the function itself
            JsObject componentDecl = ModelUtils.findJsObject(globalObject, functionOffset);
            if (componentDecl != null && componentDecl.getJSKind() == JsElement.Kind.ANONYMOUS_OBJECT && componentDecl.isDeclared()) {
                fqnOfCustomElement = componentDecl.getFullyQualifiedName();
                FileObject fo = globalObject.getFileObject();
                Collection<String> componentParams = getComponentParameters(componentDecl);
                if (fo != null) {
                    KnockoutIndexer.addCustomElement(fo.toURI(),
                            new KnockoutCustomElement(customElementName, fqnOfCustomElement, componentParams, fo.toURL(), nameOffset));
                }
            }
        }
        return Collections.emptyList();
    }

    private Collection<String> getComponentParameters(JsObject component) {
        JsObject viewModel = component.getProperty("viewModel"); //NOI18N
        List<JsObject> functionParams = null;
        if (viewModel instanceof JsFunction) {
            functionParams = (List<JsObject>) (((JsFunction) viewModel).getParameters());
        } else if (viewModel instanceof JsObject) {
            JsObject createViewModel = viewModel.getProperty("createViewModel"); //NOI18N
            if (createViewModel != null) {
                functionParams = (List<JsObject>) (((JsFunction) createViewModel).getParameters());
            }
        }
        if (functionParams != null && !functionParams.isEmpty()) {
            return functionParams.get(0).getProperties().keySet();
        }
        return Collections.emptyList();
    }

}
