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
import org.netbeans.installer.utils.cli.CLIArgumentsList;
import org.netbeans.installer.utils.helper.ExecutionMode;

/**
 *
 * @author Dmitry Lipin
 */
public class CreateBundleOption extends CLIOptionOneArgument {

    public void execute(CLIArgumentsList arguments) throws CLIOptionException {
        File targetFile = new File(arguments.next()).getAbsoluteFile();
        if (targetFile.exists()) {
            throw new CLIOptionException(ResourceUtils.getString(
                    CreateBundleOption.class,
                    WARNING_BUNDLE_FILE_EXISTS_KEY,
                    CREATE_BUNDLE_ARG,
                    targetFile));
        } else {
            ExecutionMode.setCurrentExecutionMode(
                    ExecutionMode.CREATE_BUNDLE);
            System.setProperty(
                    Registry.CREATE_BUNDLE_PATH_PROPERTY,
                    targetFile.getAbsolutePath());
        }
    }

    public String getName() {
        return CREATE_BUNDLE_ARG;
    }

      @Override
    protected String getLackOfArgumentsMessage() {
        return ResourceUtils.getString(
                CreateBundleOption.class,
                WARNING_BAD_CREATE_BUNDLE_ARG_KEY,
                CREATE_BUNDLE_ARG);
    }
    public static final String CREATE_BUNDLE_ARG =
            "--create-bundle";// NOI18N
    private static final String WARNING_BUNDLE_FILE_EXISTS_KEY =
            "O.warning.bundle.file.exists"; // NOI18N
    private static final String WARNING_BAD_CREATE_BUNDLE_ARG_KEY =
            "O.warning.bad.create.bundle.arg"; // NOI18N
}
