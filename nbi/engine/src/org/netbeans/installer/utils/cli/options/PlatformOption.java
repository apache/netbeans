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

import org.netbeans.installer.utils.cli.*;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.exceptions.CLIOptionException;

/**
 *
 * @author Dmitry Lipin
 */
public class PlatformOption extends CLIOptionOneArgument {

    @Override
    public void execute(CLIArgumentsList arguments) throws CLIOptionException {
        System.setProperty(Registry.TARGET_PLATFORM_PROPERTY, arguments.next());
    }

    @Override
    protected String getLackOfArgumentsMessage() {
        return ResourceUtils.getString(
                PlatformOption.class,
                WARNING_BAD_PLATFORM_ARG_KEY,
                PLATFORM_ARG);
    }

    public String getName() {
        return PLATFORM_ARG;
    }
    public static final String PLATFORM_ARG =
            "--platform";// NOI18N
    private static final String WARNING_BAD_PLATFORM_ARG_KEY =
            "O.warning.bad.platform.arg"; // NOI18N
}
