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


package org.netbeans.modules.properties.syntax;


import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import javax.swing.KeyStroke;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Coloring;
import org.netbeans.modules.properties.TableViewSettings;

import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * TableViewSettings that delegates to text editor module settings.
 *
 * @author Peter Zavadsky, refactored by Petr Kuzel
 * @see org.netbeans.modules.propeties.BundleEditPanel
 */
public class EditorSettingsCopy extends TableViewSettings {

    /** Singleton instance of <code>EditorSettingsCopy</code>. */
    private static EditorSettingsCopy editorSettingsCopy;
    
    private Font font;
    /** Value of key color retrieved from settings in editor module. */
    private Color keyColor;
    /** Value of key background retrieved from settings in editor module. */
    private Color keyBackground;
    /** Value of value color retrieved from settings in editor module. */
    private Color valueColor;
    /** Value of value background retrieved from settings in editor module. */
    private Color valueBackground;
    /** Value of highlight color retrieved from settings in editor module. */
    private Color highlightColor;
    /** Value of highlight bacground retrieved from settings in editor module. */
    private Color highlightBackground;
    /** Value of shadow color retrieved from settings in editor module. */
    private Color shadowColor;
    
    /** Key strokes for find next action rerieved from editor module. */
    private KeyStroke[] keyStrokesFindNext;
    /** Key strokes for find previous action retrieved from editor module. */
    private KeyStroke[] keyStrokesFindPrevious;
    /** Key strokes for toggle search highlight action retrieved from editor module. */
    private KeyStroke[] keyStrokesToggleHighlight;

    /** Support for property changes. */
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
        
    /** Flag indicating whether the settings are prepared. */
    private boolean prepared = false;
    
    private Lookup.Result<FontColorSettings> fontsColors = null;
    private final LookupListener fontsColorsTracker = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            updateColors();
        }
    };
    
    private Lookup.Result<KeyBindingSettings> keybindings = null;
    private final LookupListener keybindingsTracker = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            updateKeyStrokes();
        }
    };
    
    /** Private constructor. */
    private EditorSettingsCopy() {
    }

    
    /** Implements <code>EditorSetings</code> interface method. */
    public Color getKeyColor() {
        prepareSettings();
        if(keyColor == null) {
            keyColor = TableViewSettings.KEY_DEFAULT_COLOR;
        }
        
        return keyColor;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */    
    public Color getKeyBackground() {
        prepareSettings();
        if(keyBackground == null) {
            keyBackground = TableViewSettings.KEY_DEFAULT_BACKGROUND;
        }
        
        return keyBackground;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */
    public Color getValueColor() {
        prepareSettings();
        if(valueColor == null) {
            valueColor = TableViewSettings.VALUE_DEFAULT_COLOR;
        }
        
        return valueColor;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */
    public Color getValueBackground() {
        prepareSettings();
        if(valueBackground == null) {
            valueBackground = TableViewSettings.VALUE_DEFAULT_BACKGROUND;
        }
        
        return valueBackground;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */
    public Color getHighlightColor() {
        prepareSettings();
        if(highlightColor == null) {
            highlightColor = TableViewSettings.HIGHLIGHT_DEFAULT_COLOR;
        }
        
        return highlightColor;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */ 
    public Color getHighlightBackground() {
        prepareSettings();
        if(highlightBackground == null) {
            highlightBackground = TableViewSettings.HIGHLIGHT_DEFAULT_BACKGROUND;
        }
        
        return highlightBackground;
    }
    
    /** Implements <code>EditorSetings</code> inaterface method. */ 
    public Color getShadowColor() {
        prepareSettings();
        if(shadowColor == null) {
            shadowColor = TableViewSettings.SHADOW_DEFAULT_COLOR;
        }
        
        return shadowColor;
    }

    public Font getFont() {
        prepareSettings();
        return font;
    }    


    /** Implements <code>EditorSetings</code> interface method. */     
    public KeyStroke[] getKeyStrokesFindNext() {
        prepareSettings();
        if(keyStrokesFindNext == null || keyStrokesFindNext.length == 0) {
            keyStrokesFindNext = TableViewSettings.FIND_NEXT_DEFAULT_KEYSTROKES;
        }
        
        return keyStrokesFindNext;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */     
    public KeyStroke[] getKeyStrokesFindPrevious() {
        prepareSettings();
        if(keyStrokesFindPrevious == null || keyStrokesFindPrevious.length == 0) {
            keyStrokesFindPrevious = TableViewSettings.FIND_PREVIOUS_DEFAULT_KEYSTROKES;
        }
        
        return keyStrokesFindPrevious;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */
    public KeyStroke[] getKeyStrokesToggleHighlight() {
        prepareSettings();
        if(keyStrokesToggleHighlight == null || keyStrokesToggleHighlight.length == 0) {
            keyStrokesToggleHighlight = TableViewSettings.TOGGLE_HIGHLIGHT_DEFAULT_KEYSTROKES;
        }
        
        return keyStrokesToggleHighlight;
    }

    /** Implements <code>EditorSetings</code> interface method. */    
    public void settingsUpdated() {
        if (prepared) {
        support.firePropertyChange(new PropertyChangeEvent(this, null, null, null));
    }
    }

    /** Implements <code>EditorSetings</code> interface method. */     
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /** Implements <code>EditorSetings</code> interface method. */    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /** 
     * Gets only instance of <code>EditorSettindsCopy</code> that is also
     * registered at layer to access it declaratively.
     */
    public static synchronized EditorSettingsCopy getLayerInstance() {
            if(editorSettingsCopy == null) {
                editorSettingsCopy = new EditorSettingsCopy();
            }

        return editorSettingsCopy;
    }

    /** Prepares settings. */
    private void prepareSettings() {
        if (prepared) return;
        prepared = true;

        // Set listening on changes of settings.
        fontsColors = MimeLookup.getLookup(PropertiesKit.PROPERTIES_MIME_TYPE).lookupResult(FontColorSettings.class);
        fontsColors.addLookupListener(WeakListeners.create(LookupListener.class, fontsColorsTracker, fontsColors));
        
        keybindings = MimeLookup.getLookup(PropertiesKit.PROPERTIES_MIME_TYPE).lookupResult(KeyBindingSettings.class);
        keybindings.addLookupListener(WeakListeners.create(LookupListener.class, keybindingsTracker, keybindings));
        
        // Init settings.                            
        updateColors();
        updateKeyStrokes();
    }
    
    /** Updates colors.
     * @return <code>true</code> if colors updated succesfully or <code>false</code> otherwise. */
    private void updateColors() {
        FontColorSettings fcs = fontsColors.allInstances().iterator().next();
        String namePrefix = PropertiesTokenContext.context.getNamePrefix();
        
        // Update colors.
        Coloring keyColoring = Coloring.fromAttributeSet(fcs.getTokenFontColors(namePrefix + PropertiesTokenContext.KEY.getName()));
        keyColor = keyColoring.getForeColor();
        keyBackground = keyColoring.getBackColor();
        
        Coloring valueColoring = Coloring.fromAttributeSet(fcs.getTokenFontColors(namePrefix + PropertiesTokenContext.VALUE.getName()));
        valueColor = valueColoring.getForeColor();
        valueBackground = valueColoring.getBackColor();
        
        Coloring highlightColoring = Coloring.fromAttributeSet(fcs.getFontColors(FontColorNames.HIGHLIGHT_SEARCH_COLORING));
        highlightColor = highlightColoring.getForeColor();
        highlightBackground = highlightColoring.getBackColor();

        // If there is not the colors specified use default inherited colors.
        Coloring defaultColoring = Coloring.fromAttributeSet(fcs.getFontColors(FontColorNames.DEFAULT_COLORING));
        font = defaultColoring.getFont();
        
        Color defaultForeground = defaultColoring.getForeColor();
        Color defaultBackground = defaultColoring.getBackColor();
        
        if(keyColor == null) keyColor = defaultForeground;
        if(keyBackground == null) keyBackground = defaultBackground;
        if(valueColor == null) valueColor = defaultForeground;
        if(valueBackground == null) valueBackground = defaultBackground;
        if(highlightColor == null) highlightColor = new Color(SystemColor.textHighlightText.getRGB());
        if(highlightBackground == null) highlightBackground = new Color(SystemColor.textHighlight.getRGB());
        if(shadowColor == null) shadowColor = new Color(SystemColor.controlHighlight.getRGB());
     
    }

    /** Updates keystrokes. Dependent code. */
    private void updateKeyStrokes() {
        KeyBindingSettings kbs = keybindings.allInstances().iterator().next();
        
        // Update keyStrokes.
        HashSet<KeyStroke> nextKS = new HashSet<KeyStroke>();
        HashSet<KeyStroke> prevKS = new HashSet<KeyStroke>();
        HashSet<KeyStroke> toggleKS = new HashSet<KeyStroke>();
        
        // Loop thru all bindings in the kit class.
        
        for(MultiKeyBinding mkb : kbs.getKeyBindings()) {

            // Find key keystrokes for find next action.
            if(mkb.getActionName().equals(BaseKit.findNextAction)) {
                for (int k = 0; k < mkb.getKeyStrokeCount(); k++) {
                    nextKS.add(mkb.getKeyStroke(k));
                }
            }
            // Find key keystrokes for find previous action.
            if(mkb.getActionName().equals(BaseKit.findPreviousAction)) {
                for (int k = 0; k < mkb.getKeyStrokeCount(); k++) {
                    prevKS.add(mkb.getKeyStroke(k));
                }
            }
            // Find key keystrokes for toggle highlight action.
            if(mkb.getActionName().equals(BaseKit.toggleHighlightSearchAction)) {
                for (int k = 0; k < mkb.getKeyStrokeCount(); k++) {
                    toggleKS.add(mkb.getKeyStroke(k));
                }
            }

        } // End of inner loop.
        
        // Copy found values to our variables.
        nextKS.toArray(keyStrokesFindNext = new KeyStroke[nextKS.size()]);
        prevKS.toArray(keyStrokesFindPrevious = new KeyStroke[prevKS.size()]);
        toggleKS.toArray(keyStrokesToggleHighlight = new KeyStroke[toggleKS.size()]);

        // notify listeners about update
        settingsUpdated();
    }
    
}
