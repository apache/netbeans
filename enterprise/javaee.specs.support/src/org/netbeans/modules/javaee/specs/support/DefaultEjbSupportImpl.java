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
package org.netbeans.modules.javaee.specs.support;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.javaee.specs.support.spi.EjbSupportImplementation;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class DefaultEjbSupportImpl implements EjbSupportImplementation {

    public DefaultEjbSupportImpl() {
    }

    @Override
    public boolean isEjb31LiteSupported(J2eePlatform j2eePlatform) {
        Set<Profile> profiles = new HashSet<Profile>(j2eePlatform.getSupportedProfiles());
        profiles.remove(Profile.J2EE_13);
        profiles.remove(Profile.J2EE_14);
        profiles.remove(Profile.JAVA_EE_5);
        // we assume higher specs include it
        return !profiles.isEmpty();
    }

}
