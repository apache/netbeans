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

import org.netbeans.lib.richexecution.OS;
import org.netbeans.lib.richexecution.PtyExecutor;

/**
 * Description of a shell to be started under a pty.
 * <br>
 * On unix ...
 * <br>
 * The shell defined by <code>$SHELL</code> is started.
 * If <code>$SHELL</code> is empty <code>/bin/bash</code> is started.
 * <br>
 * On windows ...
 * <br>
 * <code>cmd.exe</code> is started.
 * <p>
 * Use {@link PtyExecutor} or subclasses thereof to run the program.
 * @author ivan
 */
public class Shell extends Program {

    private final static OS os = OS.get();
    private final String name;

    private static void error(String fmt, Object...args) {
        String msg = String.format(fmt, args);
        throw new IllegalStateException(msg);
    }


    public Shell() {
        String shell = System.getenv("SHELL");

        if (shell == null)
            shell = "/bin/bash";

        switch (os) {
            case WINDOWS:
                add("cmd.exe");
                add("/q");  // turn echo off
                add("/a");  // use ANSI
                name = "cmd.exe";
                break;

            case LINUX:
//		add("/usr/bin/strace");
//		add("-o");
//		add("/tmp/rich-cmd.tr");
                add(shell);
                name = basename(shell);
                break;
            case SOLARIS:
//		add("/usr/bin/truss");
//		add("-o");
//		add("/tmp/rich-cmd.tr");
                add(shell);
                name = basename(shell);
                break;
            case MACOS:
                add(shell);
                name = basename(shell);
                break;
            default:
                error("Unsupported os '%s'", os);
                name = "";
                break;
	}
    }

    /**
     * Return the basename of the shell being run.
     * @return the basename of the shell being run.
     */
    @Override
    public String name() {
        return name;
    }
}
