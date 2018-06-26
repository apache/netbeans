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
package org.netbeans.modules.javascript2.jsdoc.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.javascript2.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;

/**
 * Represents named parameter element.
 * <p>
 * <i>Examples:</i> @param {MyType} myName myDescription,...
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class NamedParameterElement extends ParameterElement implements DocParameter {

    private final Identifier paramName;
    private final boolean optional;
    private final String defaultValue;

    private NamedParameterElement(JsDocElementType type, Identifier paramName,
            List<Type> paramTypes, String paramDescription,
            boolean optional, String defaultValue) {
        super(type, paramTypes, paramDescription);
        this.paramName = paramName;
        this.optional = optional;
        this.defaultValue = defaultValue;
    }

    /**
     * Creates named parameter element.
     * @param type type of the element
     * @param paramName name of the parameter
     * @param paramTypes type of the parameter
     * @param paramDescription description of the parameter
     * @param optional flag if the parameter is optional
     * @param defaultValue default value of the parameter
     */
    public static NamedParameterElement create(JsDocElementType type, Identifier paramName,
            List<Type> paramTypes, String paramDescription,
            boolean optional, String defaultValue) {
        return new NamedParameterElement(type, paramName, paramTypes, paramDescription, optional, defaultValue);
    }

    /**
     * Creates named parameter element.
     * <p>
     * This creates optional parameter with no default value.
     * @param type type of the element
     * @param paramName name of the parameter
     * @param paramTypes type of the parameter
     * @param paramDescription description of the parameter
     * @param optional flag if the parameter is optional
     */
    public static NamedParameterElement create(JsDocElementType type, Identifier paramName,
            List<Type> paramTypes, String paramDescription,
            boolean optional) {
        return new NamedParameterElement(type, paramName, paramTypes, paramDescription, optional, null);
    }

    /**
     * Creates named parameter element.
     * <p>
     * This creates mandatory parameter with no default value.
     * @param type type of the element
     * @param paramName name of the parameter
     * @param paramTypes type of the parameter
     * @param paramDescription description of the parameter
     */
    public static NamedParameterElement create(JsDocElementType type, Identifier paramName,
            List<Type> paramTypes, String paramDescription) {
        return new NamedParameterElement(type, paramName, paramTypes, paramDescription, false, null);
    }

    /**
     * Creates named parameter element.
     * <p>
     * Also do diagnostics on paramName if the parameter isn't optional and with default value
     * and types, whether use in Google Compiler Syntax.
     * @param type type of the element
     * @param paramName name of the parameter
     * @param paramTypes type of the parameter
     * @param paramDescription description of the parameter
     */
    public static NamedParameterElement createWithDiagnostics(JsDocElementType type, Identifier paramName,
            List<Type> paramTypes, String paramDescription) {
        int nameStartOffset = paramName.getOffsetRange().getStart();
        String name = paramName.getName();
        if (name.indexOf('~') > 0) {
            // we dont't replace tilda if it's on the first position
            name = name.replace('~', '.'); // replacing tilda with dot. See issue #25110
        }
        boolean optional = name.matches("\\[.*\\]"); //NOI18N
        String defaultValue = null;
        List<Type> correctedTypes = new ArrayList<Type>();
        if (optional) {
            nameStartOffset++;
            name = name.substring(1, name.length() - 1);
            int indexOfEqual = name.indexOf("=");
            if (indexOfEqual != -1) {
                defaultValue = name.substring(indexOfEqual + 1);
                name = name.substring(0, indexOfEqual);
            }
            correctedTypes.addAll(paramTypes);
        } else {
            for (Type paramType : paramTypes) {
                boolean changed = false;
                String paramTypeName = paramType.getType();    
                if (paramTypeName.indexOf('~') > 0) {
                    paramTypeName = paramTypeName.replace('~', '.');
                    changed = true;
                }
                if (JsDocElementUtils.GoogleCompilerSytax.canBeThisSyntax(paramType.getType())) {
                    if (JsDocElementUtils.GoogleCompilerSytax.isMarkedAsOptional(paramTypeName)) {
                        optional = true;
                        changed = true;
                        paramTypeName = JsDocElementUtils.GoogleCompilerSytax.removeSyntax(paramTypeName);
                    } 
                } 
                if (changed) {
                    correctedTypes.add(JsDocElementUtils.createTypeUsage(paramTypeName, paramType.getOffset()));
                } else {
                    correctedTypes.add(paramType);
                }
            }
        }
        return new NamedParameterElement(type, new Identifier(name, nameStartOffset), correctedTypes,
                paramDescription, optional, defaultValue);
    }

    /**
     * Gets name of the parameter.
     * @return parameter name
     */
    @Override
    public Identifier getParamName() {
        return paramName;
    }

    /**
     * Gets default value of the parameter.
     * @return default value
     */
    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Get information if the parameter is optional or not.
     * @return flag which is {@code true} if the parameter is optional, {@code false} otherwise
     */
    @Override
    public boolean isOptional() {
        return optional;
    }

}
