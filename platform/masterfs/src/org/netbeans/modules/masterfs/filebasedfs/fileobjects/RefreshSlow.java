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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.watcher.Watcher;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

final class RefreshSlow extends AtomicBoolean implements Runnable {
    private ActionEvent ref;
    private BaseFileObj preferrable;
    private int size;
    private int index;
    private final boolean ignoreRecursiveListener;

    public RefreshSlow() {
        super();
        set(true);
        ignoreRecursiveListener = Watcher.isEnabled();
    }

    @Override
    public void run() {
        RootObj.invokeRefreshFor(this, File.listRoots(), ignoreRecursiveListener);
    }

    boolean refreshFileObject(final BaseFileObj fo, final boolean expected, final int add) {
        final boolean[] b = { true };
        ActionEvent r = this.ref;
        final Runnable goingIdle = r instanceof Runnable ? (Runnable) r : null;
        Runnable refresh = new Runnable() {
            boolean second;
            @Override
            public void run() {
                if (second) {
                    before();
                    fo.refresh(expected);
                    if (!after()) {
                        b[0] = false;
                        return;
                    }
                } else {
                    second = true;
                    FileChangedManager.idleIO(50, this, goingIdle, RefreshSlow.this);
                }
            }
        };
        FileUtil.runAtomicAction(refresh);
        if (b[0]) {
            progress(add, fo);
        }
        return b[0];
    }

    void progress(int add, FileObject obj) {
        index += add;
        if (ref != null) {
            final Object[] arr = new Object[]{index, size, obj, this, null};
            if (preferrable != null) {
                arr[4] = preferrable.getExistingParent();
            }
            ref.setSource(arr);
            if (arr[4] instanceof BaseFileObj baseFileObj) {
                preferrable = baseFileObj;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActionEvent actionEvent) {
            this.ref = actionEvent;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    void before() {
    }

    boolean after() {
        try {
            FileChangedManager.waitIOLoadLowerThan(50);
            return true;
        } catch (InterruptedException ex) {
            return false;
        }
    }

    BaseFileObj preferrable() {
        return preferrable;
    }

    void estimate(int cnt) {
        this.size = cnt;
    }
}
