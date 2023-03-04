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
package org.netbeans.test.j2ee.addmethod;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.test.j2ee.*;
import org.netbeans.test.j2ee.lib.Utils;

/**
 *
 * @author lm97939
 */
public abstract class AddMethodBase extends J2eeTestCase {

    protected String beanName;
    protected String editorPopup;
    protected String dialogTitle;
    protected boolean isDDModified = false;
    protected String toSearchInEditor;
    protected boolean saveFile = false;

    /** Creates a new instance of AddMethodTest */
    public AddMethodBase(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
    }

    protected void compareFiles() throws IOException {
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
        Utils utils = new Utils(this);
        String beanNames[] = {beanName + "Bean.java",
            beanName + "Local.java",
            beanName + "LocalBusiness.java",
            beanName + "LocalHome.java",
            beanName + "Remote.java",
            beanName + "RemoteBusiness.java",
            beanName + "RemoteHome.java",
        };
        File EJB_PROJECT_FILE = new File(new File(getDataDir(), EJBValidation.EAR_PROJECT_NAME), EJBValidation.EAR_PROJECT_NAME + "-ejb");
        utils.assertFiles(new File(EJB_PROJECT_FILE, "src/java/test"), beanNames, getName() + "_");
        String ddNames[] = {
            "ejb-jar.xml",
            "glassfish-ejb-jar.xml"
        };
        utils.assertFiles(new File(EJB_PROJECT_FILE, "src/conf"), ddNames, isDDModified ? getName() + "_" : "");
    }
}
