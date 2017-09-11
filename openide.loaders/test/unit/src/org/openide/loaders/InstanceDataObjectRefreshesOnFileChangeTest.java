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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * @author Jaroslav Tulach
 */
public class InstanceDataObjectRefreshesOnFileChangeTest extends NbTestCase
implements LookupListener, PropertyChangeListener {
    private FileSystem mem1;
    private FileSystem mem2;
    private int cnt;
    private int pcl;
    
    public InstanceDataObjectRefreshesOnFileChangeTest(String name) {
        super (name);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINER;
    }
    
    @Override
    protected void setUp () throws Exception {
        mem1 = FileUtil.createMemoryFileSystem();
        mem2 = FileUtil.createMemoryFileSystem();

        FileObject fo2 = FileUtil.createData(mem2.getRoot(), "Folder/MyInstance.instance");
        fo2.setAttribute("instanceCreate", "NewOne");
        Thread.sleep(300);
        FileObject fo1 = FileUtil.createData(mem1.getRoot(), "Folder/MyInstance.instance");
        fo1.setAttribute("instanceCreate", "OldOne");

        MockServices.setServices(DynamicFS.class);
        DynamicFS dfs = Lookup.getDefault().lookup(DynamicFS.class);
        dfs.setDelegate(mem1);
    }

    public void testSwitchRefreshesIDO() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Folder/MyInstance.instance");
        assertNotNull("File visible in SFS", fo);
        DataObject ido = DataObject.find(fo);
        Lookup lkp = ido.getLookup();
        Result<InstanceCookie> res = lkp.lookupResult(InstanceCookie.class);
        assertEquals("One cookie", 1, res.allItems().size());
        res.addLookupListener(this);
        ido.addPropertyChangeListener(this);

        assertInstance(lkp, "OldOne");

        assertEquals("no lookup change yet", 0, cnt);
        assertEquals("no pcl change yet", 0, pcl);

        DynamicFS dfs = Lookup.getDefault().lookup(DynamicFS.class);
        dfs.setDelegate(mem2);

        assertEquals("one pcl change now", 1, pcl);
        if (cnt == 0) {
            fail("At least one change in lookup shall be notified");
        }

        FileObject fo2 = FileUtil.getConfigFile("Folder/MyInstance.instance");
        assertNotNull("File is still visible in SFS", fo);
        DataObject ido2 = DataObject.find(fo2);

        assertSame("Data object remains", ido, ido2);

        assertInstance(lkp, "NewOne");
    }

    private static void assertInstance(Lookup lkp, Object instance) throws Exception {
        InstanceCookie ic = lkp.lookup(InstanceCookie.class);
        assertNotNull("InstanceCookie found", ic);
        Object o = ic.instanceCreate();
        assertNotNull("Instance created", o);
        assertEquals("Same as expected", instance, o);
    }

    public void resultChanged(LookupEvent ev) {
        cnt++;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (InstanceDataObject.PROP_COOKIE.equals(evt.getPropertyName())) {
            pcl++;
        }
    }

    public static final class DynamicFS extends MultiFileSystem {
        public DynamicFS() {
        }

        public void setDelegate(FileSystem fs) {
            super.setDelegates(fs);
        }
    }
}
