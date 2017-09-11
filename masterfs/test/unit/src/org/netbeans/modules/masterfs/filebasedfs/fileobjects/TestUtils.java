/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.watcher.Watcher;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

public class TestUtils {
    private static final Logger LOG = Logger.getLogger(TestUtils.class.getName());
    
    public static String getFileObjectPath (File f) {
        return f.getAbsolutePath().replace('\\','/');//NOI18N
    }

    public static void gcAll() {
        LOG.info("doing gcAll");
        List<Reference<Object>> refs = new ArrayList<Reference<Object>>();
        for (FileObjectFactory fbs : FileObjectFactory.getInstances()) {
            fbs.allIBaseLock.readLock().lock();
            try {
                for (Object obj : fbs.allIBaseFileObjects.values()) {
                    if (obj instanceof Reference<?>) {
                        refs.add((Reference<Object>) obj);
                    } else {
                        refs.addAll((List<Reference<Object>>) obj);
                    }
                }
            } finally {
                fbs.allIBaseLock.readLock().unlock();
            }
        }

        for (Reference<Object> ref : refs) {
            Object obj = ref.get();
            String s = obj == null ? "null" : obj.toString();
            obj = null;
            try {
                NbTestCase.assertGC("GCing " + s, ref);
                LOG.log(Level.INFO, "GCed {0}", s);
            } catch (AssertionFailedError afe) {
                LOG.log(Level.INFO, "Not GCed {0}", s);
            }
        }
        LOG.info("done gcAll");
    }

    public static void logAll() {
        LOG.info("all existing file objects");
        List<Reference<Object>> refs = new ArrayList<Reference<Object>>();
        for (FileObjectFactory fbs : FileObjectFactory.getInstances()) {
            fbs.allIBaseLock.readLock().lock();
            try {
                for (Object obj : fbs.allIBaseFileObjects.values()) {
                    if (obj instanceof Reference<?>) {
                        refs.add((Reference<Object>) obj);
                    } else {
                        refs.addAll((List<Reference<Object>>) obj);
                    }
                }
            } finally {
                fbs.allIBaseLock.readLock().unlock();
            }
        }

        for (Reference<Object> ref : refs) {
            Object obj = ref.get();
            if (obj != null) {
                LOG.log(Level.INFO, "Existing {0}", obj);
            }
        }
        LOG.info("end of file objects");
    }

    public static Runnable findSlowRefresh(FileObject fo) throws FileStateInvalidException {
        Object r = fo.getFileSystem().getRoot().getAttribute("refreshSlow");
        Assert.assertNotNull("Runnable for refreshSlow found", r);
        Assert.assertEquals("Right class", RefreshSlow.class, r.getClass());
        RefreshSlow rs = (RefreshSlow)r;
        Assert.assertTrue("Can only be used when proper property is set", Boolean.getBoolean("org.netbeans.modules.masterfs.watcher.disable"));
        Assert.assertFalse("Watcher is really disabled", Watcher.isEnabled());
        return rs;
    }
}
