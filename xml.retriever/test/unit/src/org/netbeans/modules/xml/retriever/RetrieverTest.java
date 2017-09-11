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
/*
 * RetrieverTest.java
 * JUnit based test
 *
 * Created on August 16, 2006, 5:02 PM
 */

package org.netbeans.modules.xml.retriever;

import junit.framework.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class RetrieverTest extends TestCase {
    
    public RetrieverTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(RetrieverTest.class);
        
        return suite;
    }
    
    /**
     * Test of getDefault method, of class org.netbeans.modules.xml.retriever.Retriever.
     */
    public void testGetDefault() throws URISyntaxException, UnknownHostException, IOException {
        System.out.println("getDefault");
        
        Retriever expResult = null;
        Retriever result = Retriever.getDefault();
        //uncomment the following to test the retriever method
        /*File destFolder = new File(System.getProperty("java.io.tmpdir")+File.separator+"RetrieverTest");
        if(destFolder.isDirectory())
            destFolder.renameTo(new File(destFolder.toString()+System.currentTimeMillis()));
        destFolder.mkdirs();
        URI catFileURI = null;
        catFileURI = new URI(destFolder.toURI().toString() + "/catalogfile.xml");
        FileObject dstFO = FileUtil.toFileObject(FileUtil.normalizeFile(destFolder));
        
        result.retrieveResource(dstFO, catFileURI, new URI("http://localhost:8084/grt/maindoc/UBL-Order-1.0"));
         **/
    }
    
    /**
     * Test of getDefault method, of class org.netbeans.modules.xml.retriever.Retriever.
     */
    public void testRelativize() throws Exception {
        System.out.println("getDefault");
        URI masterURI = new URI("A/B/C");
        URI slaveURI = new URI("A/B/C/D/E");
        String result = Utilities.relativize(masterURI, slaveURI);
        assert(result.equals("D/E"));        
	
        masterURI = new URI("file:/A/B/C/");
	slaveURI = new URI("file:/A/F/G");
        result = Utilities.relativize(masterURI, slaveURI);
        assert( result.equals("../../F/G"));
    }
    
    
}
