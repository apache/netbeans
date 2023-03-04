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

import java.io.IOException;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.MediaBody;
import org.netbeans.modules.css.model.api.MediaExpression;
import org.netbeans.modules.css.model.api.MediaQuery;
import org.netbeans.modules.css.model.api.MediaQueryList;
import org.netbeans.modules.css.model.api.MediaQueryOperator;
import org.netbeans.modules.css.model.api.MediaType;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class ModelElementTest extends ModelTestBase {

    public ModelElementTest(String name) {
        super(name);
    }

//    public void testAcceptVisitorGeneric_speed() throws IOException {
//        FileObject file = getTestFile("testfiles/bootstrap.css");
//        String content = file.asText();
//
//        System.out.println("Testing performance on " + file.getNameExt() + " ... ");
//
//        long a = System.currentTimeMillis();
//        CssParserResult result = TestUtil.parse(content);
//        long b = System.currentTimeMillis();
//        System.out.println("file parsing took " + (b - a) + "ms.");
//
//        Model model = createModel(result);
//
//        long c = System.currentTimeMillis();
//        System.out.println("model creation took " + (c - b) + "ms.");
//
//        StyleSheet s = getStyleSheet(model);
//        assertNotNull(s);
//
//        ModelVisitor visitor = new ModelVisitor.Adapter() {
//            @Override
//            public void visitRule(Rule rule) {
//                //no-op
//            }
//        };
//
//        s.accept(visitor);
//        long d = System.currentTimeMillis();
//
//        System.out.println("visiting took " + (d - c) + "ms.");
//
//    }

    public void testAcceptVisitorOfNewElements() {
        Model model = createModel();
        ElementFactory f = model.getElementFactory();

        MediaQueryOperator mqo = f.createMediaQueryOperator("ONLY");
        MediaType mt = f.createMediaType("screen");

        MediaExpression me = f.createMediaExpression(
                f.createMediaFeature("min-device-width"),
                f.createMediaFeatureValue(f.createExpression("1000px")));

        MediaQuery mq = f.createMediaQuery(mqo, mt, me);
        MediaQueryList mql = f.createMediaQueryList(mq);

        Rule rule = f.createRule(
                f.createSelectorsGroup(f.createSelector(".myclass")),
                f.createDeclarations(
                f.createPropertyDeclaration(
                f.createProperty("color"),
                f.createPropertyValue(f.createExpression("red")), false)));

        MediaBody mediaBody = f.createMediaBody(rule);
        Media media = f.createMedia(mql, mediaBody);
        final Body body = f.createBody();
        body.addMedia(media);

        model.runReadTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {
                styleSheet.accept(new ModelVisitor() {

                    @Override
                    public void visitRule(Rule rule) {
                    }

                    @Override
                    public void visitMedia(Media media) {
                    }
                    
                });
            }
        });
        

    }
}
