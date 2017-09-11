/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.richexecution.program;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description of a program to be executed.
 * Similar to {@link java.lang.ProcessBuilder} except for the lack of start().
 * Use {@link PtyExecutor} or subclasses thereof to run the program.
 * @author ivan
 */
public class Program {
    // Use ProcessBuilder strictly as a convenience to hold state
    private final ProcessBuilder processBuilder;
    private List<String> command;

    public Program() {
        processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        command = new ArrayList<String>();
    }

    /**
     * After {@link ProcessBuilder#ProcessBuilder(java.util.List)}.
     */
    public Program(List<String> command) {
        if (command == null)
            throw new NullPointerException();
        this.command = command;
        processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
    }

    /**
     * After {@link ProcessBuilder#ProcessBuilder(java.lang.String[])}.
     */
    public Program(String... command) {
        processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        command(command);
    }

    public void add(String arg) {
        command.add(arg);
    }

    protected String basename(String name) {
        File nameFile = new File(name);
        return nameFile.getName();
    }

    public List<String> command() {
        return command;
    }

    public void command(List<String> command) {
        if (command == null) {
            throw new NullPointerException();
        }
        this.command = command;
    }

    public void command(String... command) {
        this.command = new ArrayList<String>(command.length);
        for (String arg : command) {
            this.command.add(arg);
        }
    }

    public File directory() {
        return processBuilder.directory();
    }

    public void directory(File directory) {
        processBuilder.directory(directory);
    }

    public Map<String, String> environment() {
        return processBuilder.environment();
    }

    public String name() {
        if (command.size() == 0) {
            throw new IllegalStateException("No arguments assigned yet");
        }
        return command.get(0);
    }
}
