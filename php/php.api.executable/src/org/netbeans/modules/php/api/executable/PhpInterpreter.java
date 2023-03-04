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

package org.netbeans.modules.php.api.executable;

import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.modules.php.api.PhpOptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * PHP interpreter as it is specified in Tools > Options > PHP.
 */
public final class PhpInterpreter {

    public static final Pattern[] LINE_PATTERNS = {
        Pattern.compile(".+\\s+in\\s+(.+)\\s+on\\s+line\\s+(\\d+)"), // NOI18N
        Pattern.compile(".+\\(\\)\\s+(.+):(\\d+)"), // NOI18N
    };

    private final PhpExecutable executable;


    PhpInterpreter(String command) {
        this.executable = new PhpExecutable(command);
    }

    /**
     * Get the {@link PhpOptions#getPhpInterpreter() default}, <b>valid only</b> PHP interpreter.
     * @return the {@link PhpOptions#getPhpInterpreter() default}, <b>valid only</b> PHP interpreter
     * @throws InvalidPhpExecutableException if PHP interpreter is not valid
     *         The reason can be found in the {@link InvalidPhpExecutableException#getLocalizedMessage exception's message}.
     */
    public static PhpInterpreter getDefault() throws InvalidPhpExecutableException {
        return getCustom(Lookup.getDefault().lookup(PhpOptions.class).getPhpInterpreter());
    }

    /**
     * Get the custom, <b>valid only</b> PHP interpreter. This method is used by projects
     * that have their own PHP interpreter specified (using Run Script configuration).
     * @param command command which represents path to PHP interpreter (arguments allowed)
     * @return the custom, <b>valid only</b> PHP interpreter
     * @throws InvalidPhpExecutableException if PHP interpreter is not valid.
     *         The reason can be found in the {@link InvalidPhpExecutableException#getLocalizedMessage exception's message}.
     */
    @NbBundle.Messages("PhpInterpreter.name=PHP interpreter")
    public static PhpInterpreter getCustom(String command) throws InvalidPhpExecutableException {
        String error = PhpExecutableValidator.validateCommand(command, Bundle.PhpInterpreter_name());
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new PhpInterpreter(command);
    }

    /**
     * Get the interpreter itself.
     * @return the interpreter itself
     */
    public String getInterpreter() {
        return executable.getExecutable();
    }

    /**
     * Get parameters.
     * @return parameters
     */
    public List<String> getParameters() {
        return executable.getParameters();
    }

}
