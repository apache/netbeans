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

package org.netbeans.modules.payara.common;

import java.io.File;
import java.util.Optional;
import org.netbeans.modules.payara.common.wizards.ServerWizardIterator;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersion;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersionAPI;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.openide.WizardDescriptor;

/**
 *
 * @author vkraemer
 * @author Gaurav Gupta
 */
public class PayaraPlatformDetails {

    /**
     * Determine if the glassfishDir holds a valid install of this release of
     * Payara Server.
     *
     * @param payaraDir
     * @param version
     * @return true if the glassfishDir holds this particular server version.
     */
    public static boolean isInstalledInDirectory(PayaraPlatformVersionAPI version, File payaraDir) {
        Optional<PayaraPlatformVersionAPI> serverDetails = getVersionFromInstallDirectory(payaraDir);
        return serverDetails.isPresent() && serverDetails.get().equals(version);
    }

    /**
     * Creates an iterator for a wizard to instantiate server objects.
     * <p/>
     * @return Server wizard iterator initialized with supported Payara
     * server versions.
     */
    public static WizardDescriptor.InstantiatingIterator getInstantiatingIterator() {
        return new ServerWizardIterator(
                PayaraPlatformVersion.getVersions()
        );
    }

    /**
     * Determine the version of the Payara Server installed in a directory
     * @param payaraDir the directory that holds a Payara installation
     * @return -1 if the directory is not a Payara server install
     */
    public static Optional<PayaraPlatformVersionAPI> getVersionFromInstallDirectory(File payaraDir) {
        Optional<PayaraPlatformVersionAPI> serverDetails = Optional.empty();
        if (payaraDir == null) {
            return serverDetails;
        }
        return Optional.ofNullable(ServerUtils.getPlatformVersion(payaraDir.getAbsolutePath()));
    }

}
