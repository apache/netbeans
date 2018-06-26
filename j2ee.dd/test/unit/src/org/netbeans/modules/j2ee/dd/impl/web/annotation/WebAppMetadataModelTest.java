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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.dd.impl.web.annotation;

import java.io.File;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class WebAppMetadataModelTest extends WebAppTestCase {

    // XXX also test version in testDelegation

    public WebAppMetadataModelTest(String name) {
        super(name);
    }

    public void testDelegation() throws Exception {
        FileObject webXmlFO = TestUtilities.copyStringToFileObject(srcFO, "web.xml",
                "<?xml version='1.0' encoding='UTF-8'?>" +
                "<web-app version='2.5' xmlns='http://java.sun.com/xml/ns/javaee' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd'>" +
                "   <servlet>" +
                "       <servlet-name>Servlet</servlet-name>" +
                "       <servlet-class>org.example.Servlet</servlet-class>" +
                "   </servlet>" +
                "</web-app>");
        File webXmlFile = FileUtil.toFile(webXmlFO);
        MetadataUnit metadataUnit = createMetadataUnit(webXmlFile);
        MetadataModel model = createModel(metadataUnit);
        MetadataModelAction<WebAppMetadata, Integer> action = new MetadataModelAction<WebAppMetadata, Integer>() {
            public Integer run(WebAppMetadata metadata) throws Exception {
                WebApp webApp = metadata.getRoot();
                return (webApp != null) ? webApp.getServlet().length : 0;
            }
        };
        assertEquals(1, model.runReadAction(action));
        metadataUnit.changeDeploymentDescriptor(new File("/foo/bar/baz/web.xml"));
        assertEquals(0, model.runReadAction(action));
        metadataUnit.changeDeploymentDescriptor(null);
        assertEquals(0, model.runReadAction(action));
        metadataUnit.changeDeploymentDescriptor(webXmlFile);
        assertEquals(1, model.runReadAction(action));
    }
}
