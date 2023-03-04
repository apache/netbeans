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
package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Tomas Zezula
 */
public class FreeformFileEncodingQueryImplTest extends TestBase {

    public FreeformFileEncodingQueryImplTest(final String name) {
        super(name);
    }

   

    public void testNonExistentExternalRoot() throws Exception {
        clearWorkDir();
        final File d = getWorkDir();
        final File proj = new File (d,"proj");
        final File extSrcDir = new File (d,"ext");
        proj.mkdir();
        AntProjectHelper helper = FreeformProjectGenerator.createProject(proj, proj, "prj", null);
        Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());

        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element sf = (Element) data.insertBefore(doc.createElementNS(Util.NAMESPACE, "folders"), XMLUtil.findElement(data, "view", Util.NAMESPACE)).
                appendChild(doc.createElementNS(Util.NAMESPACE, "source-folder"));
        sf.appendChild(doc.createElementNS(Util.NAMESPACE, "label")).appendChild(doc.createTextNode("Sources"));
        sf.appendChild(doc.createElementNS(Util.NAMESPACE, "location")).appendChild(doc.createTextNode("../ext"));
        final Charset expectedCharset = StandardCharsets.UTF_8;
        sf.appendChild(doc.createElementNS(Util.NAMESPACE,"encoding")).appendChild(doc.createTextNode(expectedCharset.name()));
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        final FileEncodingQueryImplementation impl = p.getLookup().lookup(FileEncodingQueryImplementation.class);
        assertNotNull(impl);
        impl.getEncoding(p.getProjectDirectory());  //Initialize FEQImpl cache to cause cache inconsistence after ../ext is created
        final FileObject extFo = FileUtil.createFolder(extSrcDir);
        final Charset result = impl.getEncoding(extFo);
        assertEquals(expectedCharset, result);

    }
}
