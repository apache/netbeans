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
package org.netbeans.libs.git.jgit;

import java.util.stream.Collectors;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.openide.modules.ModuleInstall;

import static org.apache.sshd.common.util.security.SecurityUtils.DEFAULT_SECURITY_PROVIDER_REGISTRARS;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        // Override the security providers Apache Mina. EdDSASecurityProviderRegistrar
        // uses net.i2p.crypto.eddsa to provide support for ed25519, that is not
        // needed, as BouncyCastle also has support for the curve. As long as the
        // security provider is activated Mina will try to load it and fail fatally
        // if that fails.
        System.setProperty(
                SecurityUtils.SECURITY_PROVIDER_REGISTRARS,
                DEFAULT_SECURITY_PROVIDER_REGISTRARS
                        .stream()
                        .filter(e -> ! e.endsWith("EdDSASecurityProviderRegistrar"))
                        .collect(Collectors.joining(","))
        );
        super.restored();
    }

}
