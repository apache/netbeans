/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.ddloaders.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Test for {@link EarDataNode}.
 *
 * @author Martin Krauskopf
 */
public class EarDataNodeTest extends NbTestCase {
    
    // Copied from org.netbeans.api.project.TestUtil:
    static {
        // XXX replace with MockServices
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        assertEquals(Lkp.class, Lookup.getDefault().getClass());
    }
    
    public static final class Lkp extends ProxyLookup {
        private static Lkp DEFAULT;
        public Lkp() {
            assertNull(DEFAULT);
            DEFAULT = this;
            setLookup(new Object[0]);
        }
        public static void setLookup(Object[] instances) {
            ClassLoader l = Lkp.class.getClassLoader();
            DEFAULT.setLookups(new Lookup[] {
                Lookups.fixed(instances),
                Lookups.metaInfServices(l),
                Lookups.singleton(l),
            });
        }
    }
    
    public EarDataNodeTest(String testName) throws Exception {
        super(testName);
        Lkp.setLookup(new Object[] {
            new Pool(),
            new MR(),
            new Repo()
        });
    }
    
    public void testGetActions() throws Exception {
        File ddFile = new File(getWorkDir(), "application.xml");
        String ddContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<application version=\"1.4\" xmlns=\"http://java.sun.com/xml/ns/j2ee\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee " +
                "http://java.sun.com/xml/ns/j2ee/application_1_4.xsd\">" +
                "</application>";
        EarDataNodeTest.dump(ddFile, ddContent);
        FileObject fo = FileUtil.toFileObject(ddFile);
        EarDataObject edo = (EarDataObject) DataObject.find(fo);
        Action[] action = edo.getNodeDelegate().getActions(false);
        for (int i = 0; i < action.length; i++) {
            assertFalse("OpenAction is not present yet", action[i] instanceof OpenAction);
        }
    }
    
    public static void dump(File f, String contents) throws IOException {
        f.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(f);
        try {
            Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            w.write(contents);
            w.flush();
        } finally {
            os.close();
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        
        protected Enumeration loaders() {
            return Enumerations.singleton(DataLoader.getLoader(EarDataLoader.class));
        }
        
    }
    
    private static final class MR extends MIMEResolver {
        
        public String findMIMEType(FileObject fo) {
            return fo.getNameExt().equals("application.xml")
                    ? EarDataLoader.REQUIRED_MIME_PREFIX_1 : null;
        }
        
    }
    
    private static final class Repo extends Repository {
        
        public Repo() throws Exception {
            super(mksystem());
        }
        
        private static FileSystem mksystem() throws Exception {
            URL layerFile = Repo.class.getClassLoader().getResource(
                    "org/netbeans/modules/j2ee/ddloaders/resources/layer.xml");
            assert layerFile != null;
            MultiFileSystem mfs = new MultiFileSystem(new FileSystem[] {
                new XMLFileSystem(layerFile)
            });
            return mfs;
        }
        
    }
    
}
