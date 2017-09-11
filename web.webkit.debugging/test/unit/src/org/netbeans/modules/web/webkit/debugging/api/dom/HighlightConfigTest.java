/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.webkit.debugging.api.dom;

import java.awt.Color;
import org.json.simple.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests of class {@code HighlightConfig}.
 *
 * @author Jan Stola
 */
public class HighlightConfigTest {
    
    /**
     * Test of {@code toJSONObject} method.
     */
    @Test
    public void testToJSONObject() {
        Color content = Color.RED;
        Color padding = Color.GREEN;
        Color border = Color.BLUE;
        Color margin = Color.BLACK;
        HighlightConfig instance = new HighlightConfig();
        instance.showInfo = true;
        instance.contentColor = content;
        instance.paddingColor = padding;
        instance.borderColor = border;
        instance.marginColor = margin;
        
        JSONObject result = instance.toJSONObject();
        assertNotNull(result);

        Object showInfo = result.get("showInfo"); // NOI18N
        assertEquals(Boolean.TRUE, showInfo);
        
        Object contentColor = result.get("contentColor"); // NOI18N
        assertTrue(contentColor instanceof JSONObject);
        JSONObject contentResult = (JSONObject)contentColor;
        check(content, contentResult);

        Object paddingColor = result.get("paddingColor"); // NOI18N
        assertTrue(paddingColor instanceof JSONObject);
        JSONObject paddingResult = (JSONObject)paddingColor;
        check(padding, paddingResult);

        Object borderColor = result.get("borderColor"); // NOI18N
        assertTrue(borderColor instanceof JSONObject);
        JSONObject borderResult = (JSONObject)borderColor;
        check(border, borderResult);

        Object marginColor = result.get("marginColor"); // NOI18N
        assertTrue(marginColor instanceof JSONObject);
        JSONObject marginResult = (JSONObject)marginColor;
        check(margin, marginResult);
    }

    private static void check(Color color, JSONObject json) {
        Object red = json.get("r"); // NOI18N
        int redExpected = color.getRed();
        assertEquals(redExpected, red);

        Object green = json.get("g"); // NOI18N
        int greenExpected = color.getGreen();
        assertEquals(greenExpected, green);

        Object blue = json.get("b"); // NOI18N
        int blueExpected = color.getBlue();
        assertEquals(blueExpected, blue);

        Object alpha = json.get("a"); // NOI18N
        int alphaExpected = color.getAlpha();
        if (alphaExpected == 255) {
            assertNull(alpha);
        } else {
            assertNotNull(alpha);
            assertTrue(alpha instanceof Number);
            double alphaValue = ((Number)alpha).doubleValue()*255;
            assertEquals(alphaExpected, Math.round(alphaValue));
        }
    }

    /**
     * Test of {@code colorToRGBA} method.
     */
    @Test
    public void testColorToRGBA1() {
        int red = 23;
        int green = 37;
        int blue = 211;
        Color color = new Color(red, green, blue);
        JSONObject result = HighlightConfig.colorToRGBA(color);
        assertNotNull(result);
        check(color, result);
    }

    /**
     * Test of {@code colorToRGBA} method.
     */
    @Test
    public void testColorToRGBA2() {
        int red = 177;
        int green = 91;
        int blue = 57;
        int alpha = 51;
        Color color = new Color(red, green, blue, alpha);
        JSONObject result = HighlightConfig.colorToRGBA(color);
        assertNotNull(result);
        check(color, result);
    }

}
