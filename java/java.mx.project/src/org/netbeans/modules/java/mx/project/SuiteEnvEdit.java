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
package org.netbeans.modules.java.mx.project;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Openable;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "# {0} - file",
    "MSG_CannotGenerate=Cannot generate {0} file"
})
final class SuiteEnvEdit implements Editable {
    private static final String HINT_CONTENT = 
        "# Uncomment # and specify primary JDK path or even extra JDK paths\n" +
        "# JAVA_HOME=path\n" +
        "# EXTRA_JAVA_HOMES=path1:path2:path3\n" +
        "";
    private final SuiteProject suite;

    SuiteEnvEdit(SuiteProject suite) {
        this.suite = suite;
    }

    @Override
    public void edit() {
        open(suite.getSuitePy(), null);
        boolean globalOpened = open(suite.getGlobalEnv(), null);
        boolean localOpened = open(suite.getSuiteEnv(), null);
        if (globalOpened || localOpened) {
            return;
        }
        open(suite.getGlobalEnv(), HINT_CONTENT);
    }

    private static boolean open(FileObject fo, String generate) {
        if (fo == null) {
            return false;
        }
        if (!fo.isValid()) {
            if (generate != null) {
                try (OutputStream os = fo.getOutputStream()) {
                    os.write(generate.getBytes(StandardCharsets.UTF_8));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(Exceptions.attachLocalizedMessage(ex, Bundle.MSG_CannotGenerate(fo)));
                    return false;
                }
                fo = fo.getParent().getFileObject(fo.getNameExt());
            }
        }
        Openable o = fo.getLookup().lookup(Openable.class);
        if (o != null) {
            o.open();
            return true;
        }
        Editable e = fo.getLookup().lookup(Editable.class);
        if (e != null) {
            e.edit();
            return true;
        }
        return false;
    }
}
