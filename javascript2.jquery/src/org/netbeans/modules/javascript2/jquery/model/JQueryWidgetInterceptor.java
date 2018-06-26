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
package org.netbeans.modules.javascript2.jquery.model;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Pisl
 */
@FunctionInterceptor.Registration(priority = 50)
public class JQueryWidgetInterceptor implements FunctionInterceptor {

    private final static Pattern PATTERN = Pattern.compile("(\\$|jQuery)\\.widget");  //NOI18N

    @Override
    public Pattern getNamePattern() {
        return PATTERN;
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String name, JsObject globalObject, DeclarationScope scope, ModelElementFactory factory, Collection<FunctionArgument> args) {
        String widgetName = null;
        int widgetNameOffset = -1;
        JsObject widget = null;
        for (FunctionArgument arg : args) {
             if (arg.getKind() == FunctionArgument.Kind.STRING) {
                 widgetName = (String)arg.getValue();
                 widgetNameOffset = arg.getOffset();
             } else if (arg.getKind() == FunctionArgument.Kind.ANONYMOUS_OBJECT) {
                 widget = (JsObject)arg.getValue();
             }
        }
        if (widgetName != null && widget != null) {
            String[] parts = widgetName.split("\\.");   //NOI18N
            JsObject parent = globalObject;
            JsObject newObject;
            for (int i = 0; i < parts.length - 1; i++) {
                newObject = factory.newObject(parent, parts[i], new OffsetRange(widgetNameOffset, widgetNameOffset + parts[i].length()), true);
                widgetNameOffset = widgetNameOffset + parts[i].length() + 1;
                parent.addProperty(parts[i], newObject);
                parent = newObject;
            }
            newObject = factory.newReference(parent, parts[parts.length - 1], new OffsetRange(widgetNameOffset, widgetNameOffset + parts[parts.length - 1].length()), widget, true, null);
            parent.addProperty(parts[parts.length - 1], newObject);
        }
        return Collections.emptyList();
    }

}
