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
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.Date;
import java.util.Stack;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.DefaultAttributes;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;

/** Subversion is said to change timestamp of files on rename. Let's test
 * the editor behavior in such case.
 *
 * @author Jaroslav Tulach
 */
public class RenameWithTimestampChangeTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(RenameWithTimestampChangeTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    private DataObject obj;
    private EditorCookie edit;
    private long lastM;
    
    
    public RenameWithTimestampChangeTest (java.lang.String testName) {
        super(testName);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 0;//20000;
    }

    @Override
    protected void setUp () throws Exception {
        MockServices.setServices(DD.class);
        
        clearWorkDir();
        MyFS fs = new MyFS();
        
        FileObject fo = fs.findResource("dir/x.txt");
        lastM = fo.lastModified().getTime();
        
        obj = DataObject.find(fo);
        edit = obj.getCookie(EditorCookie.class);
        assertNotNull("we have editor", edit);

        DD.type = -1;
        DD.toReturn = new Stack<Object>();
    }

    public void testRenameTheDocumentWhilechangingTheTimestamp() throws Exception {
        Document doc = edit.openDocument();
        
        assertFalse("Not Modified", edit.isModified());

        doc.insertString(0, "Base change\n", null);
        assertTrue("Is Modified", edit.isModified());
        
        edit.open();
        waitEQ();

        JEditorPane[] arr = getPanes();
        assertNotNull("There is one opened pane", arr);
        
        obj.getFolder().rename("newName");

        assertEquals("Last modified incremented by 10000", lastM + 10000, obj.getPrimaryFile().lastModified().getTime());
        assertTrue("Name contains newName: " + obj.getPrimaryFile(), obj.getPrimaryFile().getPath().contains("newName/"));
        
        waitEQ();
        edit.saveDocument();
        
        if (DD.error != null) {
            fail("Error in dialog:\n" + DD.error);
        }
    }


    private JEditorPane[] getPanes() {
        return Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane[]>() {
            public JEditorPane[] run() {
                return edit.getOpenedPanes();
            }
        });
    }
    
    private void waitEQ() throws InterruptedException, java.lang.reflect.InvocationTargetException {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() { 
            public void run () { 
            } 
        });
    }

    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        static final long serialVersionUID = 3L;

        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new DD ());
        }
    }

    /** Our own dialog displayer.
     */
    public static final class DD extends org.openide.DialogDisplayer {
        public static Object[] options;
        public static Stack<Object> toReturn;
        public static Object message;
        public static int type;
        public static String error;
        
        public static void clear(Object t) {
            type = -1;
            message = null;
            options = null;
            toReturn.clear();
            toReturn.push(t);
        }
        
        @Override
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        @Override
        public Object notify(org.openide.NotifyDescriptor descriptor) {
            assertNull (options);
            if (type != -1) {
                error = "Second question: " + type;
                fail(error);
            }
            if (toReturn.isEmpty()) {
                error = "Not specified what we shall return: " + toReturn;
                fail(error);
            }
            Object r = toReturn.pop();
            if (toReturn.isEmpty()) {
                options = descriptor.getOptions();
                message = descriptor.getMessage();
                type = descriptor.getOptionType();
            }
            return r;
        }
        
    } // end of DD

    private static final class MyFS extends TestFileSystem {
        long lastM = 10000;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public MyFS() {
            this.info = this;
            this.list = this;
            this.change = this;
            this.attr = new DefaultAttributes(info, change, list);
        }


        @Override
        public Date lastModified(String name) {
            return new Date(lastM);
        }


        @Override
        public void rename(String oldName, String newName) throws IOException {
            if (oldName.equals(dir)) {
                dir = newName;
                lastM += 10000;
            } else {
                throw new IOException();
            }
        }

    }
}
