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
    private boolean ignoreRecursiveListener;

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
            if (arr[4] instanceof BaseFileObj) {
                preferrable = (BaseFileObj)arr[4];
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActionEvent) {
            this.ref = (ActionEvent)obj;
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
