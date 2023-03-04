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
import java.util.Arrays;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.CLIOptionException;

/**
 *
 * @author Dmitry Lipin
 */
public class RegistryOption extends CLIOptionOneArgument {

    @Override
    public void execute(CLIArgumentsList arguments) throws CLIOptionException {

        final String value = arguments.next();

        final String existing = System.getProperty(
                Registry.REMOTE_PRODUCT_REGISTRIES_PROPERTY);

        if (existing == null) {
            System.setProperty(
                    Registry.REMOTE_PRODUCT_REGISTRIES_PROPERTY,
                    value);
        } else {
            if (!Arrays.asList(
                    existing.split(StringUtils.LF)).contains(value)) {
                System.setProperty(
                        Registry.REMOTE_PRODUCT_REGISTRIES_PROPERTY,
                        existing + StringUtils.LF + value);
            }
        }

    }

    @Override
    protected String getLackOfArgumentsMessage() {
        return ResourceUtils.getString(
                RecordOption.class,
                WARNING_BAD_REGISTRY_ARG_KEY,
                REGISTRY_ARG);
    }

    public String getName() {
        return REGISTRY_ARG;
    }
    public static final String REGISTRY_ARG = 
            "--registry";// NOI18N
    private static final String WARNING_BAD_REGISTRY_ARG_KEY =
            "O.warning.bad.registry.arg"; // NOI18N
}
