/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
