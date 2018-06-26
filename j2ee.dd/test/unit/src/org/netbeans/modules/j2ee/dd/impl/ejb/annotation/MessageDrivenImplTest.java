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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 *
 * @author Martin Adamek
 */
public class MessageDrivenImplTest extends CommonTestCase  {

    public MessageDrivenImplTest(String testName) {
        super(testName);
    }
    
    public void testMessageDriven() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerMDB.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "import javax.jms.*;" +
                "@MessageDriven(mappedName = \"jms/CustomerMDB\", activationConfig =  {" +
                "        @ActivationConfigProperty(propertyName = \"acknowledgeMode\", propertyValue = \"Auto-acknowledge\")," +
                "        @ActivationConfigProperty(propertyName = \"destinationType\", propertyValue = \"javax.jms.Queue\")" +
                "    })" +
                "public class CustomerMDB implements MessageListener {" +
                "public void onMessage(Message message) {}" +
                "}");

        final String expectedResult = "foo";

        Void result = createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test CustomerMDB
                MessageDriven messageDriven = (MessageDriven) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getMessageDriven(), "CustomerMDB");
                assertEquals("foo.CustomerMDB", messageDriven.getEjbClass());
                assertEquals("CustomerMDB", messageDriven.getEjbName());
                return null;
            }
        });
    }
    
}
