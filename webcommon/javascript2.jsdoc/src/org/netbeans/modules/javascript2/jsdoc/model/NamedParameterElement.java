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
        List<Type> correctedTypes = new ArrayList<>();
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
