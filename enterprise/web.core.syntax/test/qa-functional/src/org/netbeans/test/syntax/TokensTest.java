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
package org.netbeans.test.syntax;

import java.io.File;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.lib.BasicTokensTest;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Jindrich Sedek
 */
public class TokensTest extends BasicTokensTest {
    private static boolean opened = false;
    
    public TokensTest(String name) {
        super(name);
    }

    protected boolean generateGoldenFiles() {
        return false;
    }

    public static Test suite() {
        return NbModuleSuite.allModules(TokensTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if(!opened){
            openAllTokensFiles();
            opened = true;
            Thread.sleep(5000);
        }
    }


    public void testHTML() {
        testRun("tokensHTML.html");
    }

    public void testJSP() {
        testRun("tokensTest.jsp");
    }

    public void testTag() {
        testRun("tokensTag.tag");
    }

    public void testTagX() {
        testRun("tokensTagX.tagx");
    }

    public void testJSPX() {
        testRun("tokensJSPX.jspx");
    }

    public void testJSPF() {
        testRun("tokensJSPF.jspf");
    }

    public void testCSS() {
        testRun("tokensCSS.css");
    }

    private void openAllTokensFiles() throws DataObjectNotFoundException {
        File dir = new File(getDataDir(), "tokens");
        for (File file : dir.listFiles()) {
            DataObject dataObj = DataObject.find(FileUtil.toFileObject(file));
            EditorCookie ed = dataObj.getCookie(EditorCookie.class);
            ed.open();
        }
    }
}
