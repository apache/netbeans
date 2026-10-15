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
package org.netbeans.modules.refactoring.spi.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A reference to a Scope Description to be used by a {@link ScopePanel}
 *
 * @author Ralph Benjamin Ruijs &lt;ralphbenjamin@netbeans.org&gt;
 * @since 1.30
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface ScopeReference {

    /**
     * Into which location one wants to place the reference? Translates to
     * {@link org.openide.filesystems.FileUtil#getConfigFile(java.lang.String)}.
     */
    String path();

    /**
     * Identification of the scope this reference shall point to. 
     * In case one was to create references to scopes defined by someone else, one can
     * specify the id() here.
     */
    String id() default "";
}
