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
import java.util.Locale;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.CLIOptionException;

/**
 *
 * @author Dmitry Lipin
 */
public class LocaleOption extends CLIOptionOneArgument {

    private Locale targetLocale;

    public void execute(CLIArgumentsList arguments) throws CLIOptionException {
        Locale.setDefault(targetLocale);
        LogManager.log(
                "... locale set to: " + targetLocale); // NOI18N

    }

    private void initializeLocale(String value) {
        final String[] valueParts = value.split(StringUtils.UNDERSCORE);
        switch (valueParts.length) {
            case 1:
                targetLocale = new Locale(
                        valueParts[0]);
                break;
            case 2:
                targetLocale = new Locale(
                        valueParts[0],
                        valueParts[1]);
                break;
            case 3:
                targetLocale = new Locale(
                        valueParts[0],
                        valueParts[1],
                        valueParts[2]);
                break;
            default:
                targetLocale = null;
                break;
        }        
    }

    @Override
    public void validateOptions(CLIArgumentsList arguments) throws CLIOptionException {
        super.validateOptions(arguments);
        final String value = arguments.next();

        initializeLocale(value);

        if (targetLocale == null) {
            LogManager.log(
                    "... locale is not set properly, using " + // NOI18N
                    "system default: " + Locale.getDefault()); // NOI18N
            throw new CLIOptionException(ResourceUtils.getString(
                    LocaleOption.class,
                    WARNING_BAD_LOCALE_ARG_PARAM_KEY,
                    LOCALE_ARG,
                    value));
        }

    }

    @Override
    protected String getLackOfArgumentsMessage() {
        return ResourceUtils.getString(
                LocaleOption.class,
                WARNING_BAD_LOCALE_ARG_KEY,
                LOCALE_ARG);
    }

    public String getName() {
        return LOCALE_ARG;
    }
    public static final String LOCALE_ARG =
            "--locale";// NOI18N
    private static final String WARNING_BAD_LOCALE_ARG_PARAM_KEY =
            "O.warning.bad.locale.arg.param"; // NOI18N
    private static final String WARNING_BAD_LOCALE_ARG_KEY =
            "O.warning.bad.locale.arg"; // NOI18N
}
