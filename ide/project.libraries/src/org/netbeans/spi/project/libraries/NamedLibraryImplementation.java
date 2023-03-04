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
package org.netbeans.spi.project.libraries;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * LibraryImplementation extension allowing setting/getting display name.
 * @author Tomas Zezula
 * @since org.netbeans.modules.project.libraries/1 1.31
 */
public interface NamedLibraryImplementation extends LibraryImplementation {

    /**
     * Name of displayName property.
     */
    String PROP_DISPLAY_NAME = "displayName";   //NOI18N

    /**
     * Sets the display name.
     * @param displayName the new value of the displayName.
     * If null resets the display name to the value provided
     * by the localizing bundle or the identifying name.
     */
    void setDisplayName(@NullAllowed String displayName);

    /**
     * Returns the display name if available or null.
     * @return the display name
     */
    @CheckForNull String getDisplayName();
}
