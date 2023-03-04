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
package org.netbeans.modules.css.editor.module.main;

import org.netbeans.modules.parsing.spi.ParseException;

public class AlignmentModuleTest extends CssModuleTestBase {

    public AlignmentModuleTest(String name) {
        super(name);
    }

    public void testProperties() throws ParseException {
        assertPropertyValues("align-items", "inherit", "safe flex-start");
        assertPropertyValues("align-self", "inherit", "stretch", "first baseline");
        assertPropertyValues("align-content", "inherit", "stretch", "safe end");
        assertPropertyValues("justify-content", "inherit", "space-around", "safe right");
        assertPropertyValues("justify-items", "inherit", "right legacy", "legacy right", "legacy", "safe self-start");
        assertPropertyValues("justify-self", "initial", "baseline", "safe end");
        assertPropertyValues("place-content", "space-around stretch", "safe start safe end");
        assertPropertyValues("place-items", "unset", "safe flex-start stretch");
        assertPropertyValues("place-self", "initial", "var(--test)", "first baseline safe right");
        assertPropertyValues("row-gap", "initial", "20%", "30px", "var(--test)", "normal");
        assertPropertyValues("column-gap", "initial", "20%", "30px", "var(--test)", "normal");
        assertPropertyValues("gap", "initial", "20% 30px", "30px 20%", "var(--test)", "normal", "20px normal", "var(--test) 10px");
    }

}
