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

import org.openide.filesystems.*;
import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.openide.cookies.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/** Tests that the XML DataObject takes the node as provided by its registered
 * Environment.Provider
 *
 * @author  Jaroslav Tulach
 */
public class XMLDataObjectNodeTest extends org.netbeans.junit.NbTestCase {
    private FileObject data;
    private CharSequence log;

    /** Creates new MultiFileLoaderHid */
    public XMLDataObjectNodeTest (String name) {
        super (name);
    }

    protected void setUp () throws Exception {
        log = Log.enable("org.openide.loaders", Level.WARNING);
        
        super.setUp ();
        String fsstruct [] = new String [] {
        };
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem fs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        data = FileUtil.createData (
            fs.getRoot (),
            "kuk/test/my.xml"
        );
        FileLock lock = data.lock ();
        OutputStream os = data.getOutputStream (lock);
        PrintStream p = new PrintStream (os);
        
        p.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        p.println("<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem " +
            "1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n");
        p.println ("<filesystem>");
        p.println ("</filesystem>");
        
        p.close ();
        lock.releaseLock ();
        
        MockServices.setServices(Env.class);
    }
    
    protected void tearDown () throws Exception {
        super.tearDown ();
        TestUtilHid.destroyLocalFileSystem (getName());
    }
    
    public void testGetNodeBehaviour () throws Exception {
        Env.which = data;
        
        DataObject obj = DataObject.find (data);
        
        if (obj instanceof XMLDataObject) {
            // ok
        } else {
            fail("Expecting XMLDataObject: " + obj);
        }
        
        
        Node n = obj.getNodeDelegate();
        assertEquals("Node is taken from Env Provider", "ENV", n.getName());
    }

    public static final class Env implements Environment.Provider {
        static FileObject which;

        public Lookup getEnvironment(DataObject obj) {
            if (obj.getPrimaryFile().equals(which)) {
                AbstractNode an = new AbstractNode(Children.LEAF);
                an.setName("ENV");
                return an.getLookup();
            }
            return null;
        }
    }
}
