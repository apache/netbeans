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

package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.EnumSet;
import java.util.Map;
import org.netbeans.modules.web.jsf.editor.TestBaseForTestProject;
import org.netbeans.modules.web.jsf.impl.facesmodel.DefaultLocaleImpl;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.Tag;

/**
 *
 * @author marekfukala
 */
public class DefaultFaceletLibrariesTest extends TestBaseForTestProject {

    public DefaultFaceletLibrariesTest(String name) {
        super(name);
    }

    public void testBasic() {
        DefaultFaceletLibraries instance = DefaultFaceletLibraries.getInstance();
        assertNotNull(instance);

        Map<String, FaceletsLibraryDescriptor> descriptors = instance.getLibrariesDescriptors();
        assertNotNull(descriptors);
        assertFalse(descriptors.isEmpty());

    }

    //test indexing and obtaining librarty descriptors for facelet libraries found
    //in the bundled javax.faces.jar
    public void testFoundLibraries() {
        DefaultFaceletLibraries instance = DefaultFaceletLibraries.getInstance();
        assertNotNull(instance);

        Map<String, FaceletsLibraryDescriptor> descriptors = instance.getLibrariesDescriptors();
        assertNotNull(descriptors);

        //test if all default libraries have been found and are correct
        for(DefaultLibraryInfo dli : EnumSet.complementOf(EnumSet.of(
                DefaultLibraryInfo.JSF,
                DefaultLibraryInfo.PASSTHROUGH,
                DefaultLibraryInfo.PRIMEFACES,
                DefaultLibraryInfo.PRIMEFACES_MOBILE))) {
            FaceletsLibraryDescriptor descr = descriptors.get(dli.getNamespace());
            assertNotNull(descr);

            assertEquals(dli.getNamespace(), descr.getNamespace());

            assertNotNull(descr.getDefinitionFile());

            Map<String, Tag> tags = descr.getTags();
            assertNotNull(tags);
        }        

    }

    public void testHtmlOutputStylesheet() {
        FaceletsLibraryDescriptor htmlLibDescriptor = DefaultFaceletLibraries.getInstance().getLibrariesDescriptors().get(DefaultLibraryInfo.HTML.getNamespace());
        assertNotNull(htmlLibDescriptor);

        Map<String, Tag> tags = htmlLibDescriptor.getTags();
        assertNotNull(tags);

        Tag t = tags.get("outputStylesheet");
        assertNotNull(t);

        Attribute attr = t.getAttribute("target");
        assertNull(attr);

        attr = t.getAttribute("converter");
        assertNotNull(attr);
        attr = t.getAttribute("id");
        assertNotNull(attr);
        attr = t.getAttribute("rendered");
        assertNotNull(attr);
        attr = t.getAttribute("value");
        assertNotNull(attr);
        attr = t.getAttribute("library");
        assertNotNull(attr);
        attr = t.getAttribute("name");
        assertNotNull(attr);
        attr = t.getAttribute("binding");
        assertNotNull(attr);

        assertFalse(attr.isRequired());
        assertNotNull(attr.getDescription());
    }


}