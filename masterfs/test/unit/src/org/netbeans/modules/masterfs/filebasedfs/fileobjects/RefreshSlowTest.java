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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.watcher.Watcher;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class RefreshSlowTest extends  NbTestCase {
    public RefreshSlowTest(String n) {
        super(n);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    public void testByDefaultTheRefreshIgnoresRecListeners() throws IOException {
        Logger LOG = Logger.getLogger("test." + getName());
        if (!Watcher.isEnabled()) {
            LOG.warning("Have to skip the test, as native watching is disabled");
            LOG.log(Level.WARNING, "os.name: {0} os.version: {1} os.arch: {2}", new Object[] {
                    System.getProperty("os.name"), 
                    System.getProperty("os.version"), 
                    System.getProperty("os.arch")
            });
            return;
        }
        
        File d = new File(new File(getWorkDir(), "dir"), "subdir");
        d.mkdirs();
        
        FileChangeAdapter ad = new FileChangeAdapter();
        
        FileUtil.addRecursiveListener(ad, getWorkDir());

        final FileObject fo = FileUtil.toFileObject(getWorkDir());
        Runnable r = (Runnable) fo.getFileSystem().getRoot().getAttribute("refreshSlow");
        final int cnt[] = { 0 };
        ActionEvent ae = new ActionEvent(this, 0, "") {
            @Override
            public void setSource(Object newSource) {
                assertTrue(newSource instanceof Object[]);
                Object[] arr = (Object[]) newSource;
                assertTrue("Three elements at least ", 3 <= arr.length);
                assertTrue("3rd is fileobject", arr[2] instanceof FileObject);
                FileObject checked = (FileObject) arr[2];
                assertFalse(checked + " shall not be a children of " + fo, FileUtil.isParentOf(fo, checked));
                super.setSource(newSource);
                cnt[0]++;
                fail("" + checked +"\n" + fo);
            }
        };
        
        r.equals(ae);
        r.run();
        
        assertEquals("No calls to refresh", 0, cnt[0]);
    }

}