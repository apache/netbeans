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
package org.netbeans.modules.java.j2seplatform.spi;

import java.net.URI;
import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;

/**
 * Ability to provide default Javadoc for J2SE Platform.
 * The instances registered in the "org-netbeans-api-java/platform/j2seplatform/defaultJavadocProviders"
 * folder are used to provide default Javadoc for J2SE Platforms which have no
 * Javadoc attached by an user. The {@link J2SEPlatformDefaultJavadoc} is also consulted
 * by the new java platform wizard when a new J2SE Platform is created to suggest
 * a Javadoc to a newly created platform.
 * @author Tomas Zezula
 * @since 1.32
 */
public interface J2SEPlatformDefaultJavadoc {
    /**
     * Returns a collection of suggested javadoc roots for given platform.
     * @param platform the platform to suggest javadoc for.
     * @return the suggested javadoc.
     */
    @NonNull
    Collection<URI> getDefaultJavadoc(@NonNull JavaPlatform platform);
}
