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

package org.netbeans.modules.php.api.framework.ui.commands;

import java.io.File;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Petr Hejl, Tomas Mysik
 */
public class RefreshPhpModuleRunnable implements Runnable {

    private final PhpModule phpModule;

    public RefreshPhpModuleRunnable(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    @Override
    public void run() {
        refresh(phpModule.getSourceDirectory());
        for (FileObject dir : phpModule.getTestDirectories()) {
            refresh(dir);
        }
    }

    private void refresh(FileObject fo) {
        if (fo == null) {
            return;
        }
        File file = FileUtil.toFile(fo);
        if (file != null) {
            FileUtil.refreshFor(file);
        } else {
            // just defensive fallback
            fo.refresh();
        }
    }
}
