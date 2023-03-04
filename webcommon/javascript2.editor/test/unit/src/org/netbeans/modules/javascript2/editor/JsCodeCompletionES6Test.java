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
package org.netbeans.modules.javascript2.editor;

/**
 *
 * @author Petr Pisl
 */
public class JsCodeCompletionES6Test extends JsCodeCompletionBase {

    public JsCodeCompletionES6Test(String testName) {
        super(testName);
    }
    
    public void testClassName01() throws Exception {
        // the name of Polygon class has to be offered
        checkCompletion("testfiles/completion/ecmascript6/classes/class01.js", "var p = new P^", false);
    }
    
    public void testClassName02() throws Exception {
        // the constructor should not be offered, height  and width has to be offered
        checkCompletion("testfiles/completion/ecmascript6/classes/class01.js", "console.log(pol.^height);", false);
    }
    
    public void testClassStaticMethod01() throws Exception {
        // has to offer the static distance method
        checkCompletion("testfiles/completion/ecmascript6/classes/class04.js", "Point.^", false);
    }
    
    
    
}
