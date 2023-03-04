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

package org.netbeans.modules.languages.features;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.database.DatabaseContext;
import org.netbeans.api.languages.database.DatabaseUsage;
import org.netbeans.api.languages.database.DatabaseDefinition;
import org.netbeans.api.languages.database.DatabaseItem;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.TokenType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
public class ColorsManager {
    
    public static final String COLOR = "COLOR";
    
    static List<AttributeSet> getColors(Language l, ASTPath path, Document doc) {
        List<AttributeSet> result = new ArrayList<AttributeSet> ();
        Context context = SyntaxContext.create(doc, path);
        List<Feature> fs = l.getFeatureList ().getFeatures(COLOR, path);
        Iterator<Feature> it = fs.iterator();
        while (it.hasNext()) {
            Feature f = it.next();
            if (!f.getBoolean("condition", context, true)) continue;
            result.add(createColoring(f, null));
        }
        ASTNode node = (ASTNode) path.getRoot ();
        DatabaseContext root = DatabaseManager.getRoot (node);
        if (root == null) return result;
        ASTItem item = path.getLeaf ();
        DatabaseItem i = root.getDatabaseItem (item.getOffset ());
        if (i == null || i.getEndOffset () != item.getEndOffset ()) return result;
        AttributeSet as = getAttributes (i);
        if (as != null)
            result.add (as);
        return result;
    }
    
    private static AttributeSet getAttributes (DatabaseItem item) {
        if (item instanceof DatabaseDefinition) {
            DatabaseDefinition definition = (DatabaseDefinition) item;
            if (definition.getUsages ().isEmpty ()) {
                if ("parameter".equals (definition.getType ()))
                    return getUnusedParameterAttributes ();
                if ("variable".equals (definition.getType ()))
                    return getUnusedLocalVariableAttributes ();
                if ("field".equals (definition.getType ()))
                    return getUnusedFieldAttributes ();
            } else {
                if ("parameter".equals (definition.getType ()))
                    return getParameterAttributes ();
                if ("variable".equals (definition.getType ()))
                    return getLocalVariableAttributes ();
                if ("field".equals (definition.getType ()))
                    return getFieldAttributes ();
            }
        }
        if (item instanceof DatabaseUsage) {
            DatabaseUsage usage = (DatabaseUsage) item;
            DatabaseDefinition definition = usage.getDefinition ();
            if ("parameter".equals (definition.getType ()))
                return getParameterAttributes ();
            if ("local".equals (definition.getType ()))
                return getLocalVariableAttributes ();
            if ("field".equals (definition.getType ()))
                return getFieldAttributes ();
        }
        return null;
    }
    
    private static AttributeSet unusedParameterAttributeSet;
    
    private static AttributeSet getUnusedParameterAttributes () {
        if (unusedParameterAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (115, 115, 115));
            unusedParameterAttributeSet = sas;
        }
        return unusedParameterAttributeSet;
    }
    
    private static AttributeSet parameterAttributeSet;
    
    private static AttributeSet getParameterAttributes () {
        if (parameterAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (160, 96, 1));
            parameterAttributeSet = sas;
        }
        return parameterAttributeSet;
    }
    
    private static AttributeSet unusedLocalVariableAttributeSet;
    
    private static AttributeSet getUnusedLocalVariableAttributes () {
        if (unusedLocalVariableAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (115, 115, 115));
            unusedLocalVariableAttributeSet = sas;
        }
        return unusedLocalVariableAttributeSet;
    }
    
    private static AttributeSet localVariableAttributeSet;
    
    private static AttributeSet getLocalVariableAttributes () {
        if (localVariableAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            localVariableAttributeSet = sas;
        }
        return localVariableAttributeSet;
    }
    
    private static AttributeSet unusedFieldAttributeSet;
    
    private static AttributeSet getUnusedFieldAttributes () {
        if (unusedFieldAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (115, 115, 115));
            StyleConstants.setBold (sas, true);
            unusedFieldAttributeSet = sas;
        }
        return unusedFieldAttributeSet;
    }
    
    private static AttributeSet fieldAttributeSet;
    
    private static AttributeSet getFieldAttributes () {
        if (fieldAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (9, 134, 24));
            StyleConstants.setBold (sas, true);
            fieldAttributeSet = sas;
        }
        return fieldAttributeSet;
    }
    
    public static void initColorings (Language l) {
        FontColorSettingsFactory fcsf = EditorSettings.getDefault().
                getFontColorSettings(new String[] {l.getMimeType()});
        if (!fcsf.getAllFontColors("NetBeans").isEmpty())
            return;
        
        String bundleName = getBundleName(l);
        ResourceBundle bundle = bundleName != null ? NbBundle.getBundle(bundleName) : null;
        
        Map<String,AttributeSet> colorsMap = new HashMap<String,AttributeSet> ();
        Iterator<Language> it = l.getImportedLanguages().iterator();
        while (it.hasNext())
            addColors (colorsMap, it.next (), bundle);
        addColors (colorsMap, l, bundle);
        fcsf.setAllFontColorsDefaults("NetBeans", colorsMap.values());
        fcsf.setAllFontColors("NetBeans", colorsMap.values());
        
        if (bundleName != null) {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            FileObject fo = fs.findResource("Editors/" + l.getMimeType() + "/FontsColors/NetBeans/org-netbeans-modules-editor-settings-CustomFontsColors.xml"); // NOI18N
            try {
                if (fo != null) {
                    fo.setAttribute("SystemFileSystem.localizingBundle", bundleName);
                }
            } catch (IOException e) {
            }
            fo = fs.findResource("Editors/" + l.getMimeType() + "/FontsColors/NetBeans/Defaults/org-netbeans-modules-editor-settings-CustomFontsColors.xml"); // NOI18N
            try {
                if (fo != null) {
                    fo.setAttribute("SystemFileSystem.localizingBundle", bundleName);
                }
            } catch (IOException e) {
            }
        }
    }
    
    private static void addColors (
        Map<String,AttributeSet> colorsMap, 
        Language l, 
        ResourceBundle bundle
    ) {
        if (l.getParser () == null) return;
        Map<String,AttributeSet> defaultsMap = getDefaultColors();
        List<Feature> list = l.getFeatureList ().getFeatures (COLOR);
        Iterator<Feature> it = list.iterator();
        while (it.hasNext()) {
            Feature f = it.next();
            AttributeSet as = createColoring(f, bundle);
            colorsMap.put(
                    (String) as.getAttribute(StyleConstants.NameAttribute),
                    as
                    );
        }
        
        Iterator<TokenType> it2 = l.getParser ().getTokenTypes ().iterator ();
        while (it2.hasNext()) {
            TokenType token = it2.next();
            String type = token.getType();
            if (colorsMap.containsKey(type)) continue;
            SimpleAttributeSet sas = new SimpleAttributeSet();
            sas.addAttribute(StyleConstants.NameAttribute, type);
            String displayName = type;
            if (bundle != null) {
                try {
                    displayName = bundle.getString(type);
                } catch (MissingResourceException e) {
                }
            }
            sas.addAttribute(EditorStyleConstants.DisplayName, displayName);
            String def = type;
            int i = def.lastIndexOf('_');
            if (i > 0) def = def.substring(i + 1);
            if (defaultsMap.containsKey(def))
                sas.addAttribute(EditorStyleConstants.Default, def);
            colorsMap.put(type, sas);
        }
    }
    
    private static List<AttributeSet> getColors(Language l, ResourceBundle bundle) {
        List<Feature> list = l.getFeatureList ().getFeatures("COLORS");
        List<AttributeSet> result = new ArrayList<AttributeSet> ();
        Iterator<Feature> it = list.iterator();
        while (it.hasNext()) {
            Feature f = it.next();
            result.add(createColoring(f, bundle));
        }
        return result;
    }
    
    static SimpleAttributeSet createColoring(Feature f, ResourceBundle bundle) {
        String colorName = (String) f.getValue("color_name");
        String displayName = null;
        if (colorName == null)
            colorName = f.getSelector().getAsString();
        if (bundle != null) {
            try {
                displayName = bundle.getString(colorName);
            } catch (MissingResourceException e) {
            }
        }
        if (displayName == null) {
            displayName = colorName;
        }
        return createColoring(
                colorName,
                displayName,
                (String) f.getValue("default_coloring"),
                (String) f.getValue("foreground_color"),
                (String) f.getValue("background_color"),
                (String) f.getValue("underline_color"),
                (String) f.getValue("wave_underline_color"),
                (String) f.getValue("strike_through_color"),
                (String) f.getValue("font_name"),
                (String) f.getValue("font_type")
                );
    }
    
    private static SimpleAttributeSet createColoring(
            String colorName,
            String displayName,
            String defaultColor,
            String foreground,
            String background,
            String underline,
            String waveunderline,
            String strikethrough,
            String fontName,
            String fontType
            ) {
        SimpleAttributeSet coloring = new SimpleAttributeSet();
        coloring.addAttribute(StyleConstants.NameAttribute, colorName);
        coloring.addAttribute(EditorStyleConstants.DisplayName, displayName);
        if (defaultColor != null)
            coloring.addAttribute(EditorStyleConstants.Default, defaultColor);
        if (foreground != null)
            coloring.addAttribute(StyleConstants.Foreground, readColor(foreground));
        if (background != null)
            coloring.addAttribute(StyleConstants.Background, readColor(background));
        if (strikethrough != null)
            coloring.addAttribute(StyleConstants.StrikeThrough, readColor(strikethrough));
        if (underline != null)
            coloring.addAttribute(StyleConstants.Underline, readColor(underline));
        if (waveunderline != null)
            coloring.addAttribute(EditorStyleConstants.WaveUnderlineColor, readColor(waveunderline));
        if (fontName != null)
            coloring.addAttribute(StyleConstants.FontFamily, fontName);
        if (fontType != null) {
            if (fontType.toLowerCase().indexOf("bold") >= 0)
                coloring.addAttribute(StyleConstants.Bold, Boolean.TRUE);
            if (fontType.toLowerCase().indexOf("italic") >= 0)
                coloring.addAttribute(StyleConstants.Italic, Boolean.TRUE);
        }
        return coloring;
    }
    
    private static Map<String,Color> colors = new HashMap<String,Color> ();
    static {
        colors.put("black", Color.black);
        colors.put("blue", Color.blue);
        colors.put("cyan", Color.cyan);
        colors.put("darkGray", Color.darkGray);
        colors.put("gray", Color.gray);
        colors.put("green", Color.green);
        colors.put("lightGray", Color.lightGray);
        colors.put("magenta", Color.magenta);
        colors.put("orange", Color.orange);
        colors.put("pink", Color.pink);
        colors.put("red", Color.red);
        colors.put("white", Color.white);
        colors.put("yellow", Color.yellow);
    }
    
    static Color readColor(String color) {
        if (color == null) return null;
        Color result = (Color) colors.get(color);
        if (result == null)
            result = Color.decode(color);
        return result;
    }
    
    //    public static Map<String,AttributeSet> getColorMap (Language l) {
    //        Map defaultsMap = getDefaultColors ();
    //        Map<String,AttributeSet> colorsMap = getCurrentColors (l);
    //        Iterator<TokenType> it = l.getTokenTypes ().iterator ();
    //        while (it.hasNext ()) {
    //            TokenType token = it.next ();
    //            List<SimpleAttributeSet> colors = (List<SimpleAttributeSet>) getFeature
    //                (Language.COLOR, token.getType ());
    //            if (colors != null)
    //                for (Iterator<SimpleAttributeSet> it2 = colors.iterator (); it2.hasNext ();) {
    //                    SimpleAttributeSet as = it2.next();
    //                    String id = (String) as.getAttribute ("color_name"); // NOI18N
    //                    if (id == null)
    //                        id = token.getType ();
    //                    addColor (id, as, colorsMap, defaultsMap);
    //                }
    //            else
    //                addColor (token.getType (), null, colorsMap, defaultsMap);
    //        }
    //
    //        //List<AttributeSet> colors = getColors (l);
    //        Map m = (Map) features.get (Language.COLOR);
    //        if (m == null)
    //            return Collections.<String,AttributeSet>emptyMap ();
    //        Iterator<String> it2 = m.keySet ().iterator ();
    //        while (it2.hasNext ()) {
    //            String type = it2.next ();
    //            if (colorsMap.containsKey (type))
    //                continue;
    //            Object obj = m.get (type);
    //            if (obj != null) {
    //                for (Iterator iter = ((List)obj).iterator(); iter.hasNext(); ) {
    //                    SimpleAttributeSet as = (SimpleAttributeSet) iter.next();
    //                    addColor (type, as, colorsMap, defaultsMap);
    //                }
    //            }
    //        }
    //        addColor ("error", null, colorsMap, defaultsMap);
    //        return colorsMap;
    //    }
    
    private static void addColor(
            String tokenType,
            SimpleAttributeSet sas,
            Map<String,AttributeSet> colorsMap,
            Map<String,AttributeSet> defaultsMap
            ) {
        if (sas == null)
            sas = new SimpleAttributeSet();
        else
            sas = new SimpleAttributeSet(sas);
        String colorName = (String) sas.getAttribute(StyleConstants.NameAttribute);
        if (colorName == null)
            colorName = tokenType;
        sas.addAttribute(StyleConstants.NameAttribute, colorName);
        sas.addAttribute(EditorStyleConstants.DisplayName, colorName);
        if (!sas.isDefined(EditorStyleConstants.Default)) {
            String def = colorName;
            int i = def.lastIndexOf('_');
            if (i > 0) def = def.substring(i + 1);
            if (defaultsMap.containsKey(def))
                sas.addAttribute(EditorStyleConstants.Default, def);
        }
        colorsMap.put(colorName, sas);
    }
    
    private static Map<String,AttributeSet> getDefaultColors() {
        Collection<AttributeSet> defaults = EditorSettings.getDefault().
                getDefaultFontColorDefaults("NetBeans");
        Map<String,AttributeSet> defaultsMap = new HashMap<String,AttributeSet> ();
        Iterator<AttributeSet> it = defaults.iterator(); // check if IDE Defaults module is installed
        while (it.hasNext()) {
            AttributeSet as = it.next();
            defaultsMap.put(
                    (String) as.getAttribute(StyleConstants.NameAttribute),
                    as
                    );
        }
        return defaultsMap;
    }
    
    private static Map getCurrentColors(Language l) {
        // current colors
        FontColorSettingsFactory fcsf = EditorSettings.getDefault().
                getFontColorSettings(new String[] {l.getMimeType()});
        Collection<AttributeSet> colors = fcsf.getAllFontColors("NetBeans");
        Map<String,AttributeSet> colorsMap = new HashMap<String,AttributeSet> ();
        Iterator<AttributeSet> it = colors.iterator();
        while (it.hasNext()) {
            AttributeSet as = it.next();
            colorsMap.put(
                    (String) as.getAttribute(StyleConstants.NameAttribute),
                    as
                    );
        }
        return colorsMap;
    }
    
    private static String getBundleName(Language l) {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject root = fs.findResource("Editors/" + l.getMimeType()); // NOI18N
        Object attrValue = root.getAttribute("SystemFileSystem.localizingBundle"); //NOI18N
        // [PENDING] if (bundleName == null) ... check for bundle name in nbs file
        return (String) attrValue;
    }
    
}
