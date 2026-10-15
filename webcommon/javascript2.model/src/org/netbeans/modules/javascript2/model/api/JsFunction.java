/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.model.api;

import java.util.Collection;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 *
 * @author Petr Pisl
 */
public interface JsFunction extends JsObject, DeclarationScope {
    public Collection<? extends JsObject> getParameters();
    public JsObject getParameter(String name);
    public void addReturnType(TypeUsage type);
    /**
     *
     * @return collection of possible types that can be returned by the function.
     * It can return {@link Type.UNRESOLVED} if the type can not be resolved. The function
     * also can return {@link Type.UNDEFINED} if there is no return statement.
     */
    public Collection<? extends TypeUsage> getReturnTypes();
}
