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
                    try (InputStream is = new FileInputStream(hgrc)) {
                        byte[] buf = new byte[4096];
                        int read;
                        while ((read = is.read(buf)) != -1) {
                            baos.write(buf, 0, read);
                        }
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
                    try (OutputStream os = new FileOutputStream(hgrc)) {
                        os.write(newConfig.getBytes());
                    }
                }
            } catch (IOException x) {
                throw new BuildException(x, getLocation());
            }
        }
    }

}
