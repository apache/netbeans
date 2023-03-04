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

package org.netbeans.modules.gradle.actions;

import org.netbeans.modules.gradle.api.execute.ActionMapping;
import java.io.IOException;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author Laszlo Kishalmi
 */
public class ActionMappingScannerTest {

    public ActionMappingScannerTest() {
    }

    @Test
    public void testVisitDocument() throws ParserConfigurationException, SAXException, IOException {
        Set<ActionMapping> mappings = ActionMappingScanner.loadMappings(ActionMappingScanner.class.getResourceAsStream("action-mapping.xml"));
        assertTrue(mappings.size() > 0);
    }


}
