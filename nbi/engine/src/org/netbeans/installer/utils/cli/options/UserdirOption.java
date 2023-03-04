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

import org.netbeans.installer.utils.cli.CLIOptionOneArgument;
import java.io.File;
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.cli.CLIArgumentsList;
import org.netbeans.installer.utils.exceptions.CLIOptionException;

/**
 *
 * @author Dmitry Lipin
 */
public class UserdirOption extends CLIOptionOneArgument {

    @Override
    public void execute(CLIArgumentsList arguments) throws CLIOptionException {
        System.setProperty(Installer.LOCAL_DIRECTORY_PATH_PROPERTY,
                new File(arguments.next()).getAbsolutePath());
    }

    public String getName() {
        return USERDIR_ARG;
    }

    
    @Override
    protected String getLackOfArgumentsMessage() {
        return ResourceUtils.getString(
                UserdirOption.class,
                WARNING_BAD_USERDIR_ARG_KEY,
                USERDIR_ARG);
    }
    public static final String USERDIR_ARG = 
            "--userdir";// NOI18N
    private static final String WARNING_BAD_USERDIR_ARG_KEY =
            "O.warning.bad.userdir.arg"; // NOI18N
}
