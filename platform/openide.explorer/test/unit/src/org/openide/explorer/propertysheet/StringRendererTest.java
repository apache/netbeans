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
package org.openide.explorer.propertysheet;

import static java.util.stream.Collectors.joining;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.stream.Stream;
import javax.swing.JLabel;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

public class StringRendererTest extends NbTestCase {

    private static final int MAX_DISPLAYABLE = 512;
    private static final String TEXT = "a";
    private static final String ESCAPED_TEXT = "\\u0061";
    private static final String MAX_LENGTH_TEXT = Stream.generate(() -> TEXT)
            .limit(MAX_DISPLAYABLE)
            .collect(joining());
    private static final String TOO_LONG_TEXT = Stream.generate(() -> TEXT)
            .limit(MAX_DISPLAYABLE + 1)
            .collect(joining());

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(StringRendererTest.class);
    }

    public StringRendererTest(String name) {
        super(name);
    }

    private class TestFont extends Font {

        public TestFont() {
            super(new JLabel().getFont());
        }

        @Override
        public boolean canDisplay(char c) {
            return displayableChar;
        }

        @Override
        public boolean canDisplay(int codePoint) {
            return displayableCodePoint;
        }
    }

    private boolean displayableChar;
    private boolean displayableCodePoint;
    private final Font font = new TestFont();

    private JLabel stringRenderer;

    @Override
    protected void setUp() {
        ReusablePropertyEnv env = new ReusablePropertyEnv();
        RendererFactory factory = new RendererFactory(true, env, env.getReusablePropertyModel());
        stringRenderer = (JLabel) factory.getStringRenderer();

        displayableChar = true;
        displayableCodePoint = true;
        stringRenderer.setFont(font);
    }

    private void assertSettingText(String input, String expected) {
        stringRenderer.setText(input);

        assertEquals(expected, stringRenderer.getText());
    }

    public void testConvertsNullTextToEmptyString() {
        assertSettingText(null, "");
    }

    public void testDisplayableTextIsUnchanged() {
        assertSettingText(MAX_LENGTH_TEXT, MAX_LENGTH_TEXT);
    }

    public void testTruncatesToMaxLengthText() {
        assertSettingText(TOO_LONG_TEXT, MAX_LENGTH_TEXT);
    }

    public void testShouldTransformTabsToSpaces() {
        assertSettingText("\t", "        ");
    }

    public void testRemovesNewLines() {
        assertSettingText("\n", "");
    }

    public void testRemovesCarriageReturns() {
        assertSettingText("\r", "");
    }

    public void testRemovesLineFeeds() {
        assertSettingText("\r", "");
    }

    public void testEscapesBackspaces() {
        assertSettingText("\b", "\\b");
    }

    public void testEscapesFormFeeds() {
        assertSettingText("\f", "\\f");
    }

    public void testEscapesUndisplayableText() {
        displayableChar = false;
        displayableCodePoint = false;

        assertSettingText(TEXT, ESCAPED_TEXT);
    }

    public void testDoesNotEscapeDisplayableCodePoint() {
        displayableChar = false;

        assertSettingText(TEXT, TEXT);
    }
}
