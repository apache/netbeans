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

package org.netbeans.modules.settings.convertors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @author radim
 */
public class LayersTest extends NbTestCase {
    
    public LayersTest(String testName) {
        super(testName);
    }

    /* XXX this does not make sense as a unit test; could be in a functional test using NbModuleSuite:
    public void testFastParsingOfXMLFiles() throws Exception {
        CharSequence chars = Log.enable(XMLSettingsSupport.class.getName(), Level.FINE);
        int len = 0;
        FileObject dir = FileUtil.getConfigRoot();
        Enumeration<? extends FileObject> en = dir.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            if (fo.isFolder())
                continue;
            if (!"settings".equals(fo.getExt())) {
                continue;
            }
            // check only settings files without convertors
            String loc = fo.getURL().toExternalForm();
            Document doc = XMLUtil.parse(new InputSource(loc), false, true, null, EntityCatalog.getDefault());
            if (!"-//NetBeans//DTD Session settings 1.0//EN".equals(doc.getDoctype().getPublicId()))
                continue;
            
            log("checking "+fo.getPath());
            try {
                XMLSettingsSupport.SettingsRecognizer sr = new XMLSettingsSupport.SettingsRecognizer(true, fo);
                sr.parse();
            }
            catch (IOException ioe) {
                fail("IOException was thrown: "+ioe.getMessage());
            }
            if (chars.length() > len) {
                log("quickParse fails");
                len = chars.length();
            }
        }
        if (chars.length() > 0) {
            fail("fast parsing of .settings files fails :"+chars.toString());
        }
    }
     */
    
    // This is supposed to be run in ide mode.
    // It is meaningless if run in code mode,
    // and it will furthermore fail if the internet connection is down.
    @RandomlyFails
    public void testCorrectContentOfSettingsFiles() throws Exception {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        assertNotNull ("In the IDE mode, there always should be a classloader", l);
        
        List<Module> urls = new ArrayList<Module>();
        Enumeration<URL> en = l.getResources("META-INF/MANIFEST.MF");
        while (en.hasMoreElements ()) {
            URL u = en.nextElement();
            InputStream is = u.openStream();
            Manifest mf;
            try {
                mf = new Manifest(is);
            } finally {
                is.close();
            }
            String module = mf.getMainAttributes ().getValue ("OpenIDE-Module");
            if (module == null) continue;
            String layer = mf.getMainAttributes ().getValue ("OpenIDE-Module-Layer");
            if (layer == null) continue;
            
            URL layerURL = new URL(u, "../" + layer);
            Module m = new Module();
            m.module = module;
            m.layer = layerURL;
            urls.add(m);
        }

//        CharSequence chars = Log.enable(XMLSettingsSupport.class.getName(), Level.FINE);
        StringBuilder sb = new StringBuilder();
        for (Module m: urls) {
            if ("org.netbeans.modules.settings.xtest/1".equals(m.module)) {
                continue;
            }
            log("Checking layer of "+m.module);
            XMLFileSystem xmlfs = new XMLFileSystem(m.layer);
            FileObject dir = xmlfs.getRoot();
            Enumeration<? extends FileObject> en2 = dir.getChildren(true);
            while (en2.hasMoreElements()) {
                FileObject fo = en2.nextElement();
                if (fo.isFolder())
                    continue;
                if (!"settings".equals(fo.getExt())) {
                    continue;
                }
                
                if ("Services/org-netbeans-core-IDESettings.settings".equals(fo.getPath())) {
                    // for some reason defined in layer of core/ui although belongs to core
                    continue;
                }
                // check only settings files without convertors
                String loc = fo.getURL().toExternalForm();
                Document doc = XMLUtil.parse(new InputSource(loc), false, true, null, EntityCatalog.getDefault());
                if (!"-//NetBeans//DTD Session settings 1.0//EN".equals(doc.getDoctype().getPublicId()))
                    continue;
                
                log("checking "+fo.getPath());
                try {
                    XMLSettingsSupport.SettingsRecognizer sr = new XMLSettingsSupport.SettingsRecognizer(true, fo);
                    sr.parse();
//                    String cnb = m.module;
                    String cnb = (m.module.indexOf('/') == -1)? m.module: m.module.substring(0, m.module.indexOf('/'));
                    String cnbFromFile = sr.getCodeNameBase();
                    if (sr.getCodeNameBase() != null && sr.getCodeNameBase().indexOf('/') != -1) {
                        cnbFromFile = sr.getCodeNameBase().substring(0, sr.getCodeNameBase().indexOf('/'));
                    }
                    if (!cnb.equals(cnbFromFile)) {
                        sb.append("Codenamebase of module in ").append(fo.getPath()).
                                append(" does not refer to module ").append(m.module).append(" it refers to ").
                                append(sr.getCodeNameBase()).append('\n');
                    }
                    // TODO check instance... attrs
                }
                catch (IOException ioe) {
                    fail("IOException was thrown: "+ioe.getMessage());
                }
//                if (chars.length() > len) {
//                    log("quickParse fails");
//                    len = chars.length();
//                }
            }
        }
        if (sb.length() > 0) {
            fail(sb.toString());
        }
    }

    private static class Module {
        String module;
        URL layer;
    }
}
