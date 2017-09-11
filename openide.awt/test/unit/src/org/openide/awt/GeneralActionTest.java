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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.openide.awt;

import java.awt.event.ActionListener;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class GeneralActionTest extends NbTestCase {
    private FileObject folder;
    
    public GeneralActionTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        folder = FileUtil.getConfigFile("actions/support/test");
        assertNotNull("testing layer is loaded: ", folder);
    }
    
    protected void tearDown() throws Exception {
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    public void testKeyMustBeProvided() {
        String key = null;
        Action defaultDelegate = null;
        Lookup context = Lookup.EMPTY;
        
        ContextAwareAction expResult = null;
        try {
            ContextAwareAction result = GeneralAction.callback(key, defaultDelegate, context, false, false);
            fail("Shall fail as key is null");
        } catch (NullPointerException ex) {
            // ok
        }
    }

    public void testAttributesOnClone() throws Exception { // #182601
        Action a = GeneralAction.callback("whatever", null, Lookup.EMPTY, false, false);
        a.putValue("attr", "value");
        Action a2 = ((ContextAwareAction) a).createContextAwareInstance(Lookup.EMPTY);
        assertEquals("value", a2.getValue("attr"));
    }
    
    private static int myListenerCounter;
    private static ActionListener myListener() {
        myListenerCounter++;
        return null;
    }
    private static int myIconResourceCounter;
    private static String myIconResource() {
        myIconResourceCounter++;
        return "/org/netbeans/modules/actions/support/TestIcon.png";
    }
    
    
    private Action readAction(String fileName) throws Exception {
        FileObject fo = this.folder.getFileObject(fileName);
        assertNotNull("file " + fileName, fo);
        
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("File object has not null instanceCreate attribute", obj);
        
        if (!(obj instanceof Action)) {
            fail("Object needs to be action: " + obj);
        }
        
        return (Action)obj;
    }
}
