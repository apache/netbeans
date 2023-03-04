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

package org.netbeans.modules.html.parser.model;

import nu.validator.htmlparser.impl.ElementName;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class ElementDescriptorRulesTest extends NbTestCase {

    public ElementDescriptorRulesTest(String name) {
        super(name);
    }

    public void testOpenEndTags() {
        ElementDescriptor div = ElementDescriptor.forElementName(ElementName.DIV);

        assertFalse(ElementDescriptorRules.OPTIONAL_OPEN_TAGS.contains(div));
        assertFalse(ElementDescriptorRules.OPTIONAL_END_TAGS.contains(div));

        ElementDescriptor tbody = ElementDescriptor.forElementName(ElementName.TBODY);
        assertTrue(ElementDescriptorRules.OPTIONAL_OPEN_TAGS.contains(tbody));
        assertTrue(ElementDescriptorRules.OPTIONAL_END_TAGS.contains(tbody));
    }

//    public void testMathMLTags() {
//
//        ElementName sin = ElementName.SIN;
//        assertNull(ElementDescriptor.forElementName(sin));
//
//        assertTrue(ElementDescriptorRules.MATHML_TAG_NAMES.contains(sin.name));
//        assertFalse(ElementDescriptorRules.SVG_TAG_NAMES.contains(sin.name));
//
//    }
//
//    public void testSVGTags() {
//
//        ElementName svg = ElementName.SVG;
//        assertNull(ElementDescriptor.forElementName(svg));
//
//        assertTrue(ElementDescriptorRules.SVG_TAG_NAMES.contains(svg.name));
//        assertFalse(ElementDescriptorRules.MATHML_TAG_NAMES.contains(svg.name));
//
//    }
    

}