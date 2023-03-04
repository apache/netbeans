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
package org.netbeans.modules.nativeexecution.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.support.ui.api.FileNamesCompletionProvider;
import org.openide.util.Utilities;

/**
 * Doesn't work on Windows
 * 
 * @author akrasny
 */
public final class SSHKeyFileCompletionProvider extends FileNamesCompletionProvider {

    final boolean isWindows = Utilities.isWindows();

    public SSHKeyFileCompletionProvider() {
        super(ExecutionEnvironmentFactory.getLocal());
    }

    @Override
    protected List<String> listDir(String dir) {
        if (isWindows) {
            return Collections.emptyList();
        }

        File dirFile = new File(dir);

        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return Collections.emptyList();
        }

        final File[] listFiles = dirFile.listFiles(SSHKeyFileFilter.getInstance());

        if (listFiles == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>(listFiles.length);
        int idx = dirFile.getAbsolutePath().length();

        for (File f : listFiles) {
            String fname = f.getAbsolutePath().substring(idx);

            if (fname.charAt(0) == '/') {
                fname = fname.substring(1);
            }

            if (f.isDirectory()) {
                fname = fname.concat("/"); // NOI18N
            }

            result.add(fname);
        }

        return result;
    }
}
