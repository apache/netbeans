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
package org.netbeans.modules.lsp.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**Register a {@code language-configuration.json} for a given mime type.
 *
 * Will also register language features defined in {@link LanguageFeatures}
 * as appropriate.
 *
 * @since 1.38
 */
@Repeatable(LanguageConfigurationRegistrations.class)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface LanguageConfigurationRegistration {
    /** The mime-type under which the grammar should be registered.
     *
     * @return mime-type
     */
    public String mimeType();

    /** The language configuration to register.
     *
     * @return language configuration
     */
    public String languageConfiguration();

}
