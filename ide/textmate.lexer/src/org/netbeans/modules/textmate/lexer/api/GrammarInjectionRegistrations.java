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
package org.netbeans.modules.textmate.lexer.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/** Register multiple grammar injections.
 *
 * @since 1.3
 */
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface GrammarInjectionRegistrations {

    /**The grammar injection registration to delegate to.
     *
     * @return grammar injection registrations
     */
    public GrammarInjectionRegistration[] value();

}
