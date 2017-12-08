/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.richexecution.program;

import org.netbeans.lib.richexecution.*;
import java.util.StringTokenizer;

/**
 * Description of a command to be run under a shell.
 * <p>
 * For example:
 * <pre>
 * Program printit = new Command("/bin/cat /etc/termcap");
 * </pre>
 * <p>
 * Use {@link PtyExecutor} or subclasses thereof to run the program.
 * @author ivan
 */
public final class Command extends Shell {
    private final String name;

    public Command(String command) {
        StringTokenizer st = new StringTokenizer(command);
        String cmdName = st.nextToken();
        name = basename(cmdName);

	if (OS.get() == OS.WINDOWS)
	    add("/c");
	else
	    add("-c");
        add(command);
    }

    /**
     * Return basename of the first word of the command.
     * @return basename of the first word of the command.
     */
    @Override
    public String name() {
        return name;
    }
}
