/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.nio.charset.Charset;
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
        final Charset expectedCharset = Charset.forName("UTF-8");
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
