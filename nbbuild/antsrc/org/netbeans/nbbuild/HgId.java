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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Computes Mercurial changeset ID (in short format, i.e. 12 hex digits) of current working copy.
 * Compared to {@code hg id -i} or even {@code hg log -r . --template '{node|short}\n'}
 * it is very quick. Does not check for second merge parent or modification status
 * or tags. Sets property to {@code unknown-revn} if there is any problem.
 */
public class HgId extends Task {

    private File file;
    /**
     * Locates the repository.
     * May be repository root, or any file or folder inside it.
     */
    public void setFile(File file) {
        this.file = file;
    }

    private String property;
    /**
     * Declares which property to define with the resulting ID.
     */
    public void setProperty(String property) {
        this.property = property;
    }

    public @Override void execute() throws BuildException {
        if (file == null || property == null) {
            throw new BuildException("define file and property");
        }
        File dirstate = null;
        for (File root = file; root != null; root = root.getParentFile()) {
            File ds = new File(new File(root, ".hg"), "dirstate");
            if (ds.isFile()) {
                dirstate = ds;
                break;
            }
        }
        String id = "unknown-revn";
        if (dirstate != null && dirstate.length() >= 6) {
            try {
                InputStream is = new FileInputStream(dirstate);
                try {
                    byte[] data = new byte[6];
                    if (is.read(data) < 6) {
                        throw new IOException("truncated read");
                    }
                    id = String.format("%012x", new BigInteger(1, data));
                } finally {
                    is.close();
                }
            } catch (IOException x) {
                log("Could not read " + dirstate + ": " + x, Project.MSG_WARN);
            }
        } else {
            log("No dirstate found starting from " + file, Project.MSG_WARN);
        }
        assert id.length() == 12 : id;
        getProject().setNewProperty(property, id);
    }

}
