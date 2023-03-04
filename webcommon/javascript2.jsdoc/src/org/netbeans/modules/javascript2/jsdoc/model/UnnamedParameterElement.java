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

import java.util.List;
import org.netbeans.modules.javascript2.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;

/**
 * Represents parameter element which does not need any parameter name.
 * <p>
 * <i>Examples:</i> @throws {MyError} my description,...
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class UnnamedParameterElement extends ParameterElement implements DocParameter {

    private UnnamedParameterElement(JsDocElementType type,
            List<Type> paramTypes, String paramDescription) {
        super(type, paramTypes, paramDescription);
    }

    /** Creates unnamed parameter element.
     * @param type type of the element
     * @param paramTypes type of the parameter
     * @param paramDescription description of the parameter
     */
    public static UnnamedParameterElement create(JsDocElementType type,
            List<Type> paramTypes, String paramDescription) {
        return new UnnamedParameterElement(type, paramTypes, paramDescription);
    }

    @Override
    public Identifier getParamName() {
        return null;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

}
