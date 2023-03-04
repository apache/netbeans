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
package org.netbeans.modules.htmlui;

import java.util.HashMap;
import javax.swing.Action;
import org.netbeans.api.htmlui.OpenHTMLRegistration;
import org.netbeans.modules.htmlui.Pages;
import org.openide.awt.ActionID;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author jtulach
 */
public class OpenHTMLRegistrationTest {
    
    public OpenHTMLRegistrationTest() {
    }

    @ActionID(category = "Test", id="html.test")
    @OpenHTMLRegistration(displayName = "Open me!",
        iconBase = "x.png",
        url = "empty.html",
        techIds = { "uno", "duo", "tre" }
    )
    public static void main() {
    }
    
    @Test(timeOut = 9000)
    public void verifyRegistered() {
        final String path = "Actions/Test/html-test.instance";
        final FileObject fo = FileUtil.getConfigFile(path);
        assertNotNull(fo, "Registration found");
        Action a = FileUtil.getConfigObject(path, Action.class);
        assertNotNull(a, "Action found");
        assertEquals(a.getValue(Action.NAME), "Open me!");
        
        assertEquals(fo.getAttribute("class"), OpenHTMLRegistrationTest.class.getCanonicalName(), "Fully qualified name");
        assertEquals(fo.getAttribute("method"), "main");
        
        class FOMap extends HashMap<String,Object> {

            @Override
            public Object get(Object key) {
                return fo.getAttribute(key.toString());
            }
        }

        Pages.OpenHtmlAction r = new Pages.OpenHtmlAction(new FOMap());
        Object[] arr = r.getTechIds();
        assertEquals(arr.length, 3, "Three different ids");
        assertEquals(arr[0], "uno");
        assertEquals(arr[1], "duo");
        assertEquals(arr[2], "tre");
    }
}
