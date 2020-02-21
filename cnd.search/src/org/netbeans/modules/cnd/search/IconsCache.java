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
package org.netbeans.modules.cnd.search;

import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * NOT thread safe
 *
 */
public final class IconsCache {

    private static final IconsCache cache = new IconsCache();
    private final static int cacheLifetime = 1000 * 60 * 1; // 1 min
    private final Task cleanUpTask;
    private final HashMap<String, Image> map = new HashMap<String, Image>();
    private final FileObject root = FileUtil.createMemoryFileSystem().getRoot();

    private IconsCache() {
        cleanUpTask = RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        cache.map.clear();
                    }
                });
            }
        }, cacheLifetime);
    }

    public static Image getIcon(String name, int type) {
        cache.cleanUpTask.schedule(cacheLifetime);

        if (name.indexOf('.') < 0) {
            name = "noext"; // NOI18N
        }

        Image icon = cache.map.get(name + type);
        if (icon == null) {
            FileObject fo = createMemoryFile(name);
            try {
                DataObject dob = DataObject.find(fo);
                icon = dob.getNodeDelegate().getIcon(type);
                cache.map.put(name + type, icon);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (fo != null) {
                    try {
                        fo.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return icon;
    }

    private static FileObject createMemoryFile(String name) {
        FileObject fo = null;
        try {
            fo = FileUtil.createData(cache.root, name);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return fo;
    }
}
