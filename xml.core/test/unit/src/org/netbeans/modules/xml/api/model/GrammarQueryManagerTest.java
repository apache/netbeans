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

package org.netbeans.modules.xml.api.model;

import junit.framework.*;

/**
 * The test always fails because you cannot register
 * test class in lookup using layer. I would like to
 * see FolderLookup tests for system FS.
 * <p>
 * Last test version passes but kills system class loader.
 *
 * @author Petr Kuzel
 */
public class GrammarQueryManagerTest extends TestCase {
    
    public GrammarQueryManagerTest(java.lang.String testName) {
        super(testName);
    }
    
    /** Test of getDefault method, of class org.netbeans.modules.xml.text.completion.api.GrammarQueryManager. */
    public void testGetDefault() throws Exception {
        System.out.println("testGetDefault");
//
//        FileSystem def = FileUtil.getConfigRoot().getFileSystem();
//        org.netbeans.core.projects.SystemFileSystem system =
//            (org.netbeans.core.projects.SystemFileSystem) def;
//        FileSystem[] original = system.getLayers();
//        ProxyClassLoader testLoader = new ProxyClassLoader(
//            new ClassLoader[] {SampleGrammarQueryManager.class.getClassLoader()}
//        );
//                
//        try {
//            
//            // modify default FS content
//            
//            URL source = getClass().getResource("data/filesystem.xml");
//            XMLFileSystem xmlfs = new XMLFileSystem(source);        
//            List layers = new ArrayList(Arrays.asList(original));            
//            layers.add(xmlfs);
//            FileSystem[] fss = (FileSystem[]) layers.toArray(new FileSystem[0]);
//            system.setLayers(fss);
//            
//            // adjust system classloader
//            
//            ClassLoader loader = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
//            ProxyClassLoader systemLoader = (ProxyClassLoader) loader;
//            systemLoader.append(new ClassLoader[] {testLoader});
//            
//            // test
//            
//            GrammarQueryManager manager = GrammarQueryManager.getDefault();
//            GrammarEnvironment env = new GrammarEnvironment(org.openide.util.Enumerations.empty(), new InputSource(), null);
//            Enumeration trigger = manager.enabled(env);
//            assertTrue("No grammar found!", trigger!=null);
//        } finally {
//            
//            // rollback
//            
//            system.setLayers(original);
//            
//            testLoader.destroy();
//        }        
    }
    
}
