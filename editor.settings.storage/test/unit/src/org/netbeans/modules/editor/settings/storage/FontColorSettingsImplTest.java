/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.settings.storage;

import java.awt.Color;
import java.net.URL;
import java.util.Collection;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.openide.util.Lookup;

/** Testing basic functionality of Editor Settings Storage friend API
 * 
 *  @author Martin Roskanin
 */
public class FontColorSettingsImplTest extends NbTestCase {

    public FontColorSettingsImplTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/test-layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/core/resources/mf-layer.xml"), // for MIMEResolverImpl to work
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );

        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
        // which is needed by Nb EntityCatalog (org.netbeans.core).
        // Also see the test dependencies in project.xml
        Main.initializeURLFactory();
    }

    public void testDefaults() {
        MimePath mimePath = MimePath.parse("text/x-type-B");
        
        checkSingleAttribute(mimePath, "test-inheritance-coloring-1", StyleConstants.Background, 0xAA0000);
        checkSingleAttribute(mimePath, "test-inheritance-coloring-2", StyleConstants.Background, 0xAA0000);

        checkSingleAttribute(mimePath, "test-inheritance-coloring-A", StyleConstants.Background, 0xABCDEF);
        checkSingleAttribute(mimePath, "test-inheritance-coloring-B", StyleConstants.Background, 0xABCDEF);

        checkSingleAttribute(mimePath, "test-inheritance-coloring-X", StyleConstants.Background, 0xBB0000);
        checkSingleAttribute(mimePath, "test-inheritance-coloring-Y", StyleConstants.Background, 0xBB0000);
    }

    public void testAllLanguagesTheCrapWay() {
        Collection<AttributeSet> colorings = EditorSettings.getDefault().getFontColorSettings(new String[0]).getAllFontColors(EditorSettingsImpl.DEFAULT_PROFILE);
        assertNotNull("Can't get colorings for all languages", colorings);
        
        AttributeSet attribs = null;
        for(AttributeSet coloring : colorings) {
            String name = (String) coloring.getAttribute(StyleConstants.NameAttribute);
            if (name != null && name.equals("test-all-languages-set-all")) {
                attribs = coloring;
                break;
            }
        }
        
        assertNotNull("Can't find test-all-languages-set-all coloring", attribs);
        assertEquals("Wrong color", new Color(0x0A0B0C), attribs.getAttribute(StyleConstants.Background));
        assertEquals("Wrong color", new Color(0x0D0E0F), attribs.getAttribute(StyleConstants.Foreground));
        assertEquals("Wrong color", new Color(0x010203), attribs.getAttribute(StyleConstants.Underline));
        assertEquals("Wrong color", new Color(0x040506), attribs.getAttribute(StyleConstants.StrikeThrough));
        assertEquals("Wrong color", new Color(0x070809), attribs.getAttribute(EditorStyleConstants.WaveUnderlineColor));
    }

    public void testAllLanguagesTheStandardWay() {
        checkSingleAttribute(MimePath.EMPTY, "test-all-languages-set-all", StyleConstants.Background, 0x0A0B0C);
        checkSingleAttribute(MimePath.EMPTY, "test-all-languages-set-all", StyleConstants.Foreground, 0x0D0E0F);
        checkSingleAttribute(MimePath.EMPTY, "test-all-languages-set-all", StyleConstants.Underline, 0x010203);
        checkSingleAttribute(MimePath.EMPTY, "test-all-languages-set-all", StyleConstants.StrikeThrough, 0x040506);
        checkSingleAttribute(MimePath.EMPTY, "test-all-languages-set-all", EditorStyleConstants.WaveUnderlineColor, 0x070809);
    }
    
    public void testUserChangesOverrideDefaults() {
        MimePath mimePath = MimePath.parse("text/x-type-C");
        Lookup lookup = MimeLookup.getLookup(mimePath);
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        
        AttributeSet attribs = fcs.getTokenFontColors("test-module-and-user");
        assertNotNull("Can't find 'test-module-and-user' coloring", attribs);
        
        assertNull("Wrong foreColor", attribs.getAttribute(StyleConstants.Foreground));
        assertEquals("Wrong bgColor", new Color(0xCC0000), attribs.getAttribute(StyleConstants.Background));
        assertNull("Wrong font family", attribs.getAttribute(StyleConstants.FontFamily));
        assertNull("Wrong font size", attribs.getAttribute(StyleConstants.FontSize));
        assertNull("Wrong bold", attribs.getAttribute(StyleConstants.Bold));
        assertNull("Wrong italic", attribs.getAttribute(StyleConstants.Italic));
        
        checkSingleAttribute(mimePath, "test-module", StyleConstants.Foreground, 0x000011);
        checkSingleAttribute(mimePath, "test-module", StyleConstants.Background, 0x000022);
        checkSingleAttribute(mimePath, "test-module", StyleConstants.Bold, false);
        checkSingleAttribute(mimePath, "test-module", StyleConstants.Italic, true);
    }
    
    private void checkSingleAttribute(MimePath mimePath, String coloringName, Object attributeKey, int rgb) {
        Lookup lookup = MimeLookup.getLookup(mimePath);
        
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        
        AttributeSet attribs = fcs.getTokenFontColors(coloringName);
        assertNotNull("Can't find " + coloringName + " coloring", attribs);
        assertEquals("Wrong color", new Color(rgb), attribs.getAttribute(attributeKey));
    }
    
    private void checkSingleAttribute(MimePath mimePath, String coloringName, Object attributeKey, Object attributeValue) {
        Lookup lookup = MimeLookup.getLookup(mimePath);
        
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        
        AttributeSet attribs = fcs.getTokenFontColors(coloringName);
        assertNotNull("Can't find " + coloringName + " coloring", attribs);
        assertEquals("Wrong value of '" + attributeKey + "'", attributeValue, attribs.getAttribute(attributeKey));
    }
    
}
