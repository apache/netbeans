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

import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.MediaBody;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class MediaITest extends ModelTestBase {

    public MediaITest(String name) {
        super(name);
    }

    public void testResolvedProperty() throws BadLocationException, ParseException {
        String code = "@media screen {}";
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);

        Media m = styleSheet.getBody().getMedias().get(0);
        assertNotNull(m);
        assertNull(m.getMediaBody()); //no mediaBody element

        ElementFactory ef = model.getElementFactory();
        MediaBody mb = ef.createMediaBody();
        mb.addRule(ef.createRule(ef.createSelectorsGroup(ef.createSelector(".clz")),
                ef.createDeclarations(ef.createPropertyDeclaration(ef.createProperty("color"),
                ef.createPropertyValue(ef.createExpression("red")), false))));
        m.setMediaBody(mb);

        assertEquals("@media screen {\n"
                + "\n"
                + ".clz {\n"
                + "    color: red;\n"
                + "\n"
                + "}\n"
                + "\n"
                + "\n"
                + "}", model.getModelSource().toString());

    }

}
