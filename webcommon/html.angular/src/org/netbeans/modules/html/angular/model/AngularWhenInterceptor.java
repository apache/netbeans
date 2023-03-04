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
package org.netbeans.modules.html.angular.model;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;
import org.netbeans.modules.html.angular.Utils;
import org.netbeans.modules.html.angular.index.AngularJsController;
import org.netbeans.modules.html.angular.index.AngularJsIndexer;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
@FunctionInterceptor.Registration(priority = 16)
public class AngularWhenInterceptor implements FunctionInterceptor {

    private static final Pattern PATTERN = Pattern.compile("(.)*\\.(when|state)");  //NOI18N
    public static final String TEMPLATE_URL_PROP = "templateUrl";  //NOI18N
    public static final String CONTROLLER_PROP = "controller";     //NOI18N
    public static final String CONTROLLER_AS_PROP = "controllerAs"; //NOI18N

    @Override
    public Pattern getNamePattern() {
        return PATTERN;
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, 
            String name, JsObject globalObject, DeclarationScope scope, ModelElementFactory factory, Collection<FunctionArgument> args) {
        if (!AngularJsIndexer.isScannerThread()) {
            return Collections.emptyList();
        }
        for (FunctionArgument arg : args) {
            if (arg.getKind() == FunctionArgument.Kind.ANONYMOUS_OBJECT) {
                JsObject aObject = (JsObject) arg.getValue();
                JsObject url = aObject.getProperty(TEMPLATE_URL_PROP);
                JsObject controller = aObject.getProperty(CONTROLLER_PROP);
                JsObject controllerAs = aObject.getProperty(CONTROLLER_AS_PROP);
                FileObject fo = globalObject.getFileObject();
                if (url != null && controller != null && fo != null) {
                    String content = null;
                    Source source = Source.create(fo);
                    content = source.createSnapshot().getText().toString();                   
                    if (content != null) {
                        String template = getStringValueAt(content, url.getOffsetRange().getStart());
                        String controllerName = getStringValueAt(content, controller.getOffsetRange().getStart());
                        if (controllerName.isEmpty()
                                && (controller.getJSKind() == JsElement.Kind.METHOD
                                || controller.getJSKind() == JsElement.Kind.FUNCTION)) {
                            // probably anonymous function as a controller
                            controllerName = controller.getFullyQualifiedName();
                            if (controllerName != null && !controllerName.isEmpty()) {
                                // save the controller itself to the index
                                AngularJsIndexer.addController(fo.toURI(),
                                        new AngularJsController(controllerName, controllerName, fo.toURL(), controller.getOffset()));
                            }
                        }
                        String controllerAsName = null;
                        if (controllerAs != null) {
                            controllerAsName = getStringValueAt(content, controllerAs.getOffsetRange().getStart());
                        }
                        if (template != null && controllerName != null) {
                            AngularJsIndexer.addTemplateController(fo.toURI(), Utils.cutQueryFromTemplateUrl(template), controllerName, controllerAsName);
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private enum STATE {

        INIT, INSTRING, INVALUE, END
    }

    @SuppressWarnings("fallthrough")
    private String getStringValueAt(String content, int offset) {
        String value = "";
        STATE state = STATE.INIT;
        while (state != STATE.END && offset < content.length()) {
            char ch = content.charAt(offset);
            switch (state) {
                case INIT:
                    switch (ch) {
                    case ':':
                        state = STATE.INVALUE;
                        break;
                    case '}':
                    case ',':
                    case '\'':
                    case '"':
                        state = STATE.END;
                        break;
                    default:
                }
                    break;
                case INVALUE:
                    switch (ch) {
                        case '\'':
                        case '"':
                            state = STATE.INSTRING;
                            value = "";
                            break;
                        case '}':
                        case ',':
                        case '\n':
                        case '\r':
                            state = STATE.END;
                            break;
                        case ' ': // do nothing
                            break;
                        default:
                            value += ch;
                    }
                    break;
                case INSTRING:
                    switch (ch) {
                        case '\'':
                        case '"':
                            state = STATE.END;
                            break;
                        default:
                            value += ch;
                    }
                default:
            }
            offset++;
        }
        return value;
    }

}
