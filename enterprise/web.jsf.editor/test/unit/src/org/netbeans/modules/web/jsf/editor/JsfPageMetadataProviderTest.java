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
package org.netbeans.modules.web.jsf.editor;

import java.util.Collection;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.web.common.api.WebPageMetadata;
import org.netbeans.modules.web.jsfapi.api.JsfUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author marekfukala
 */
public class JsfPageMetadataProviderTest extends TestBaseForTestProject {

    public JsfPageMetadataProviderTest(String testName) {
        super(testName);
    }

    public static Test Xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JsfPageMetadataProviderTest("testX"));
        return suite;
    }

    public void testInstanceInGlobalLookup() {
        JsfPageMetadataProvider instance = Lookup.getDefault().lookup(JsfPageMetadataProvider.class);
        assertNotNull(instance);
    }

    public void testGetPageMetadata() throws Exception {
        FileObject file = getWorkFile("testWebProject/web/index.xhtml");
        assertNotNull(file);

        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(new HtmlSource(file)).analyze();
        InstanceContent ic = new InstanceContent();
        ic.add(result);
        Lookup lookup = new AbstractLookup(ic);

        WebPageMetadata meta = WebPageMetadata.getMetadata(lookup);
        assertNotNull(meta);
        
        Collection<String> namespaces = (Collection<String>) meta.value(JsfPageMetadataProvider.JSF_LIBRARIES_KEY);
        assertNotNull(namespaces);
        assertTrue(namespaces.contains("http://java.sun.com/jsf/html"));
        assertTrue(namespaces.contains("http://java.sun.com/jsp/jstl/core"));
        assertFalse(namespaces.contains("http://www.w3.org/1999/xhtml"));

        //test mimetype
        assertEquals(JsfUtils.JSF_XHTML_FILE_MIMETYPE, meta.value(WebPageMetadata.MIMETYPE));
    }

    public void testNoPageMetadataForPlainXHTML() throws Exception {
        FileObject file = getWorkFile("testWebProject/web/nofacelets.xhtml");
        assertNotNull(file);

        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(new HtmlSource(file)).analyze();
        InstanceContent ic = new InstanceContent();
        ic.add(result);
        Lookup lookup = new AbstractLookup(ic);

        WebPageMetadata meta = WebPageMetadata.getMetadata(lookup);
        assertNull(meta);
    }

}
