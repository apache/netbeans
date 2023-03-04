/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
