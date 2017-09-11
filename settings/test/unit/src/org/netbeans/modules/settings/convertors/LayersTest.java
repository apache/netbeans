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
