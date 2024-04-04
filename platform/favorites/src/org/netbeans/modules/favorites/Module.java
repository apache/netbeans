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
package org.netbeans.modules.favorites;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.swing.UIManager;
import org.netbeans.modules.favorites.api.Favorites;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.OnShowing;

/**
 * For lifecycle tasks.
 * @author mbien
 */
public final class Module {

    private Module() {}

    @OnShowing
    public final static class EDTInit implements Runnable {
        @Override
        public void run() {
            Function<File[], File[]> favAppender = (files) -> {
                if (!UIManager.getBoolean(LFCustoms.FILECHOOSER_FAVORITES_ENABLED)) {
                    return files;
                }
                List<File> shortcuts = new ArrayList<>(Arrays.asList(files));
                for (FileObject favorite : Favorites.getDefault().getFavoriteRoots()) {
                    File file = FileUtil.toFile(favorite);
                    if (file.isDirectory()) {
                        shortcuts.add(file);
                    }
                }
                return shortcuts.toArray(new File[0]);
            };
            UIManager.put(LFCustoms.FILECHOOSER_SHORTCUTS_FILESFUNCTION, favAppender);
        }
    }
    
}
