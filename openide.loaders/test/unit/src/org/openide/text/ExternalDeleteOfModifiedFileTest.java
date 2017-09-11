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
import java.util.Stack;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

import org.openide.DialogDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;

/** Modified editor shall not be closed when its file is externally removed.
 *
 * @author Jaroslav Tulach
 */
public class ExternalDeleteOfModifiedFileTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ExternalDeleteOfModifiedFileTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    private DataObject obj;
    private EditorCookie edit;
    
    
    public ExternalDeleteOfModifiedFileTest (java.lang.String testName) {
        super(testName);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 20000;
    }

    @Override
    protected void setUp () throws Exception {
        MockServices.setServices(DD.class);
        
        
        clearWorkDir();
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        
        FileObject fo = fs.getRoot().createData("Ahoj", "txt");
        
        obj = DataObject.find(fo);
        edit = obj.getCookie(EditorCookie.class);
        assertNotNull("we have editor", edit);

        DD.type = -1;
        DD.toReturn = new Stack<Object>();
    }

    public void testModifyTheFileAndThenPreventItToBeSavedOnFileDisappear() throws Exception {
        Document doc = edit.openDocument();
        
        doc.insertString(0, "Ahoj", null);
        assertTrue("Modified", edit.isModified());
        
        edit.open();
        waitEQ();

        JEditorPane[] arr = getPanes();
        assertNotNull("There is one opened pane", arr);
        
        java.awt.Component c = arr[0];
        while (!(c instanceof CloneableEditor)) {
            c = c.getParent();
        }
        CloneableEditor ce = (CloneableEditor)c;

        // select close
        DD.toReturn.push(DialogDescriptor.CANCEL_OPTION);
        
        java.io.File f = FileUtil.toFile(obj.getPrimaryFile());
        assertNotNull("There is file behind the fo", f);
        f.delete();
        obj.getPrimaryFile().getParent().refresh();

        waitEQ();
        
        assertNotNull ("Text message was there", DD.message);
        assertEquals("Ok/cancel type", DialogDescriptor.OK_CANCEL_OPTION, DD.type);
        
        String txt = doc.getText(0, doc.getLength());
        assertEquals("The right text is there", txt, "Ahoj");
        
        arr = getPanes();
        assertNotNull("Panes are still open", arr);
        assertTrue("Document is remains modified", edit.isModified());
        
        // explicit close request, shall show the get another dialog
        // and now say yes to close
        DD.clear(DialogDescriptor.OK_OPTION);
        
        ce.close();
        waitEQ();
        
        arr = getPanes();
        assertNull("Now everything is closed", arr);
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

    /** Our own dialog displayer.
     */
    public static final class DD extends org.openide.DialogDisplayer {
        public static Object[] options;
        public static Stack<Object> toReturn;
        public static Object message;
        public static int type;
        
        public static void clear(Object t) {
            type = -1;
            message = null;
            options = null;
            toReturn.clear();
            toReturn.push(t);
        }
        
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        public Object notify(org.openide.NotifyDescriptor descriptor) {
            assertNull (options);
            if (type != -1) {
                fail("Second question: " + type);
            }
            if (toReturn.isEmpty()) {
                fail("Not specified what we shall return: " + toReturn);
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
    
}
