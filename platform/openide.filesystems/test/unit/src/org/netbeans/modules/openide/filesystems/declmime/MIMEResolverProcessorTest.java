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
package org.netbeans.modules.openide.filesystems.declmime;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Item;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@MIMEResolver.Registration(
        resource="mime-resolver-rule.xml",
        displayName="#MYNAME",
        position=91,
        showInFileChooser="#XML_BNM_FILES"
)
@NbBundle.Messages({
    "MYNAME=My Name",
    "EXTNAME=XYZ extension",
    "SPACENAME=Cosmic space",
    "ABCXYX_FILES=ABC and XYZ Files",
    "TEST_FILES=Test Files",
    "XML_BNM_FILES=XML and BNM Files"
})
@MIMEResolver.ExtensionRegistration(
    displayName="#EXTNAME", 
    extension={"abc", "xyz"}, 
    mimeType="text/x-yz",
    position=92,
    showInFileChooser={"#ABCXYX_FILES", "#TEST_FILES"}
)
@MIMEResolver.NamespaceRegistration(
    displayName="#SPACENAME",
    checkedExtension={ "axml", "bxml", "cxml" },
    acceptedExtension="jarda",
    mimeType="text/x-my+xml",
    doctypePublicId={ "-//My/Type/EN", "-//Your/Type/EN" },
    elementName="myandyour",
    elementNS="http://some.org/ns/123",
    position=93
)
public class MIMEResolverProcessorTest extends NbTestCase {
    private FileObject root;
    public MIMEResolverProcessorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        
        URL u = this.getClass().getResource("data-fs.xml");
        XMLFileSystem fs = new XMLFileSystem(u);
        MultiFileSystem mfs = new MultiFileSystem(lfs, fs);
        root = mfs.getRoot().getFileObject("root");        
    }
    
    

    public void testXMLFileResolver() throws Exception {
        final String PATH = "Services/MIMEResolver/"
            + "org-netbeans-modules-openide-filesystems-declmime-MIMEResolverProcessorTest-Registration.xml";
        FileObject fo = FileUtil.getConfigFile(PATH);
        assertNotNull("Registration found", fo);
        String dispName = fo.getFileSystem().getDecorator().annotateName(fo.getName(), Collections.singleton(fo));
        assertEquals("Proper display name", Bundle.MYNAME(), dispName);
        
        assertNotNull("Declaration found", fo);
        MIMEResolver mime = FileUtil.getConfigObject(PATH, MIMEResolver.class);
        assertNotNull("Mime type found", mime);
        assertEquals("build1.xml recognized as Ant script", "text/x-ant+xml", mime.findMIMEType(root.getFileObject("build1", "xml")));
        
        Map<String, Set<String>> map = MIMEResolverImpl.getMIMEToExtensions(fo);
        assertNotNull("Map is provided", map);
        assertFalse("Map is not empty", map.isEmpty());
        Set<String> arr = map.get("text/x-ant+xml");
        assertEquals("One extension", 1, arr.size());
        assertEquals("It is xml", "xml", arr.iterator().next());
    }
    
    public void testExtensionResolver() throws Exception {
        final String PATH = "Services/MIMEResolver/"
                + "org-netbeans-modules-openide-filesystems-declmime-MIMEResolverProcessorTest-Extension.xml";
        FileObject fo = FileUtil.getConfigFile(PATH);
        assertNotNull("Registration found", fo);
        String dispName = fo.getFileSystem().getDecorator().annotateName(fo.getName(), Collections.singleton(fo));
        assertEquals("Proper display name", Bundle.EXTNAME(), dispName);

        assertNotNull("Declaration found", fo);
        MIMEResolver mime = FileUtil.getConfigObject(PATH, MIMEResolver.class);
        assertNotNull("Mime type found", mime);
        
        FileObject check = FileUtil.createMemoryFileSystem().getRoot().createData("my.xyz");
        FileObject check2 = FileUtil.createMemoryFileSystem().getRoot().createData("my.xyz");
        assertEquals("xyz recognized OK", "text/x-yz", mime.findMIMEType(check));        
        assertEquals("abc recognized OK", "text/x-yz", mime.findMIMEType(check2));        

        Map<String, Set<String>> map = MIMEResolverImpl.getMIMEToExtensions(fo);
        assertNotNull("Map is provided", map);
        assertFalse("Map is not empty", map.isEmpty());
        Set<String> arr = map.get("text/x-yz");
        assertEquals("One extension", 2, arr.size());
        assertTrue("contains abc", arr.contains("abc"));
        assertTrue("contains xyz", arr.contains("xyz"));
    }
    
    public void testExtensionFileFilterRegistration() {
        final String PATH = "Services/MIMEResolver/"
                + "org-netbeans-modules-openide-filesystems-declmime-MIMEResolverProcessorTest-Registration.xml";
        FileObject fo = FileUtil.getConfigFile(PATH);
        assertNotNull(fo);
        assertEquals("xml", fo.getAttribute("ext.0"));
        assertEquals("[importantName, true, false]bnm", fo.getAttribute("fileName.0"));
        assertEquals("text/x-ant+xml", fo.getAttribute("mimeType.0"));
        assertEquals("text/x-bnm", fo.getAttribute("mimeType.1"));
        assertEquals("XML and BNM Files", fo.getAttribute("fileChooser.0"));
    }

    public void testResourceFileFilterRegistration() {
        final String PATH = "Services/MIMEResolver/"
                + "org-netbeans-modules-openide-filesystems-declmime-MIMEResolverProcessorTest-Extension.xml";
        FileObject fo = FileUtil.getConfigFile(PATH);
        assertNotNull(fo);
        assertEquals("abc", fo.getAttribute("ext.0"));
        assertEquals("xyz", fo.getAttribute("ext.1"));
        assertEquals("text/x-yz", fo.getAttribute("mimeType"));
        assertEquals("ABC and XYZ Files", fo.getAttribute("fileChooser.0"));
        assertEquals("Test Files", fo.getAttribute("fileChooser.1"));
    }

    public void testNameElement() throws Exception {
        MIMEResolver resolver = findResolver("Cosmic space");
        assertMimeType(resolver, "text/x-my+xml", "namespace.axml", "namespace.bxml");
        assertMimeType(resolver, null, "nodtd.axml");
        assertMimeType(resolver, null, "pid.xml");        assertMimeType(resolver, null, "noelem.bxml");
        assertMimeType(resolver, "text/x-my+xml", "namespace.cxml");
    }
    public void testAcceptedExtension() throws Exception {
        MIMEResolver resolver = findResolver("Cosmic space");
        FileObject fo = root.createData("my.jarda");
        assertMimeType(resolver, "text/x-my+xml", "my.jarda");
        assertEquals("Is empty", 0, fo.getSize());
    }
    private void assertMimeType(MIMEResolver resolver, String expectedMimeType, String... filenames) throws IOException {
        for (String filename : filenames) {
            final FileObject fo = root.getFileObject(filename);
            assertNotNull("Original found: " + filename, fo);
            String mimeType = resolver.findMIMEType(fo);
            assertEquals("File " + filename + " not properly resolved by " + resolver + ".", expectedMimeType, mimeType);
        }
    }
    
    private static MIMEResolver findResolver(String name) {
        final Collection<? extends Item<MIMEResolver>> arr = Lookups.forPath("Services/MIMEResolver").lookupResult(MIMEResolver.class).allItems();
        for (Lookup.Item<MIMEResolver> i : arr) {
            if (i.getDisplayName().equals(name)) {
                return i.getInstance();
            }
        }
        fail("Cannot find resolver name: " + name + " but found " + arr);
        return null;
    }
}
