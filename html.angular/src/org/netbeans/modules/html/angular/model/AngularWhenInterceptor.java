/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

    private final static Pattern PATTERN = Pattern.compile("(.)*\\.(when|state)");  //NOI18N
    public final static String TEMPLATE_URL_PROP = "templateUrl";  //NOI18N
    public final static String CONTROLLER_PROP = "controller";     //NOI18N
    public final static String CONTROLLER_AS_PROP = "controllerAs"; //NOI18N

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
