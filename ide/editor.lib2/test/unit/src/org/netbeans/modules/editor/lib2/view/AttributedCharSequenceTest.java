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
package org.netbeans.modules.editor.lib2.view;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author mmetelka
 */
public class AttributedCharSequenceTest extends NbTestCase {
    
    private static AttributeSet[] colorAttrs = {
        AttributesUtilities.createImmutable(StyleConstants.Background, Color.red),
        AttributesUtilities.createImmutable(StyleConstants.Background, Color.green),
        AttributesUtilities.createImmutable(StyleConstants.Background, Color.blue)
    };

    private static AttributeSet[] fontNameAttrs = {
        AttributesUtilities.createImmutable(StyleConstants.FontFamily, "Monospaced"),
        AttributesUtilities.createImmutable(StyleConstants.FontFamily, "Dialog"),
    };
    private static AttributeSet[] fontStyleAttrs = {
        AttributesUtilities.createImmutable(StyleConstants.Italic, true),
        AttributesUtilities.createImmutable(StyleConstants.Bold, true),
    };
    private static AttributeSet[] fontSizeAttrs = {
        AttributesUtilities.createImmutable(StyleConstants.FontSize, 12),
        AttributesUtilities.createImmutable(StyleConstants.FontSize, 15),
    };
    private static AttributeSet[] fontAttrSets = {
        AttributesUtilities.createComposite(fontNameAttrs[0], fontStyleAttrs[0], fontSizeAttrs[0]),
        AttributesUtilities.createComposite(fontNameAttrs[1], fontStyleAttrs[1], fontSizeAttrs[1])
    };

    
    public AttributedCharSequenceTest(String name) {
        super(name);
    }

    public void testRuns() throws Exception {
        AttributeSet defaultAttrs = fontAttrSets[0];
        Map<Attribute,Object> defaultTextAttrs = AttributedCharSequence.translate(defaultAttrs);
        String text = "Nazdar Test";
        AttributedCharSequence acs = new AttributedCharSequence();
        acs.addTextRun(3, AttributedCharSequence.translate(fontNameAttrs[1]));
        try {
            acs.addTextRun(2, AttributedCharSequence.translate(fontNameAttrs[1]));
            throw new IllegalStateException("Expected exception not thrown.");
        } catch (IllegalArgumentException ex) {
            // Expected
        }
        acs.addTextRun(6, AttributedCharSequence.translate(colorAttrs[0]));
        acs.addTextRun(9, AttributedCharSequence.translate(fontSizeAttrs[1]));
        acs.setText(text, defaultTextAttrs);
        
        assertEquals('N', acs.first());
        assertEquals(acs.getRunStart(), 0);
        assertEquals(acs.getRunLimit(), 3);
        assertEquals('a', acs.next());
        assertEquals('z', acs.next());
        assertEquals('d', acs.next());
        assertEquals(acs.getRunStart(), 3);
        assertEquals(acs.getRunLimit(), 6);
        
        assertEquals('t', acs.last());
        assertEquals(acs.getRunStart(), 9);
        assertEquals(acs.getRunLimit(), text.length());

        acs.setIndex(6);
        assertEquals(acs.getRunStart(), 6);
        assertEquals(acs.getRunLimit(), 9);
        acs.setIndex(7);
        assertEquals(acs.getRunStart(), 6);
        assertEquals(acs.getRunLimit(), 9);
        acs.setIndex(5);
        assertEquals(acs.getRunStart(), 3);
        assertEquals(acs.getRunLimit(), 6);
        acs.setIndex(0);
        assertEquals(acs.getRunStart(), 0);
        assertEquals(acs.getRunLimit(), 3);
        acs.setIndex(text.length());
        assertEquals(acs.getRunStart(), 9);
        assertEquals(acs.getRunLimit(), text.length());


        Set<Attribute> allKeysE = new HashSet<Attribute>();
        allKeysE.add(TextAttribute.BACKGROUND);
        allKeysE.add(TextAttribute.FAMILY);
        allKeysE.add(TextAttribute.SIZE);
        allKeysE.add(TextAttribute.POSTURE);
        Set<Attribute> allKeys = acs.getAllAttributeKeys();
        assertEquals(allKeysE, allKeys);
    }
}
