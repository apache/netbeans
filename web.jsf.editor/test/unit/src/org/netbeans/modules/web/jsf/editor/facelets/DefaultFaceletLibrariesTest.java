/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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