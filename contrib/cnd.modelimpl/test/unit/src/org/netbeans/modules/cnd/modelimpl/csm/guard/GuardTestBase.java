/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.modelimpl.csm.guard;

import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 * A common base class for guard based tests
 */
public class GuardTestBase  extends TraceModelTestBase {

    public GuardTestBase(String testName) {
        super(testName);
    }

    protected void parse(String... fileNames) throws Exception {
        performModelTest(tansformParameters(fileNames), null, System.err);
    }

    protected String[] tansformParameters(String[] files) {
        String[] result = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            if (files[i].startsWith("-")) {
                result[i] = files[i];
            } else {
                result[i] = getDataFile(files[i]).getAbsolutePath();
            }
        }
        return result;
    }

//    protected String getClassName(Class cls){
//        String s = cls.getName();
//        return s.substring(s.lastIndexOf('.')+1);
//    }

}
