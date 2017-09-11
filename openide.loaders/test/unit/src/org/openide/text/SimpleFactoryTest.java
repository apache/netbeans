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

package org.openide.text;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.actions.Savable;
import org.netbeans.junit.*;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.util.Lookup;

/** Check behavior of 
 * {@link DataEditorSupport#create(org.openide.loaders.DataObject, org.openide.loaders.MultiDataObject.Entry, org.openide.nodes.CookieSet, java.util.concurrent.Callable)}
 * factory method.
 *
 * @author  Jaroslav Tulach
 */
public final class SimpleFactoryTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(SimpleFactoryTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    private FileSystem lfs;
    private DataObject obj;
    
    public SimpleFactoryTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected boolean runInEQ() {
        return false;
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        clearWorkDir ();
        
        MockServices.setServices(DLP.class);
        super.setUp();
        
        LocalFileSystem l = new LocalFileSystem ();
        l.setRootDirectory (getWorkDir ());
        lfs = l;
        
        FileObject fo = FileUtil.createData (lfs.getRoot (), "AA/" + getName () + ".test");
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);
        
        assertEquals ("The right class", obj.getClass (), SO.class);
    }

    public void testOurNodeSubclassCreated() throws Exception {
        final EditorCookie ec = obj.getLookup().lookup(EditorCookie.class);
        assertNotNull("Editor", ec);
        Openable open = obj.getLookup().lookup(Openable.class);
        assertNotNull("Open", open);
        open.open();
        
        class GEP implements Runnable {
            JEditorPane[] arr;
            
            @Override
            public void run() {
                arr = ec.getOpenedPanes();
            }
        }
        GEP gep = new GEP();
        EventQueue.invokeAndWait(gep);
        
        assertEquals("One", 1, gep.arr.length);
        
        MyCE mice = null;
        Container c = gep.arr[0];
        for (;;) {
            if (c instanceof MyCE) {
                // OK
                mice = (MyCE)c;
                break;
            }
            if (c instanceof CloneableEditor) {
                fail("Wrong CloneableEditor: " + c);
            }
            if (c == null) {
                fail("No good parent!");
            }
            c = c.getParent();
        }
        
        assertNotNull("MyCE found", mice);
        assertNull("No integers", mice.getLookup().lookup(Integer.class));
        ((SO)obj).addInteger();
        assertEquals("One integer in object", Integer.valueOf(10), obj.getLookup().lookup(Integer.class));
        assertEquals("One integer", Integer.valueOf(10), mice.getLookup().lookup(Integer.class));
        
        Savable sav = obj.getLookup().lookup(Savable.class);
        assertNull("No savable yet", sav);
        
        ec.getDocument().insertString(0, "Ahoj!", null);
        
        sav = obj.getLookup().lookup(Savable.class);
        assertNotNull("Now modified", sav);
        
        assertEquals(obj.getPrimaryFile().getNameExt(), sav.toString());
        obj.setModified(false);
        assertNull("Changes discarded", obj.getLookup().lookup(Savable.class));
    }
    
    private static final class SL extends org.openide.loaders.UniFileLoader {
        public SL () {
            super (SO.class.getName ());
            getExtensions().addExtension("test");
        }
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SO (primaryFile);
        }
    } // end of SL
    
    private static final class SO extends org.openide.loaders.MultiDataObject 
    implements Callable<CloneableEditorSupport.Pane> {
        private CloneableEditorSupport support = DataEditorSupport.create(this, getPrimaryEntry(), getCookieSet (), this);
        
        
        public SO (FileObject fo) throws org.openide.loaders.DataObjectExistsException {
            super (fo, SL.getLoader(SL.class));
            getCookieSet ().add((Node.Cookie)support);
        }
        
        @Override
        public Lookup getLookup() {
            return getCookieSet().getLookup();
        }
        
        @Override
        public Pane call() throws Exception {
            return new MyCE(support);
        }
        
        public void addInteger() {
            getCookieSet().assign(Integer.class, 10);            
        }
    } // end of SO
    
    private static final class MyCE extends CloneableEditor {
        private MyCE(CloneableEditorSupport support) {
            super(support);
            this.associateLookup(support.getLookup());
        }
    }

    public static final class DLP extends org.openide.loaders.DataLoaderPool {

        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            return java.util.Collections.enumeration(
                java.util.Collections.singleton(
                    SL.getLoader (SL.class)
                )
            );
        }
        
    } // end of DataLoaderPool
}
