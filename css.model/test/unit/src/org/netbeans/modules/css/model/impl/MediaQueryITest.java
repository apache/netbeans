/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.model.impl;

import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.Expression;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.MediaBody;
import org.netbeans.modules.css.model.api.MediaExpression;
import org.netbeans.modules.css.model.api.MediaFeature;
import org.netbeans.modules.css.model.api.MediaFeatureValue;
import org.netbeans.modules.css.model.api.MediaQuery;
import org.netbeans.modules.css.model.api.MediaQueryList;
import org.netbeans.modules.css.model.api.MediaQueryOperator;
import org.netbeans.modules.css.model.api.MediaType;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.Page;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class MediaQueryITest extends ModelTestBase {

    public MediaQueryITest(String name) {
        super(name);
    }

    public void testCreateMediaQuery() {
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

        model.runWriteTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                styleSheet.setBody(body);

                //test query the created model

                Body qbody = styleSheet.getBody();
                assertNotNull(qbody);

                List<Media> medias = qbody.getMedias();
                assertNotNull(medias);
                assertEquals(1, medias.size());

                Media qmedia = medias.get(0);
                assertNotNull(qmedia);

                MediaBody mediaBody = qmedia.getMediaBody();
                assertNotNull(mediaBody);

                List<Page> pages = mediaBody.getPages();
                assertNotNull(pages);
                assertEquals(0, pages.size());

                List<Rule> rules = mediaBody.getRules();
                assertNotNull(rules);
                assertEquals(1, rules.size());

                MediaQueryList qmq = qmedia.getMediaQueryList();
                assertNotNull(qmq);

                List<MediaQuery> mqs = qmq.getMediaQueries();
                assertNotNull(mqs);
                assertEquals(1, mqs.size());

                MediaQuery mq1 = mqs.get(0);
                assertNotNull(mq1);

                MediaQueryOperator mqo1 = mq1.getMediaQueryOperator();
                assertNotNull(mqo1);
                assertEquals("ONLY", mqo1.getContent().toString());

                MediaType mt1 = mq1.getMediaType();
                assertNotNull(mt1);
                assertEquals("screen", mt1.getContent().toString());

                Collection<MediaExpression> mes1 = mq1.getMediaExpressions();
                assertNotNull(mes1);
                assertEquals(1, mes1.size());

                MediaExpression me1 = mes1.iterator().next();
                assertNotNull(me1);

                MediaFeature mf = me1.getMediaFeature();
                assertNotNull(mf);
                assertEquals("min-device-width", mf.getContent().toString());

                MediaFeatureValue mfv = me1.getMediaFeatureValue();
                assertNotNull(mfv);

                Expression expr = mfv.getExpression();
                assertNotNull(expr);
                assertEquals("1000px", expr.getContent().toString());


                //        Utils.dump(styleSheet);

            }
        });

        System.out.println(model.getModelSource().toString());
        
        assertEquals("@media ONLY screen AND ( min-device-width : 1000px )  {\n"
                + "\n"
                + ".myclass {\n"
                + "    color: red;\n"
                + "\n"
                + "}\n"
                + "\n\n"
                + "}", model.getModelSource().toString());

    }

    public void testParseMediaQuery() throws BadLocationException, ParseException {
        String source = "@media screen and (color), projection and (color) { div { color: red; } }";

        StyleSheet styleSheet = createStyleSheet(source);
        assertNotNull(styleSheet);

//        Utils.dump(styleSheet);

        Body body = styleSheet.getBody();
        assertNotNull(body);

        List<Media> medias = body.getMedias();
        assertNotNull(medias);
        assertEquals(1, medias.size());

        Media media = medias.get(0);
        assertNotNull(media);

        MediaBody mediaBody = media.getMediaBody();
        assertNotNull(mediaBody);

        List<Page> pages = mediaBody.getPages();
        assertNotNull(pages);
        assertEquals(0, pages.size());

        List<Rule> rules = mediaBody.getRules();
        assertNotNull(rules);
        assertEquals(1, rules.size());

        MediaQueryList mql = media.getMediaQueryList();
        assertNotNull(mql);

        List<MediaQuery> mqs = mql.getMediaQueries();
        assertNotNull(mqs);
        assertEquals(2, mqs.size());

        MediaQuery mq1 = mqs.get(0);
        assertNotNull(mq1);

        MediaQuery mq2 = mqs.get(1);
        assertNotNull(mq2);

        MediaQueryOperator mqo1 = mq1.getMediaQueryOperator();
        assertNull(mqo1);

        MediaType mt1 = mq1.getMediaType();
        assertNotNull(mt1);
        assertEquals("screen", mt1.getContent().toString());

        Collection<MediaExpression> mes1 = mq1.getMediaExpressions();
        assertNotNull(mes1);
        assertEquals(1, mes1.size());

        MediaExpression me1 = mes1.iterator().next();
        assertNotNull(me1);

        MediaFeature mf = me1.getMediaFeature();
        assertNotNull(mf);
        assertEquals("color", mf.getContent().toString());

        MediaFeatureValue mfv = me1.getMediaFeatureValue();
        assertNull(mfv);

    }

    public void testParseMediaQuery2() throws BadLocationException, ParseException {
        String source = "@media aural and (device-aspect-ratio: 16/9) { div { color:red; } }";

        StyleSheet styleSheet = createStyleSheet(source);

        assertNotNull(styleSheet);

//        Utils.dump(styleSheet);

        Body body = styleSheet.getBody();
        assertNotNull(body);

        List<Media> medias = body.getMedias();
        assertNotNull(medias);
        assertEquals(1, medias.size());

        Media media = medias.get(0);
        assertNotNull(media);

        MediaBody mediaBody = media.getMediaBody();
        List<Page> pages = mediaBody.getPages();
        assertNotNull(pages);
        assertEquals(0, pages.size());

        List<Rule> rules = mediaBody.getRules();
        assertNotNull(rules);
        assertEquals(1, rules.size());

        MediaQueryList mql = media.getMediaQueryList();
        assertNotNull(mql);

        List<MediaQuery> mqs = mql.getMediaQueries();
        assertNotNull(mqs);
        assertEquals(1, mqs.size());

        MediaQuery mq1 = mqs.get(0);
        assertNotNull(mq1);

        MediaQueryOperator mqo1 = mq1.getMediaQueryOperator();
        assertNull(mqo1);

        MediaType mt1 = mq1.getMediaType();
        assertNotNull(mt1);
        assertEquals("aural", mt1.getContent().toString());

        Collection<MediaExpression> mes1 = mq1.getMediaExpressions();
        assertNotNull(mes1);
        assertEquals(1, mes1.size());

        MediaExpression me1 = mes1.iterator().next();
        assertNotNull(me1);

        MediaFeature mf = me1.getMediaFeature();
        assertNotNull(mf);
        assertEquals("device-aspect-ratio", mf.getContent().toString());

        MediaFeatureValue mfv = me1.getMediaFeatureValue();
        assertNotNull(mfv);

        Expression expr = mfv.getExpression();
        assertNotNull(expr);
        assertEquals("16/9", expr.getContent().toString());

    }
}
