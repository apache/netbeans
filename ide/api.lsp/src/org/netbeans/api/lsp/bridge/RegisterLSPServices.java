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
package org.netbeans.api.lsp.bridge;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * For every mime-type specified in this annotation, the implementations of the
 * services defined in this module will be looked up and used to provide
 * the IDE features inside the IDE.
 *
 * @since 1.30
 */
@Target(ElementType.PACKAGE)
public @interface RegisterLSPServices {

    /**
     * The mime-types for which the implementations of this module's services
     * should be used.
     *
     * @return the mime-types
     */
    public String[] mimeTypes();
}
