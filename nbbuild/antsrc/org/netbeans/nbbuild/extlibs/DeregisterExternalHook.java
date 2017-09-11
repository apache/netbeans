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

package org.netbeans.nbbuild.extlibs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Issue #141817: remove external.py registration.
 */
public class DeregisterExternalHook extends Task {

    private File root;
    /** Location of NB source root. */
    public void setRoot(File root) {
        this.root = root;
    }

    @Override
    public void execute() throws BuildException {
        new File(root, "nbbuild/antsrc/org/netbeans/nbbuild/extlibs/external.pyc").delete();
        File[] repos = {
            root,
            new File(root, "contrib"),
        };
        for (File repo : repos) {
            File dotHg = new File(repo, ".hg");
            if (!dotHg.isDirectory()) {
                log(repo + " is not a Mercurial repository", Project.MSG_VERBOSE);
                continue;
            }
            try {
                File hgrc = new File(dotHg, "hgrc");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (hgrc.isFile()) {
                    InputStream is = new FileInputStream(hgrc);
                    try {
                        byte[] buf = new byte[4096];
                        int read;
                        while ((read = is.read(buf)) != -1) {
                            baos.write(buf, 0, read);
                        }
                    } finally {
                        is.close();
                    }
                }
                String config = baos.toString();
                String newConfig = config.
                        replaceAll("(?m)^external *=.+\r?\n", "").
                        replaceAll("(?m)^\\*/external/\\*\\.\\{zip,jar,gz,bz2,gem,dll\\} = (up|down)load:.+\r?\n", "").
                        replace("# To preauthenticate, use: https://jhacker:secret@hg.netbeans.org/binaries/upload", "").
                        replaceAll("(^|\r?\n)(\r?\n)*(\\[(extensions|encode|decode)\\](\r?\n)+)+(?=\\[|$)", "$1");
                if (!newConfig.equals(config)) {
                    log("Unregistering external hook from " + hgrc);
                    OutputStream os = new FileOutputStream(hgrc);
                    try {
                        os.write(newConfig.getBytes());
                    } finally {
                        os.close();
                    }
                }
            } catch (IOException x) {
                throw new BuildException(x, getLocation());
            }
        }
    }

}
