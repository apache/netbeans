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
package org.netbeans.modules.javascript2.requirejs;

import java.io.File;
import java.io.StringWriter;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.ModelTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class TestCreateModelRequireJs extends ModelTestBase {
    
    public TestCreateModelRequireJs(String testName) {
        super(testName);
    }
    
    public void testCreateModel() throws Exception {
        String file = "require/requirejs.js";
        
        if (!new File(getDataDir(), file).canRead()) {
            return;
        }
        
        FileObject fo = getTestFile(file);

        Model model = getModel(file);
        JsObject requirejs = model.getGlobalObject().getProperty("requirejs");
        
        final StringWriter sw = new StringWriter();
        Model.Printer p = new Model.Printer() {

            @Override
            public void println(String str) {
                // XXX hacks improving the model
                //String real = str;
                //real = real.replaceAll("_L28.ko", "ko");
                sw.append(str).append("\n");
            }
        };
        model.writeObject(p, requirejs, true);
        assertDescriptionMatches(fo, sw.toString(), false, ".model", true);
    }
    
}
