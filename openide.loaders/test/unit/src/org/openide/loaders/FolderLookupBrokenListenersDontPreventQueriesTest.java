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

package org.openide.loaders;

import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public class FolderLookupBrokenListenersDontPreventQueriesTest extends NbTestCase implements LookupListener {
    private Lookup.Result<?> res;
    private FileObject fo;
    private int listenerVisited;

    public FolderLookupBrokenListenersDontPreventQueriesTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return null;// Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        
        fo = FileUtil.createFolder(FileUtil.getConfigRoot(), getName());
    }

    @Override
    protected void tearDown() throws Exception {
        synchronized (this) {
            // free the locked threads
            notifyAll();
        }
    }

    public void resultChanged(LookupEvent ev) {
        amIBrokenYesYouAre();
    }

    @RandomlyFails // NB-Core-Build #2892
    public void testIssue163315() throws Exception {
        FileObject ioe = FileUtil.createData(fo, "java-io-IOException.instance");
        FileObject iae = FileUtil.createData(fo, "java-lang-IllegalArgumentException.instance");

        @SuppressWarnings("deprecation")
        Lookup lkp = new FolderLookup(DataFolder.findFolder(fo)).getLookup();
        res = lkp.lookupResult(Exception.class);
        assertEquals("Two items found", 2, res.allInstances().size());
        res.addLookupListener(this);

        FileObject ise = FileUtil.createData(fo, "java-lang-IllegalStateException.instance");
        assertEquals("Three now", 3, res.allInstances().size());

        FileObject npe = FileUtil.createData(fo, "java-lang-NullPointerException.instance");
        assertEquals("Four now", 4, res.allInstances().size());

        if (listenerVisited == 0) {
            fail("Listener shall be notified at least once, well only once");
        }
    }

    private synchronized void amIBrokenYesYouAre() {
        listenerVisited++;
        try {
            // yes, you are broken: wait forever (almost)
            wait();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
