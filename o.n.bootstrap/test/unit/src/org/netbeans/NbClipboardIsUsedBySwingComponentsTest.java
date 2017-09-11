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

package org.netbeans;

import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExClipboard;

/** Test that verifies that Clipboard is used by swing components.
 * @author Jaroslav Tulach
 * @see "#40693"
 */
public class NbClipboardIsUsedBySwingComponentsTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NbClipboardIsUsedBySwingComponentsTest.class);
    }

    private Clip clip;
    private javax.swing.JTextField field;
    
    public NbClipboardIsUsedBySwingComponentsTest (String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(Clip.class);
        System.setProperty ("netbeans.security.nocheck", "true");
        Object clip = Lookup.getDefault ().lookup (ExClipboard.class);
        assertNotNull ("Some clipboard found", clip);
        assertEquals ("Correct clipboard found", Clip.class, clip.getClass());
        this.clip = (Clip)clip;
        
        if (System.getSecurityManager () == null) {
            java.text.NumberFormat.getInstance ();

            Object clazz = org.netbeans.TopSecurityManager.class;
            SecurityManager m = new org.netbeans.TopSecurityManager ();
            System.setSecurityManager (m);
            
            inMiddleOfSettingUpTheManager();
            
            org.netbeans.TopSecurityManager.makeSwingUseSpecialClipboard (this.clip);
        } else {
            inMiddleOfSettingUpTheManager();
        }
        
        field = new javax.swing.JTextField ();
    }
    protected boolean runInEQ () {
        return true;
    }
    
    protected javax.swing.JTextField getField () {
        return field;
    }
    
    
    public void testClipboardOurClipboardUsedDuringCopy () {
        javax.swing.JTextField f = getField ();
        f.setText ("Ahoj");
        f.selectAll ();
        assertEquals ("Ahoj", f.getSelectedText ());
        f.copy ();
        
        Clip.assertCalls ("Copy should call setContent", 1, 0);
        assertClipboard ("Ahoj");
    }
    
    public void testClipboardOurClipboardUsedDuringCut () {
        javax.swing.JTextField f = getField ();
        f.setText ("DoCut");
        f.selectAll ();
        assertEquals ("DoCut", f.getSelectedText ());
        f.cut ();
        
        Clip.assertCalls ("Cut should call setContent", 1, 0);
        assertClipboard ("DoCut");
        
        assertEquals ("Empty", "", f.getText ());
    }
    
    public void testClipboardOurClipboardUsedDuringPaste () {
        javax.swing.JTextField f = getField ();
        
        StringSelection sel = new StringSelection ("DoPaste");
        clip.setContents (sel, sel);
        Clip.assertCalls ("Of course there is one set", 1, 0);
        
        assertClipboard ("DoPaste");
        f.paste ();
        
        Clip.assertCalls ("Paste should call getContent", 0, 1);
        assertEquals ("Text is there", "DoPaste", f.getText ());
    }
    
    public void testCopyFromEditorPasteToTheSameOneIssue40785 () {
        javax.swing.JTextField f = getField ();
        f.setText (getName ());
        f.selectAll ();
        assertEquals ("Selection is correct", getName (), f.getSelectedText ());
        f.copy ();
        Clip.assertCalls ("Once in, none out", 1, 0);
        f.setText ("");
        f.paste ();
        Clip.assertCalls ("Once out, none in", 0, 1);
        
        assertEquals ("Test is again the same", getName (), f.getText ());
    }
    
    public void testItIsStillPossibleToGetTheClipboardForNormalCode () throws Exception {
        assertNotNull (
            java.awt.Toolkit.getDefaultToolkit ().getSystemClipboard ()
        );
    }
    
    public void assertClipboard (String text) {
        try {
            Transferable t = clip.getContentsSuper (this);
            Object obj = t.getTransferData (java.awt.datatransfer.DataFlavor.stringFlavor);
            assertEquals ("Clipboard is the same", text, obj);
        } catch (java.io.IOException ex) {
            fail (ex.getMessage ());
        } catch (java.awt.datatransfer.UnsupportedFlavorException ex) {
            fail (ex.getMessage ());
        }
    }

    protected void inMiddleOfSettingUpTheManager() {
    }
    
    public static final class Clip extends ExClipboard {
        private static int setContents;
        private static int getContents;
        
        public Clip () {
            super ("Clip");
        }
        
        protected ExClipboard.Convertor[] getConvertors () {
            return new ExClipboard.Convertor[0];
        }
        
        public void setContents (Transferable contents, ClipboardOwner owner) {
            super.setContents (contents, owner);
            setContents++;
        }
        
        public Transferable getContents (Object requestor) {
            Transferable retValue;
            getContents++;
            retValue = super.getContents (requestor);
            return retValue;
        }
        public Transferable getContentsSuper (Object requestor) {
            return  super.getContents (requestor);
        }
        
        public static void assertCalls (String msg, int setContents, int getContents) {
            if (setContents != -1) assertEquals (msg + " setContents", setContents, Clip.setContents);
            if (getContents != -1) assertEquals (msg + " getContents", getContents, Clip.getContents);
            
            Clip.setContents = 0;
            Clip.getContents = 0;
        }
    } // Clip
}
