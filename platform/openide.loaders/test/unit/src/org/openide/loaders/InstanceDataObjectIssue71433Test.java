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

package org.openide.loaders;

import org.netbeans.junit.NbTestCase;

/** Test behavior of method escapeAndCut. This is important for window system persistence
 * which uses InstanceDataObject to serialize TopComponent instance.
 *
 * @author Marek Slama
 */
public class InstanceDataObjectIssue71433Test extends NbTestCase {

    public InstanceDataObjectIssue71433Test(String name) {
        super (name);
    }

    /** escapeAndCut must create the same result when
     * 1.espaceAndCut is used
     * 2.espaceAndCut, unescape, espaceAndCut is used
     */
    public void testEscapeAndCut () throws Exception {
        //This special case caused escaped string to be cut just after '#' char
        //from beginning.
        String testName = "com.pantometrics.editor.form.display.PantoEditTopComponent";
        String resultName1 = InstanceDataObject.escapeAndCut(testName);
        String resultName2 = InstanceDataObject.unescape(resultName1);
        resultName2 = InstanceDataObject.escapeAndCut(resultName2);
        
        assertEquals ("Must be the same", resultName1, resultName2);
    }
    
}
