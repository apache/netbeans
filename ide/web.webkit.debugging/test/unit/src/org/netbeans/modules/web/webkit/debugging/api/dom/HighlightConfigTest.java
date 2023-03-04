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
