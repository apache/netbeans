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

package org.netbeans.modules.editor.settings.storage;

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory;
import org.netbeans.modules.editor.settings.storage.api.KeyBindingSettingsFactory;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/** Testing basic functionality of Editor Settings Storage friend API
 * 
 *  @author Martin Roskanin
 */
public class EditorSettingsStorageTest extends NbTestCase {

    public EditorSettingsStorageTest(String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws Exception {
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

    public void testBgColor() {
        checkSingleAttribute("test-bgColor", StyleConstants.Background, 0xAA0000);
    }
    
    public void testForeColor() {
        checkSingleAttribute("test-foreColor", StyleConstants.Foreground, 0x00BB00);
    }
    
    public void testUnderlineColor() {
        checkSingleAttribute("test-underline", StyleConstants.Underline, 0x0000CC);
    }
    
    public void testStrikeThroughColor() {
        checkSingleAttribute("test-strikeThrough", StyleConstants.StrikeThrough, 0xDD0000);
    }
    
    public void testWaveUnderlinedColor() {
        checkSingleAttribute("test-waveUnderlined", EditorStyleConstants.WaveUnderlineColor, 0x00EE00);
    }
    
    public void testAllColors() {
        MimePath mimePath = MimePath.parse("text/x-type-A");
        Lookup lookup = MimeLookup.getLookup(mimePath);
        
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        
        AttributeSet attribs = fcs.getTokenFontColors("test-all");
        assertNotNull("Can't find test-all coloring", attribs);
        assertEquals("Wrong background color", 
            new Color(0x0A0B0C), attribs.getAttribute(StyleConstants.Background));
        assertEquals("Wrong foreground color", 
            new Color(0x0D0E0F), attribs.getAttribute(StyleConstants.Foreground));
        assertEquals("Wrong underline color", 
            new Color(0x010203), attribs.getAttribute(StyleConstants.Underline));
        assertEquals("Wrong strikeThrough color", 
            new Color(0x040506), attribs.getAttribute(StyleConstants.StrikeThrough));
        assertEquals("Wrong waveUnderline color", 
            new Color(0x070809), attribs.getAttribute(EditorStyleConstants.WaveUnderlineColor));
    }

    public void testFont() {
        MimePath mimePath = MimePath.parse("text/x-type-A");
        Lookup lookup = MimeLookup.getLookup(mimePath);
        
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        
        {
        AttributeSet attribs = fcs.getTokenFontColors("test-font-cute-A");
        assertNotNull("Can't find test-font-cute-A coloring", attribs);
        assertEquals("Wrong font family", 
            "Test Font Cute A", attribs.getAttribute(StyleConstants.FontFamily));
        assertEquals("Wrong font size", 
            new Integer(17), attribs.getAttribute(StyleConstants.FontSize));
        assertEquals("Wrong font style (bold)", 
            Boolean.TRUE, attribs.getAttribute(StyleConstants.Bold));
        assertEquals("Wrong font style (italic)", 
            Boolean.FALSE, attribs.getAttribute(StyleConstants.Italic));
        }
        {
        AttributeSet attribs = fcs.getTokenFontColors("test-font-cute-B");
        assertNotNull("Can't find test-font-cute-B coloring", attribs);
        assertEquals("Wrong font family", 
            "Test Font Cute B", attribs.getAttribute(StyleConstants.FontFamily));
        assertEquals("Wrong font size", 
            new Integer(13), attribs.getAttribute(StyleConstants.FontSize));
        assertEquals("Wrong font style (bold)", 
            Boolean.FALSE, attribs.getAttribute(StyleConstants.Bold));
        assertEquals("Wrong font style (italic)", 
            Boolean.TRUE, attribs.getAttribute(StyleConstants.Italic));
        }
    }
    
    private void checkSingleAttribute(String coloringName, Object attributeKey, int rgb) {
        MimePath mimePath = MimePath.parse("text/x-type-A");
        Lookup lookup = MimeLookup.getLookup(mimePath);
        
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        
        AttributeSet attribs = fcs.getTokenFontColors(coloringName);
        assertNotNull("Can't find " + coloringName + " coloring", attribs);
        assertEquals("Wrong " + attributeKey, new Color(rgb), attribs.getAttribute(attributeKey));
    }
    
    public void testSetColors() {
        MimePath mimePath = MimePath.parse("text/x-type-A");
        Lookup lookup = MimeLookup.getLookup(mimePath);
        
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        
        // Check preconditions
        AttributeSet attribs = fcs.getTokenFontColors("test-set-all");
        assertNotNull("Can't find test-set-all coloring", attribs);
        assertEquals("Wrong background color", 
            new Color(0x0A0B0C), attribs.getAttribute(StyleConstants.Background));
        assertEquals("Wrong foreground color", 
            new Color(0x0D0E0F), attribs.getAttribute(StyleConstants.Foreground));
        assertEquals("Wrong underline color", 
            new Color(0x010203), attribs.getAttribute(StyleConstants.Underline));
        assertEquals("Wrong strikeThrough color", 
            new Color(0x040506), attribs.getAttribute(StyleConstants.StrikeThrough));
        assertEquals("Wrong waveUnderline color", 
            new Color(0x070809), attribs.getAttribute(EditorStyleConstants.WaveUnderlineColor));
        
        // Prepare new coloring
        SimpleAttributeSet newAttribs = new SimpleAttributeSet();
        newAttribs.addAttribute(StyleConstants.NameAttribute, "test-set-all");
        newAttribs.addAttribute(StyleConstants.Background, new Color(0xFFFFF0));
        newAttribs.addAttribute(StyleConstants.Foreground, new Color(0xFFFFF1));
        newAttribs.addAttribute(StyleConstants.Underline, new Color(0xFFFFF2));
        newAttribs.addAttribute(StyleConstants.StrikeThrough, new Color(0xFFFFF3));
        newAttribs.addAttribute(EditorStyleConstants.WaveUnderlineColor, new Color(0xFFFFF4));
        
        // Change the coloring
        setOneColoring("text/x-type-A", newAttribs);
        
        // Check that the new attributes were set
        fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        attribs = fcs.getTokenFontColors("test-set-all");
        assertNotNull("Can't find test-set-all coloring", attribs);
        assertEquals("Wrong background color", 
            new Color(0xFFFFF0), attribs.getAttribute(StyleConstants.Background));
        assertEquals("Wrong foreground color", 
            new Color(0xFFFFF1), attribs.getAttribute(StyleConstants.Foreground));
        assertEquals("Wrong underline color", 
            new Color(0xFFFFF2), attribs.getAttribute(StyleConstants.Underline));
        assertEquals("Wrong strikeThrough color", 
            new Color(0xFFFFF3), attribs.getAttribute(StyleConstants.StrikeThrough));
        assertEquals("Wrong waveUnderline color", 
            new Color(0xFFFFF4), attribs.getAttribute(EditorStyleConstants.WaveUnderlineColor));
    }
    
    // Editor settings isn't the only source for events - Test is broken
    public void disabledTestEvents() {
        MimePath mimePath = MimePath.parse("text/x-type-A");
        Lookup lookup = MimeLookup.getLookup(mimePath);
        Lookup.Result<FontColorSettings> result = lookup.lookup(new Lookup.Template<FontColorSettings>(FontColorSettings.class));
        Listener listener = new Listener();
        
        result.addLookupListener(listener);

        Collection<? extends FontColorSettings> instances = result.allInstances();
        assertEquals("Wrong number of FontColorSettings instances", 1, instances.size());
        
        FontColorSettings fcs = instances.iterator().next();
        assertNotNull("Can't find FontColorSettings", fcs);
        
        // Check preconditions
        AttributeSet attribs = fcs.getTokenFontColors("test-events");
        assertNotNull("Can't find test-events coloring", attribs);
        assertEquals("Wrong background color", 
            new Color(0x0A0B0C), attribs.getAttribute(StyleConstants.Background));
        assertEquals("There should be no events yet", 0, listener.eventsCnt);
        
        // Prepare new coloring
        SimpleAttributeSet newAttribs = new SimpleAttributeSet();
        newAttribs.addAttribute(StyleConstants.NameAttribute, "test-events");
        newAttribs.addAttribute(StyleConstants.Background, new Color(0xFFFFF0));

        // Change the coloring
        setOneColoring("text/x-type-A", newAttribs);

        // Check that the event was fired
        assertEquals("Wrong number of events", 1, listener.eventsCnt);
        
        // Check that the new attributes were set
        fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        attribs = fcs.getTokenFontColors("test-events");
        assertNotNull("Can't find test-events coloring", attribs);
        assertEquals("Wrong background color", 
            new Color(0xFFFFF0), attribs.getAttribute(StyleConstants.Background));
    }
    
    public void testColoringsImmutability() {
        MimePath mimePath = MimePath.parse("text/x-type-A");
        Lookup lookup = MimeLookup.getLookup(mimePath);
        
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        
        // Check preconditions
        AttributeSet attribs = fcs.getTokenFontColors("test-immutable");
        assertNotNull("Can't find test-immutable coloring", attribs);
        assertEquals("Wrong background color", 
            new Color(0x0A0B0C), attribs.getAttribute(StyleConstants.Background));
        
        // Prepare new coloring
        SimpleAttributeSet newAttribs = new SimpleAttributeSet();
        newAttribs.addAttribute(StyleConstants.NameAttribute, "test-immutable");
        newAttribs.addAttribute(StyleConstants.Background, new Color(0xFFFFF0));

        // Change the coloring
        setOneColoring("text/x-type-A", newAttribs);

        // Check that the original FontColorSettings has not changed
        attribs = fcs.getTokenFontColors("test-immutable");
        assertNotNull("Can't find test-immutable coloring", attribs);
        assertEquals("Wrong background color", 
            new Color(0x0A0B0C), attribs.getAttribute(StyleConstants.Background));
        
        // Check that the new attributes were set
        fcs = lookup.lookup(FontColorSettings.class);
        assertNotNull("Can't find FontColorSettings", fcs);
        attribs = fcs.getTokenFontColors("test-immutable");
        assertNotNull("Can't find test-immutable coloring", attribs);
        assertEquals("Wrong background color", 
            new Color(0xFFFFF0), attribs.getAttribute(StyleConstants.Background));
    }

    public void testKeyBindingsImmutability() {
        MimePath mimePath = MimePath.parse("text/x-type-A");
        Lookup lookup = MimeLookup.getLookup(mimePath);
        
        KeyBindingSettings kbs = lookup.lookup(KeyBindingSettings.class);
        assertNotNull("Can't find KeyBindingSettings", kbs);
        
        // Check preconditions
        List<MultiKeyBinding> bindings = kbs.getKeyBindings();
        assertNotNull("Key bindings should not be null", bindings);
        MultiKeyBinding kb = findBindingForAction("test-action-1", bindings);
        checkKeyBinding(kb, "O-O");
        
        // Change the coloring
        MultiKeyBinding newKb = new MultiKeyBinding(Utilities.stringToKey("DS-D"), "test-action-1");
        setOneKeyBinding("text/x-type-A", newKb);

        // Check that the original KeyBindingSettings has not changed
        bindings = kbs.getKeyBindings();
        assertNotNull("Key bindings should not be null", bindings);
        kb = findBindingForAction("test-action-1", bindings);
        checkKeyBinding(kb, "O-O");
        
        // Check that the new attributes were set
        kbs = lookup.lookup(KeyBindingSettings.class);
        assertNotNull("Can't find KeyBindingSettings", kbs);
        bindings = kbs.getKeyBindings();
        assertNotNull("Key bindings should not be null", bindings);
        kb = findBindingForAction("test-action-1", bindings);
        checkKeyBinding(kb, "DS-D");
    }
    
    private void setOneColoring(String mimeType, AttributeSet coloring) {
        String coloringName = (String) coloring.getAttribute(StyleConstants.NameAttribute);
        FontColorSettingsFactory fcsf = EditorSettingsImpl.getDefault().getFontColorSettings(new String [] { mimeType });
        Collection<AttributeSet> all = new ArrayList<AttributeSet>(fcsf.getAllFontColors(EditorSettingsImpl.DEFAULT_PROFILE));
        
        for(Iterator<AttributeSet> i = all.iterator(); i.hasNext(); ) {
            AttributeSet attribs = (AttributeSet) i.next();
            String name = (String) attribs.getAttribute(StyleConstants.NameAttribute);
            if (Utilities.compareObjects(name, coloringName)) {
                i.remove();
                break;
            }
        }
        
        all.add(coloring);
        fcsf.setAllFontColors(EditorSettingsImpl.DEFAULT_PROFILE, all);
    }

    private void setOneKeyBinding(String mimeType, MultiKeyBinding keyBinding) {
        KeyBindingSettingsFactory kbsf = EditorSettingsImpl.getDefault().getKeyBindingSettings(new String [] { mimeType });
        List<MultiKeyBinding> all = new ArrayList<MultiKeyBinding>(kbsf.getKeyBindingDefaults(EditorSettingsImpl.DEFAULT_PROFILE));

        for(Iterator<MultiKeyBinding> i = all.iterator(); i.hasNext(); ) {
            MultiKeyBinding kb = (MultiKeyBinding) i.next();
            if (Utilities.compareObjects(kb.getActionName(), keyBinding.getActionName())) {
                i.remove();
                break;
            }
        }

        all.add(keyBinding);
        kbsf.setKeyBindings(EditorSettingsImpl.DEFAULT_PROFILE, all);
    }
    
    private void checkKeyBinding(MultiKeyBinding kb, String... keyStrokes) {
        assertNotNull("Key binding should not be null", kb);
        
        ArrayList<KeyStroke> list = new ArrayList<KeyStroke>();
        for(String s : keyStrokes) {
            KeyStroke stroke = Utilities.stringToKey(s);
            if (stroke != null) {
                list.add(stroke);
            }
        }
        
        assertEquals("Wrong number of key strokes", list.size(), kb.getKeyStrokeCount());
        for(int i = 0; i < list.size(); i++) {
            assertEquals("KeyStroke[" + i + "] is different", 
                list.get(i), kb.getKeyStroke(i));
        }        
    }
    
    private MultiKeyBinding findBindingForAction(String actionName, List<MultiKeyBinding> list){
        for (MultiKeyBinding kb : list){
            if (actionName.equals(kb.getActionName())) {
                return kb;
            }
        }
        return null;
    }
    
    private static final class Listener implements LookupListener {
        
        public int eventsCnt = 0;
        
        public Listener() {
            
        }

        public void resultChanged(LookupEvent ev) {
            eventsCnt++;
        }
    } // End of Listener class
}
