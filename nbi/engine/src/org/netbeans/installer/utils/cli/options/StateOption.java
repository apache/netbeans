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
import java.io.File;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.exceptions.CLIOptionException;

/**
 *
 * @author Dmitry Lipin
 */
public class StateOption extends CLIOptionOneArgument {

    @Override
    public void execute(CLIArgumentsList arguments) throws CLIOptionException {
        File stateFile = new File(arguments.next()).getAbsoluteFile();
        if (!stateFile.exists()) {
            throw new CLIOptionException(ResourceUtils.getString(
                    StateOption.class,
                    WARNING_MISSING_STATE_FILE_KEY,
                    STATE_ARG,
                    stateFile));
        } else {
            System.setProperty(
                    Registry.SOURCE_STATE_FILE_PATH_PROPERTY,
                    stateFile.getAbsolutePath());
        }

    }

    @Override
    protected String getLackOfArgumentsMessage() {
        return ResourceUtils.getString(
                StateOption.class,
                WARNING_BAD_STATE_FILE_ARG_KEY,
                STATE_ARG);
    }

    public String getName() {
        return STATE_ARG;
    }
    private static final String WARNING_MISSING_STATE_FILE_KEY =
            "O.warning.missing.state.file"; // NOI18N
    private static final String WARNING_BAD_STATE_FILE_ARG_KEY =
            "O.warning.bag.state.file.arg"; // NOI18N
    public static final String STATE_ARG = 
            "--state";// NOI18N
}

