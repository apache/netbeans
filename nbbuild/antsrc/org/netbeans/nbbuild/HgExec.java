/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        return Collections.singletonList("hg");
    }

}
