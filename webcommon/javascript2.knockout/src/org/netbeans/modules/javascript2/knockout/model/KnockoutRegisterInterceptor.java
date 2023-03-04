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
