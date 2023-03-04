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
public class RecordOption extends CLIOptionOneArgument {

    @Override
    public void execute(CLIArgumentsList arguments) throws CLIOptionException {
        File stateFile = new File(arguments.next()).getAbsoluteFile();
        if (stateFile.exists()) {
            throw new CLIOptionException(ResourceUtils.getString(
                    RecordOption.class,
                    WARNING_TARGET_STATE_FILE_EXISTS_KEY,
                    RECORD_ARG,
                    stateFile));
        } else {
            System.setProperty(
                    Registry.TARGET_STATE_FILE_PATH_PROPERTY,
                    stateFile.getAbsolutePath());
        }


    }

    @Override
    protected String getLackOfArgumentsMessage() {
        return ResourceUtils.getString(
                RecordOption.class,
                WARNING_BAD_TARGET_STATE_FILE_ARG_KEY,
                RECORD_ARG);
    }

    public String getName() {
        return RECORD_ARG;
    }
    public static final String RECORD_ARG =
            "--record";// NOI18N
    private static final String WARNING_TARGET_STATE_FILE_EXISTS_KEY =
            "O.warning.target.state.file.exists"; // NOI18N
    private static final String WARNING_BAD_TARGET_STATE_FILE_ARG_KEY =
            "O.warning.bad.target.state.file.arg"; // NOI18N
}
