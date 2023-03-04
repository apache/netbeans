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

package org.netbeans.spi.editor.mimelookup;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.api.editor.mimelookup.MimeRegistration;

/**Should be used for services that are to be registered to the MimeLookup and that
 * need a specific subfolder to be used for the registration (optional, mime-type root
 * will be searched if this annotation is missing)
 *
 * @author Jan Lahoda
 * @since 1.19
 * @see MimeRegistration
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MimeLocation {

    /**
     * Folder under which the services should be registered in the system filesystem.
     * The full path for registering the services will then be <code>Editors/&lt;mime-type&gt;/&lt;subfolderName&gt;</code>
     */
    public String subfolderName();

    @SuppressWarnings("rawtypes")
    public Class<? extends InstanceProvider> instanceProviderClass() default InstanceProvider.class;
    
}
