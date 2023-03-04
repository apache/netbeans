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

package org.netbeans.installer.utils.cli;

import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.CLIOptionException;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class CLIOption {

    protected static final String PARSING_ARGUMENT_STRING =
            "parsing command line parameter \"{0}\"";//NOI18N
    protected static final String UNARY_ARG_VALUE =
            Boolean.toString(true); // NOI18N

    public void init() {
        LogManager.logIndent(StringUtils.format(
                PARSING_ARGUMENT_STRING, getName())); // NOI18N
    }
    public boolean canExecute(String arg) {
        return getName().equalsIgnoreCase(arg);
    }
    public void finish() {
        LogManager.unindent(); // NOI18N
    }

    public void validateOptions(CLIArgumentsList arguments) throws CLIOptionException {
        validateArgumentsNumber(arguments);
    }

    private void validateArgumentsNumber(CLIArgumentsList arguments) throws CLIOptionException {
        final int number = getNumberOfArguments();
        if (number != 0 && (number + arguments.getIndex()) >= arguments.length()) {
            throw new CLIOptionException(getLackOfArgumentsMessage());
        }
    }

    protected String getLackOfArgumentsMessage() {
        return null;
    }

    protected abstract int getNumberOfArguments();

    public abstract String getName();

    public abstract void execute(CLIArgumentsList arguments) throws CLIOptionException;
}
