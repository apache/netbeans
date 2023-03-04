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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.exceptions.CLIOptionException;

/**
 *
 * @author Dmitry Lipin
 */
public class PropertiesOption extends CLIOptionOneArgument {

    @Override
    public void execute(CLIArgumentsList arguments) throws CLIOptionException {
        final File propertiesFile = new File(arguments.next());
        InputStream is = null;
        try {
            is = new FileInputStream(propertiesFile);
            Properties props = new Properties();
            props.load(is);
            System.getProperties().putAll(props);
        } catch (IOException e) {
            LogManager.log(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogManager.log(e);
                }
            }
        }

    }

    @Override
    protected String getLackOfArgumentsMessage() {
        return ResourceUtils.getString(
                PropertiesOption.class,
                WARNING_BAD_PROPERTIES_ARG_KEY,
                PROPERTIES_ARG);
    }

    public String getName() {
        return PROPERTIES_ARG;
    }
    public static final String PROPERTIES_ARG = 
            "--properties";// NOI18N
    private static final String WARNING_BAD_PROPERTIES_ARG_KEY =
            "O.warning.bad.properties.arg"; // NOI18N
}
