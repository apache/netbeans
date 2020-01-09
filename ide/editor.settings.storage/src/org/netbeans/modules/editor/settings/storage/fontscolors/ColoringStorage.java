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

package org.netbeans.modules.editor.settings.storage.fontscolors;

import java.util.Set;
import org.netbeans.modules.editor.settings.storage.*;
import java.awt.Color;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.netbeans.modules.editor.settings.storage.spi.StorageReader;
import org.netbeans.modules.editor.settings.storage.spi.StorageWriter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 *
 * @author Jan Jancura, Vita Stejskal
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.settings.storage.spi.StorageDescription.class)
public final class ColoringStorage implements StorageDescription<String, AttributeSet>, StorageImpl.Operations<String, AttributeSet> {

    // -J-Dorg.netbeans.modules.editor.settings.storage.ColoringStorage.level=FINE
    private static final Logger LOG = Logger.getLogger(ColoringStorage.class.getName());

    public static final String ID = "FontsColors"; //NOI18N
    /* test */ static final String MIME_TYPE = "text/x-nbeditor-fontcolorsettings"; //NOI18N

    public ColoringStorage(String type) {
        this.type = type;
    }
    
    // ---------------------------------------------------------
    // StorageDescription implementation
    // ---------------------------------------------------------

    public ColoringStorage() {
        this(FAV_TOKEN);
    }
    
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isUsingProfiles() {
        return true;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public String getLegacyFileName() {
        return null;
    }

    @Override
    public StorageReader<String, AttributeSet> createReader(FileObject f, String mimePath) {
        throw new UnsupportedOperationException("Should not be called.");
    }

    @Override
    public StorageWriter<String, AttributeSet> createWriter(FileObject f, String mimePath) {
        throw new UnsupportedOperationException("Should not be called.");
    }
    
    // ---------------------------------------------------------
    // StorageDescription implementation
    // ---------------------------------------------------------
    
    @Override
    public Map<String, AttributeSet> load(MimePath mimePath, String profile, boolean defaults) {
        assert mimePath != null : "The parameter mimePath must not be null"; //NOI18N
        assert profile != null : "The parameter profile must not be null"; //NOI18N
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors"); //NOI18N
        Map<String, List<Object []>> files = new HashMap<>();
        SettingsType.Locator locator = SettingsType.getLocator(this);
        locator.scan(baseFolder, mimePath.getPath(), profile, true, true, !defaults, false, files);
        
        assert files.size() <= 1 : "Too many results in the scan"; //NOI18N

        List<Object []> profileInfos = files.get(profile);
        if (profileInfos == null) {
            return Collections.<String, AttributeSet>emptyMap();
        }
        
        List<Object []> filesForLocalization; 
        if (!profile.equals(EditorSettingsImpl.DEFAULT_PROFILE)) {
            // If non-default profile load the default profile supplied by modules
            // to find the localizing bundles.
            Map<String, List<Object []>> defaultProfileModulesFiles = new HashMap<>();
            locator.scan(baseFolder, mimePath.getPath(), EditorSettingsImpl.DEFAULT_PROFILE, true, true, false, false, defaultProfileModulesFiles);
            filesForLocalization = defaultProfileModulesFiles.get(EditorSettingsImpl.DEFAULT_PROFILE);
            
            // if there is no default profile (eg. in tests)
            if (filesForLocalization == null) {
                filesForLocalization = Collections.<Object []>emptyList();
            }
        } else {
            filesForLocalization = profileInfos;
        }
        
        Map<String, SimpleAttributeSet> fontsColorsMap = new HashMap<>();
        for(Object [] info : profileInfos) {
            assert info.length == 5;
            FileObject profileHome = (FileObject) info[0];
            FileObject settingFile = (FileObject) info[1];
            boolean modulesFile = ((Boolean) info[2]);
            boolean legacyFile = ((Boolean) info[4]);

            // Skip files with wrong type of colorings
            if (!type.equals(getColoringFileType(settingFile))) {
                continue;
            }
            
            // Load colorings from the settingFile
            ColoringsReader reader = new ColoringsReader(settingFile, mimePath.getPath());
            Utils.load(settingFile, reader, !legacyFile);
            Map<String, SimpleAttributeSet> sets = reader.getAdded();
            
            // Process loaded colorings
            for(SimpleAttributeSet as : sets.values()) {
                if (FAV_ANNOTATION.equals(type)) {
                    fontsColorsMap.put((String) as.getAttribute(StyleConstants.NameAttribute), as);
                    continue;
                }
                String name = (String) as.getAttribute(StyleConstants.NameAttribute);
                String translatedName = null;
                SimpleAttributeSet previous = fontsColorsMap.get(name);
                
                if (previous == null) {
                    // Find display name
                    String displayName = findDisplayName(name, settingFile, filesForLocalization);

                    if (displayName == null && !modulesFile) {
                        if (translatedName != null) {
                            displayName = findDisplayName(translatedName, settingFile, filesForLocalization);
                        }
                        if (displayName == null) {
                            // This coloring came from a user (no modules equivalent)
                            // and has no suitable display name. Probably an obsolete
                            // coloring from previous version, we will ignore it.
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Ignoring an extra coloring '" + name + "' that was not defined by modules."); //NOI18N
                            }
                            continue;
                        } else {
                            // fix the name
                            as.addAttribute(StyleConstants.NameAttribute, translatedName);
                            name = translatedName;
                        }
                    }
                    
                    if (displayName == null) {
                        displayName = name;
                    }

                    as.addAttribute(EditorStyleConstants.DisplayName, displayName);
                    as.addAttribute(ATTR_MODULE_SUPPLIED, modulesFile);
                    
                    fontsColorsMap.put(name, as);
                } else {
                    // the scanner alwyas returns modules files first, followed by user files
                    // when a coloring was defined in a user file it must not be merged
                    // with its default version supplied by modules
                    boolean moduleSupplied = (Boolean) previous.getAttribute(ATTR_MODULE_SUPPLIED);
                    if (moduleSupplied == modulesFile) {
                        mergeAttributeSets(previous, as);
                    } else {
                        // Copy over the display name and the link to the default coloring
                        as.addAttribute(EditorStyleConstants.DisplayName, previous.getAttribute(EditorStyleConstants.DisplayName));
                        Object df = previous.getAttribute(EditorStyleConstants.Default);
                        if (df != null) {
                            as.addAttribute(EditorStyleConstants.Default, df);
                        }
                        as.addAttribute(ATTR_MODULE_SUPPLIED, modulesFile);
                        
                        fontsColorsMap.put(name, as);
                    }
                }
            }
        }
            
        return NbUtils.immutize(fontsColorsMap, ATTR_MODULE_SUPPLIED);
    }
    
    @Override
    public boolean save(
            MimePath mimePath, String profile, boolean defaults, 
            final Map<String, AttributeSet> fontColors, 
            final Map<String, AttributeSet> defaultFontColors
    ) throws IOException {
        assert mimePath != null : "The parameter mimePath must not be null"; //NOI18N
        assert profile != null : "The parameter profile must not be null"; //NOI18N
        
        final String settingFileName = SettingsType.getLocator(this).getWritableFileName(
                mimePath.getPath(), 
                profile, 
                FAV_TOKEN.equals(type) ? "-tokenColorings" : FAV_HIGHLIGHT.equals(type) ? "-highlights" : "-annotations", //NOI18N
                defaults);

        FileUtil.runAtomicAction((FileSystem.AtomicAction) () -> {
            FileObject baseFolder = FileUtil.getConfigFile("Editors"); //NOI18N
            FileObject f = FileUtil.createData(baseFolder, settingFileName);
            f.setAttribute(FA_TYPE, type);

            Map<String, AttributeSet> added = new HashMap<>();
            Map<String, AttributeSet> removed = new HashMap<>();
            Utils.diff(defaultFontColors, fontColors, added, removed);
            
            ColoringsWriter writer = new ColoringsWriter();
            writer.setAdded(added);
            writer.setRemoved(removed.keySet());

            Utils.save(f, writer);
        });
        
        return true; // reset the cache, to force reloading from files next time colorings are accessed
    }

    @Override
    public void delete(MimePath mimePath, String profile, boolean defaults) throws IOException {
        assert mimePath != null : "The parameter mimePath must not be null"; //NOI18N
        assert profile != null : "The parameter profile must not be null"; //NOI18N
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors"); //NOI18N
        Map<String, List<Object []>> files = new HashMap<>();
        SettingsType.getLocator(this).scan(baseFolder, mimePath.getPath(), profile, true, defaults, !defaults, false, files);
        
        assert files.size() <= 1 : "Too many results in the scan"; //NOI18N

        final List<Object []> profileInfos = files.get(profile);
        if (profileInfos != null) {
            FileUtil.runAtomicAction((FileSystem.AtomicAction) () -> {
                for(Object [] info : profileInfos) {
                    FileObject settingFile = (FileObject) info[1];

                    // Skip files with wrong type of colorings
                    if (!type.equals(getColoringFileType(settingFile))) {
                        continue;
                    }

                    settingFile.delete();
                }
            });
        }
    }
    
    // ---------------------------------------------------------
    // private implementation
    // ---------------------------------------------------------
    
    private static final String HIGHLIGHTING_FILE_NAME = "editorColoring.xml"; // NOI18N
    
    private static final String E_ROOT = "fontscolors"; //NOI18N
    private static final String E_COLORDEF = "colordef"; //NOI18N
    private static final String E_FONTCOLOR = "fontcolor"; //NOI18N
    private static final String E_FONT = "font"; //NOI18N
    private static final String A_NAME = "name"; //NOI18N
    private static final String A_COLOR = "color"; //NOI18N
    private static final String A_FOREGROUND = "foreColor"; //NOI18N
    private static final String A_BACKGROUND = "bgColor"; //NOI18N
    private static final String A_STRIKETHROUGH = "strikeThrough"; //NOI18N
    private static final String A_WAVEUNDERLINE = "waveUnderlined"; //NOI18N
    private static final String A_UNDERLINE = "underline"; //NOI18N
    private static final String A_DEFAULT = "default"; //NOI18N
    private static final String A_SIZE = "size"; //NOI18N
    private static final String A_STYLE = "style"; //NOI18N
    private static final String V_BOLD_ITALIC = "bold+italic"; //NOI18N
    private static final String V_BOLD = "bold"; //NOI18N
    private static final String V_ITALIC = "italic"; //NOI18N
    private static final String V_PLAIN = "plain"; //NOI18N
    
    private static final String PUBLIC_ID = "-//NetBeans//DTD Editor Fonts and Colors settings 1.2//EN"; //NOI18N
    private static final String SYSTEM_ID = "http://netbeans.apache.org/dtds/EditorFontsColors-1_2.dtd"; //NOI18N

    private static final String FA_TYPE = "nbeditor-settings-ColoringType"; //NOI18N
    public static final String FAV_TOKEN = "token"; //NOI18N
    public static final String FAV_HIGHLIGHT = "highlight"; //NOI18N
    public static final String FAV_ANNOTATION = "annotation"; //NOI18N
    
    private static final Object ATTR_MODULE_SUPPLIED = new Object();
    
    private final String type;
    
    private static String findDisplayName(String name, FileObject settingFile, List<Object []> filesForLocalization) {
        // Try the settingFile first
        String displayName = Utils.getLocalizedName(settingFile, name, null, true);

        // Then try all module files from the default profile
        if (displayName == null) {
            for(Object [] locFileInfo : filesForLocalization) {
                FileObject locFile = (FileObject) locFileInfo[1];
                displayName = Utils.getLocalizedName(locFile, name, null, true);
                if (displayName != null) {
                    break;
                }
            }
        }
        
        return displayName;
    }
    
    private static void mergeAttributeSets(SimpleAttributeSet original, AttributeSet toMerge) {
        for(Enumeration names = toMerge.getAttributeNames(); names.hasMoreElements(); ) {
            Object key = names.nextElement();
            Object value = toMerge.getAttribute(key);
            original.addAttribute(key, value);
        }
    }
    

    private static class ColoringsReader extends StorageReader<String, SimpleAttributeSet> {

        private final Map<String, String> colordefs = new HashMap<>();
        private final Map<String, SimpleAttributeSet> colorings = new HashMap<>();
        private SimpleAttributeSet attribs = null;
        
        public ColoringsReader(FileObject f, String mimePath) {
            super(f, mimePath);
        }

        public @Override Map<String, SimpleAttributeSet> getAdded() {
            return colorings;
        }

        public @Override Set<String> getRemoved() {
            return Collections.<String>emptySet();
        }

        public @Override void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            try {
                switch (name) {
                    case E_ROOT:
                        // We don't read anythhing from the root element
                        colordefs.clear();
                        break;
                    case E_COLORDEF:
                        String nameAttr = attributes.getValue(A_NAME);
                        String colorAttr = attributes.getValue(A_COLOR);
                        String oldRef = colordefs.put(nameAttr, colorAttr);
                        if (oldRef != null) {
                            LOG.log(Level.WARNING, "Color reference: '" + nameAttr + "' previously defined as '" + oldRef + "' is now '" + colorAttr +"'");
                        }   break;
                    case E_FONTCOLOR:
                        {
                            assert attribs == null;
                            attribs = new SimpleAttributeSet();
                            String value;
                            String nameAttributeValue = attributes.getValue(A_NAME);
                            attribs.addAttribute(StyleConstants.NameAttribute, nameAttributeValue);
                            value = attributes.getValue(A_BACKGROUND);
                            if (value != null) {
                                attribs.addAttribute(StyleConstants.Background, stringToColor(value));
                            }       value = attributes.getValue(A_FOREGROUND);
                            if (value != null) {
                                attribs.addAttribute(StyleConstants.Foreground, stringToColor(value));
                            }       value = attributes.getValue(A_UNDERLINE);
                            if (value != null) {
                                attribs.addAttribute(StyleConstants.Underline, stringToColor(value));
                            }       value = attributes.getValue(A_STRIKETHROUGH);
                            if (value != null) {
                                attribs.addAttribute(StyleConstants.StrikeThrough, stringToColor(value));
                            }       value = attributes.getValue(A_WAVEUNDERLINE);
                            if (value != null) {
                                attribs.addAttribute(EditorStyleConstants.WaveUnderlineColor, stringToColor(value));
                            }       value = attributes.getValue(A_DEFAULT);
                            if (value != null) {
                                attribs.addAttribute(EditorStyleConstants.Default, value);
                            }       colorings.put(nameAttributeValue, attribs);
                            break;
                        }
                    case E_FONT:
                        {
                            assert attribs != null;
                            String value;
                            value = attributes.getValue(A_NAME);
                            if (value != null) {
                                attribs.addAttribute(StyleConstants.FontFamily, value);
                            }       value = attributes.getValue(A_SIZE);
                            if (value != null) {
                                try {
                                    attribs.addAttribute(StyleConstants.FontSize, Integer.decode(value));
                                } catch (NumberFormatException ex) {
                                    LOG.log(Level.WARNING, value + " is not a valid Integer; parsing attribute " + A_SIZE + //NOI18N
                                            getProcessedFile().getPath(), ex);
                                }
                            }       value = attributes.getValue(A_STYLE);
                            if (value != null) {
                                attribs.addAttribute(StyleConstants.Bold, value.contains(V_BOLD));
                                attribs.addAttribute(StyleConstants.Italic, value.contains(V_ITALIC));
                            }       break;
                        }
                    default:
                        break;
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Can't parse colorings file " + getProcessedFile().getPath(), ex); //NOI18N
            }
        }

        public @Override void endElement(String uri, String localName, String name) throws SAXException {
            if (name.equals(E_FONTCOLOR)) {
                // reset the attribs
                attribs = null;
            }
        }

        Color stringToColor(String name) throws Exception {
            String resolve = colordefs.getOrDefault(name, name);
            return ColoringStorage.stringToColor(resolve);
        }

    } // End of ColoringsReader class
    
    private static final class ColoringsWriter extends StorageWriter<String, AttributeSet> {
        
        public ColoringsWriter() {
            super();
        }
        
        @Override
        public Document getDocument() {
            Document doc = XMLUtil.createDocument(E_ROOT, null, PUBLIC_ID, SYSTEM_ID);
            Node root = doc.getElementsByTagName(E_ROOT).item(0);

            for(AttributeSet category : getAdded().values()) {
                Element fontColor = doc.createElement(E_FONTCOLOR);
                root.appendChild(fontColor);
                fontColor.setAttribute(A_NAME, (String) category.getAttribute(StyleConstants.NameAttribute));

                if (category.isDefined(StyleConstants.Foreground)) {
                    fontColor.setAttribute(
                        A_FOREGROUND, 
                        colorToString((Color) category.getAttribute(StyleConstants.Foreground))
                    );
                }
                if (category.isDefined(StyleConstants.Background)) {
                    fontColor.setAttribute(
                        A_BACKGROUND,
                        colorToString((Color) category.getAttribute(StyleConstants.Background))
                    );
                }
                if (category.isDefined(StyleConstants.StrikeThrough)) {
                    fontColor.setAttribute(
                        A_STRIKETHROUGH,
                        colorToString((Color) category.getAttribute(StyleConstants.StrikeThrough))
                    );
                }
                if (category.isDefined(EditorStyleConstants.WaveUnderlineColor)) {
                    fontColor.setAttribute(
                        A_WAVEUNDERLINE,
                        colorToString((Color) category.getAttribute(EditorStyleConstants.WaveUnderlineColor))
                    );
                }
                if (category.isDefined(StyleConstants.Underline)) {
                    fontColor.setAttribute(
                        A_UNDERLINE,
                        colorToString((Color) category.getAttribute(StyleConstants.Underline))
                    );
                }
                if (category.isDefined(EditorStyleConstants.Default)) {
                    fontColor.setAttribute(
                        A_DEFAULT,
                        (String) category.getAttribute(EditorStyleConstants.Default)
                    );
                }

                if ( category.isDefined(StyleConstants.FontFamily) ||
                     category.isDefined(StyleConstants.FontSize) ||
                     category.isDefined(StyleConstants.Bold) ||
                     category.isDefined(StyleConstants.Italic)
                ) {
                    Element font = doc.createElement(E_FONT);
                    fontColor.appendChild(font);

                    if (category.isDefined(StyleConstants.FontFamily)) {
                        font.setAttribute(
                            A_NAME,
                            (String) category.getAttribute(StyleConstants.FontFamily)
                        );
                    }
                    if (category.isDefined(StyleConstants.FontSize)) {
                        font.setAttribute(
                            A_SIZE,
                            ((Integer) category.getAttribute(StyleConstants.FontSize)).toString()
                        );
                    }
                    if (category.isDefined(StyleConstants.Bold) ||
                        category.isDefined(StyleConstants.Italic)
                    ) {
                        Boolean bold = Boolean.FALSE, italic = Boolean.FALSE;

                        if (category.isDefined(StyleConstants.Bold)) {
                            bold = (Boolean) category.getAttribute(StyleConstants.Bold);
                        }
                        if (category.isDefined(StyleConstants.Italic)) {
                            italic = (Boolean) category.getAttribute(StyleConstants.Italic);
                        }

                        font.setAttribute(A_STYLE, bold ?
                            (italic ? V_BOLD_ITALIC : V_BOLD) :
                            (italic ? V_ITALIC : V_PLAIN)
                        );
                    }
                }
            }
            
            return doc;
        }
    } // End of ColoringsWriter class
    
    private static String getColoringFileType(FileObject f) {
        Object typeValue = f.getAttribute(FA_TYPE);
        if (typeValue instanceof String) {
            return (String)typeValue;
        } else {
            if (f.getNameExt().equals(HIGHLIGHTING_FILE_NAME)) {
                return FAV_HIGHLIGHT;
            } else {
                return FAV_TOKEN;
            }
        }
    }
    
    private static final Map<Color, String> colorToName = new HashMap<Color, String>();
    private static final Map<String, Color> nameToColor = new HashMap<String, Color>();
    
    static {
        colorToName.put (Color.black, "black");
        nameToColor.put ("black", Color.black);
        colorToName.put (Color.blue, "blue");
        nameToColor.put ("blue", Color.blue);
        colorToName.put (Color.cyan, "cyan");
        nameToColor.put ("cyan", Color.cyan);
        colorToName.put (Color.darkGray, "darkGray");
        nameToColor.put ("darkGray", Color.darkGray);
        colorToName.put (Color.gray, "gray");
        nameToColor.put ("gray", Color.gray);
        colorToName.put (Color.green, "green");
        nameToColor.put ("green", Color.green);
        colorToName.put (Color.lightGray, "lightGray");
        nameToColor.put ("lightGray", Color.lightGray);
        colorToName.put (Color.magenta, "magenta");
        nameToColor.put ("magenta", Color.magenta);
        colorToName.put (Color.orange, "orange");
        nameToColor.put ("orange", Color.orange);
        colorToName.put (Color.pink, "pink");
        nameToColor.put ("pink", Color.pink);
        colorToName.put (Color.red, "red");
        nameToColor.put ("red", Color.red);
        colorToName.put (Color.white, "white");
        nameToColor.put ("white", Color.white);
        colorToName.put (Color.yellow, "yellow");
        nameToColor.put ("yellow", Color.yellow);
    }
    
    private static String colorToString (Color color) {
	if (colorToName.containsKey (color))
	    return (String) colorToName.get (color);
	return Integer.toHexString (color.getRGB ());
    }
    
    private static Color stringToColor (String color) throws Exception {
	if (nameToColor.containsKey (color))
	    return (Color) nameToColor.get (color);
        try {
            return new Color ((int) Long.parseLong (color, 16), (color.length() != 6)); // formats: #AARRGGBB or #RRGGBB (Alpha is defaulted to 255)
        } catch (NumberFormatException ex) {
            LOG.log(Level.WARNING, "Can''t parse ''{0}'' to a Color. Using RED as a replacement.", color);
            return Color.red;
        }
    }
}
