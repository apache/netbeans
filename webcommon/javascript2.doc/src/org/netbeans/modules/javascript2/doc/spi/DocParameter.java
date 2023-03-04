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
package org.netbeans.modules.javascript2.doc.spi;

import java.util.List;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;

/**
 * Stores named and unnamed documentation parameters.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public interface DocParameter {

    /**
     * Gets name of the parameter.
     * @return parameter name, {@code null} if it doesn't have any name
     */
    Identifier getParamName();

    /**
     * Gets default value of the parameter.
     * @return default value, {@code null} if no default value set
     */
    String getDefaultValue();

    /**
     * Get information if the parameter is optional or not.
     * @return flag which is {@code true} if the parameter is optional, {@code false} otherwise
     */
    boolean isOptional();

    /**
     * Gets the description of the parameter.
     * @return parameter description, can be empty string, never {@code null}
     */
    String getParamDescription();

    /**
     * Gets the parameter type.
     * @return parameter possible types, or empty list when no type is set, never {@code null}
     */
    List<Type> getParamTypes();

}
