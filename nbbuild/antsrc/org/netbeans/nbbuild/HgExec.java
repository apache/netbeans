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

package org.netbeans.nbbuild;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Commandline;

/**
 * Just like {@code <exec>} but fixes the executable as {@code hg} (Mercurial).
 * The advantage is that it also checks for variants like {@code hg.cmd} etc.
 * See issue #134636.
 */
public class HgExec extends ExecTask {

    public HgExec() {
        List<String> cmd = hgExecutable();
        super.setExecutable(cmd.get(0));
        for (String arg : cmd.subList(1, cmd.size())) {
            createArg().setValue(arg);
        }
    }

    @Override
    public void setCommand(Commandline cmdl) {
        throw new BuildException("Cannot call this");
    }

    @Override
    public void setExecutable(String value) {
        throw new BuildException("Cannot call this");
    }

    /**
     * Get a command to run Mercurial.
     * For Windows users, {@link Runtime#exec} does not work directly on {@code *.bat} / {@code *.cmd} files.
     * Find what the desired Hg executable form is in the path and call it appropriately.
     */
    public static List<String> hgExecutable() {
        String path = System.getenv("Path");
        if (path != null) {
            for (String component : path.split(File.pathSeparator)) {
                if (new File(component, "hg.bat").isFile() || new File(component, "hg.cmd").isFile()) {
                    return Arrays.asList("cmd", "/c", "hg");
                } else if (new File(component, "hg.exe").isFile() || new File(component, "hg").isFile()) {
                    return Collections.singletonList("hg");
                }
            }
        }
        return Collections.singletonList("git");
    }

}
