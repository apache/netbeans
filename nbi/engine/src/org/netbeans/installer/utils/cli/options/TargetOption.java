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
package org.netbeans.installer.utils.cli.options;

import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.exceptions.CLIOptionException;
import org.netbeans.installer.utils.cli.CLIArgumentsList;
import org.netbeans.installer.utils.cli.CLIOptionTwoArguments;

/**
 *
 * @author Dmitry Lipin
 */
public class TargetOption extends CLIOptionTwoArguments {

    public void execute(CLIArgumentsList arguments) throws CLIOptionException {
        final String uid = arguments.next();
        final String version = arguments.next();

        System.setProperty(Registry.TARGET_COMPONENT_UID_PROPERTY, uid);
        System.setProperty(Registry.TARGET_COMPONENT_VERSION_PROPERTY, version);

        LogManager.log(
                "target component:"); // NOI18N
        LogManager.log(
                "... uid:     " + uid); // NOI18N
        LogManager.log(
                "... version: " + version); // NOI18N
    }
    
    @Override
    protected String getLackOfArgumentsMessage() {
        return ResourceUtils.getString(
                TargetOption.class, WARNING_BAD_TARGET_ARG_KEY,
                TARGET_ARG);
    }

    public String getName() {
        return TARGET_ARG;
    }
    public static final String TARGET_ARG =
            "--target";// NOI18N
    private static final String WARNING_BAD_TARGET_ARG_KEY =
            "O.warning.bad.target.arg"; // NOI18N
}
