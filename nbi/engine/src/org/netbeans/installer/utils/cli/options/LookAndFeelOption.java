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
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.exceptions.CLIOptionException;

/**
 *
 * @author Dmitry Lipin
 */
public class LookAndFeelOption extends CLIOptionOneArgument {

    public void execute(CLIArgumentsList arguments) throws CLIOptionException {
        final String value = arguments.next();
        System.setProperty(
                UiUtils.LAF_CLASS_NAME_PROPERTY,
                value);

        LogManager.log(
                "... class name: " + value); // NOI18N
    }
 
    @Override
    protected String getLackOfArgumentsMessage() {
        return ResourceUtils.getString(
                LookAndFeelOption.class,
                WARNING_BAD_LOOK_AND_FEEL_ARG_KEY,
                LOOK_AND_FEEL_ARG);
    }

    public String getName() {
        return LOOK_AND_FEEL_ARG;
    }
    private static final String WARNING_BAD_LOOK_AND_FEEL_ARG_KEY =
            "O.warning.bad.look.and.feel.arg"; // NOI18N
    public static final String LOOK_AND_FEEL_ARG =
            "--look-and-feel";// NOI18N
}
