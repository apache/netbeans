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
package org.netbeans.modules.nbjavac.api;

import java.util.ResourceBundle;
import javax.lang.model.SourceVersion;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author mbien
 */
public class NBJavacBrandingTest {

    @Test
    public void testModuleDescription() {
        String desc = ResourceBundle.getBundle("org.netbeans.modules.nbjavac.api.Bundle")
                                    .getString("OpenIDE-Module-Long-Description");
        String expected = "JDK-" + SourceVersion.latest().ordinal();
        assertTrue(
                "Wrong JDK version in module description?"
              + "\nexpected token: " + expected
              + "\nmodule descripton: " + desc,
                desc.contains(expected)
        );
    }

}
