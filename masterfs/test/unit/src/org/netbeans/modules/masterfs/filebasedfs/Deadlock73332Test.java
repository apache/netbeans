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

package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj;
import org.netbeans.modules.masterfs.providers.BaseAnnotationProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Radek Matous
 */
public class Deadlock73332Test extends NbTestCase {
    private static FileObject folder;
    static {
        System.setProperty("org.openide.util.Lookup", Deadlock73332Test.TestLookup.class.getName());
        assertTrue(Lookup.getDefault().getClass().getName(),Lookup.getDefault() instanceof Deadlock73332Test.TestLookup);
    }
    
    
    public Deadlock73332Test(String testName) {
        super(testName);
    }
    
    public void testDeadLock() throws Exception {
        assertNotNull(folder);
        assertTrue(folder instanceof BaseFileObj);
        FileObject data = FileUtil.createData(folder, "/a/b/c/data.txt");
        assertNotNull(data);
        FileLock lock = data.lock();
        try {
            data.move(lock,folder, data.getName(), data.getExt());
        } finally {
            lock.releaseLock();
        }
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File f = this.getWorkDir();
        folder = FileUtil.toFileObject(f);
    }
    
    public static class TestLookup extends ProxyLookup {
        public TestLookup() {
            super();
            setLookups(new Lookup[] {getMetaInfLookup()});
        }
        
        private Lookup getMetaInfLookup() {
            return Lookups.metaInfServices(Thread.currentThread().getContextClassLoader());
        }
        
        protected @Override void beforeLookup(Lookup.Template<?> template) {
            if (folder != null && template.getType().isAssignableFrom(BaseAnnotationProvider.class)) {
                RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {
                    public @Override void run() {
                        folder.getChildren(true);
                    }
                });
                task.waitFinished();
            }
        }
    }
    
}
