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
package org.netbeans.modules.css.model.impl;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.css.lib.api.NodeType;

/**
 *
 * @author marekfukala
 */
public class UtilsTest extends NbTestCase {

    public UtilsTest(String name) {
        super(name);
    }

    public void testGetImplementingClassNameForNodeType() {
        assertEquals("org.netbeans.modules.css.model.impl.CharSetI",
                Utils.getImplementingClassNameForNodeType(NodeType.charSet));
        assertEquals("org.netbeans.modules.css.model.impl.ImportItemI",
                Utils.getImplementingClassNameForNodeType(NodeType.importItem));
        assertEquals("org.netbeans.modules.css.model.impl.WsI",
                Utils.getImplementingClassNameForNodeType(NodeType.ws));

        //element name with uderscores
        assertEquals("org.netbeans.modules.css.model.impl.GenericAtRuleI",
                Utils.getImplementingClassNameForNodeType(NodeType.generic_at_rule));

        boolean failed = false;
        try {
            assertEquals("org.netbeans.modules.css.model.impl.RuleDI",
                    Utils.getImplementingClassNameForNodeType("rule_"));
        } catch (AssertionError ae) {
            failed = true;
        }
        
        assertTrue(failed);
        
        failed = false;
        try {
            assertEquals("org.netbeans.modules.css.model.impl.RuleDI",
                    Utils.getImplementingClassNameForNodeType("rule__d"));
        } catch (AssertionError ae) {
            failed = true;
        }
        
        assertTrue(failed);

    }
}
