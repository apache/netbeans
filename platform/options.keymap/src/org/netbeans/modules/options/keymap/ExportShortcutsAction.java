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


package org.netbeans.modules.options.keymap;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.modules.options.keymap.XMLStorage.Attribs;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

public class ExportShortcutsAction {
    
    private ExportShortcutsAction() {}
    
    private static Action exportIDEActionsAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_IDE_Actions_Action"));}
        
        public void actionPerformed (ActionEvent e) {

            // 1) load all keymaps to allKeyMaps
            LayersBridge layersBridge = new LayersBridge ();
            Map<String, Set<ShortcutAction>> categoryToActions = layersBridge.getActions ();
            Map<String, Map<String, ShortcutAction>> m = resolveNames (categoryToActions);

            generateLayersXML (layersBridge, m);
        }
    };
    
    public static Action getExportIDEActionsAction () {
        return exportIDEActionsAction;
    }
    
    private static Action exportIDEShortcutsAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_IDE_Shortcuts_Action"));}
        
        public void actionPerformed (ActionEvent e) {

            // 1) load all keymaps to allKeyMaps
            Map<String, Map<String, ShortcutAction>> allKeyMaps = 
                    new HashMap<String, Map<String, ShortcutAction>> ();
            LayersBridge layersBridge = new LayersBridge ();
            // no synchronization needed; the bridge is a new instance
            layersBridge.getActions ();
            List keyMaps = layersBridge.getProfiles ();
            Iterator it3 = keyMaps.iterator ();
            while (it3.hasNext ()) {
                String keyMapName = (String) it3.next ();
                Map<ShortcutAction, Set<String>> actionToShortcuts = layersBridge.getKeymap (keyMapName);
                Map<String, ShortcutAction> shortcutToAction = LayersBridge.shortcutToAction (actionToShortcuts);
                allKeyMaps.put (keyMapName, shortcutToAction);
            }

            generateLayersXML (layersBridge, allKeyMaps);
        }
    };
    
    public static Action getExportIDEShortcutsAction () {
        return exportIDEShortcutsAction;
    }
    
    private static Action exportEditorShortcutsAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_Editor_Shortcuts_Action"));}
        
        public void actionPerformed (ActionEvent e) {
            KeymapManager editorBridge = null;
            for (KeymapManager km : KeymapModel.getKeymapManagerInstances()) {
                if ("EditorBridge".equals(km.getName())) {
                    editorBridge = km;
                    break;
                }
            }
            if (editorBridge != null) {
                final KeymapManager feb = editorBridge;
                // FIXME - synchronize
                final Object[] o = new Object[1];
                KeymapModel.waitFinished(new Runnable() { 
                    public void run() {
                        o[0] = feb.getKeymap(feb.getCurrentProfile ());
                    }
                });
                generateEditorXML ((Map<ShortcutAction, Set<String>>)o[0]);
            }
        }
    };
    
    public static Action getExportEditorShortcutsAction () {
        return exportEditorShortcutsAction;
    }
    
    private static Action exportShortcutsToHTMLAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_Shortcuts_to_HTML_Action"));}
        
        public void actionPerformed (ActionEvent e) {
            exportShortcutsOfAllProfilesToHTML ();
        }

        @Override
        public boolean isEnabled() {
            return !Boolean.getBoolean(ExportShortcutsAction.class.getName() + ".disable");
        }
        
    };
    
    public static Action getExportShortcutsToHTMLAction () {
        return exportShortcutsToHTMLAction;
    }

    
    // helper methods ..........................................................
    public static void exportShortcutsOfProfileToHTML (String profile) {
	final boolean showSystemSpecificShortcuts = true;
	exportShortcutsToHTML(new KeymapModel(), Arrays.asList(profile), showSystemSpecificShortcuts);
    }

    private static void exportShortcutsOfAllProfilesToHTML () {
	KeymapModel keymapModel = new KeymapModel ();
	List<String> allProfiles = keymapModel.getProfiles ();
	final boolean showSystemSpecificShortcuts = false;
	exportShortcutsToHTML(keymapModel, allProfiles, showSystemSpecificShortcuts);
    }
    
    
    private static void exportShortcutsToHTML (KeymapModel keymapModel, Collection<String> profiles, boolean displayHumanReadibleShortcuts) {
        // read all shortcuts to keymaps
        Map<String, Map<ShortcutAction, Set<String>>> keymaps = 
                new TreeMap<String, Map<ShortcutAction, Set<String>>> ();
        for (String profile: profiles) {
            keymaps.put (
                profile,
                keymapModel.getKeymap (profile)
            );
        }
        
        try {
            StringBuffer sb = new StringBuffer ();

            Attribs attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "html", attribs, "");
            XMLStorage.generateFolderStart (sb, "body", attribs, "  ");
            attribs.add ("border", "1");
            attribs.add ("cellpadding", "1");
            attribs.add ("cellspacing", "0");
            XMLStorage.generateFolderStart (sb, "table", attribs, "    ");
            attribs = new Attribs (true);

            // print header of table
            XMLStorage.generateFolderStart (sb, "tr", attribs, "      ");
            XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
            XMLStorage.generateFolderStart (sb, "h2", attribs, "        ");
            sb.append ("Action Name");
            XMLStorage.generateFolderEnd (sb, "h2", "        ");
            XMLStorage.generateFolderEnd (sb, "td", "        ");
            for (String profile: keymaps.keySet ()) {
                XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
                XMLStorage.generateFolderStart (sb, "h2", attribs, "        ");
                sb.append (profile);
                XMLStorage.generateFolderEnd (sb, "h2", "        ");
                XMLStorage.generateFolderEnd (sb, "td", "        ");
            }
            
            // print body of table
            exportShortcutsToHTML2 (keymapModel, sb, keymaps, displayHumanReadibleShortcuts);
            
            XMLStorage.generateFolderEnd (sb, "table", "    ");
            XMLStorage.generateFolderEnd (sb, "body", "  ");
            XMLStorage.generateFolderEnd (sb, "html", "");
            
            FileObject fo = FileUtil.createData (
                FileUtil.getConfigRoot (),
                "shortcuts.html"
            );
            FileLock fileLock = fo.lock ();
            try (OutputStream outputStream = fo.getOutputStream (fileLock);
                OutputStreamWriter writer = new OutputStreamWriter (outputStream)){
                writer.write (sb.toString ());
                writer.close ();

		if (fo.canRead() && displayHumanReadibleShortcuts) {
		    //open generated HTML in external browser
                    URL u = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                    if (u == null) {
                        u = fo.toURL();
                    }
		    HtmlBrowser.URLDisplayer.getDefault().showURLExternal(u);
		}
	    } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
            } finally {
                fileLock.releaseLock ();
            }
	    
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }

    /**
     * Writes body of shortcuts table to given StringBuffer.
     */
    private static void exportShortcutsToHTML2 (
        KeymapModel keymapModel, 
        StringBuffer sb,
        Map<String, Map<ShortcutAction, Set<String>>> keymaps, 
	boolean displayHumanReadibleShortcuts
    ) {
        List<String> categories = new ArrayList<String> (keymapModel.getActionCategories ());
        Collections.<String>sort (categories);
        Attribs attribs = new Attribs (true);
        for (String category: categories) {
            
            // print category title
            XMLStorage.generateFolderStart (sb, "tr", attribs, "      ");
            attribs.add ("colspan", Integer.toString (keymaps.size () + 1));
            attribs.add ("rowspan", "1");
            XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
            attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "h3", attribs, "        ");
            sb.append (category);
            XMLStorage.generateFolderEnd (sb, "h3", "        ");
            XMLStorage.generateFolderEnd (sb, "td", "        ");
            XMLStorage.generateFolderEnd (sb, "tr", "      ");
            
            // print body of one category
            exportShortcutsToHTML3 (sb, keymapModel, category, keymaps, displayHumanReadibleShortcuts);
        }
    }

    /**
     * Writes body of given category.
     */
    private static void exportShortcutsToHTML3 (
        StringBuffer sb, 
        KeymapModel keymapModel, 
        String category,
        Map<String, Map<ShortcutAction, Set<String>>> keymaps, 
	boolean displayHumanReadibleShortcuts
    ) {
        Set<ShortcutAction> actions = keymapModel.getActions (category);

        // sort actions
        Map<String, ShortcutAction> sortedActions = new TreeMap<String, ShortcutAction> ();
        for (ShortcutAction action: actions) {
            sortedActions.put (
                action.getDisplayName (), 
                action
            );
        }

        // print actions
        Attribs attribs = new Attribs (true);
        for (Map.Entry<String, ShortcutAction> entry: sortedActions.entrySet()) {
            String actionName = entry.getKey();
            ShortcutAction action = entry.getValue();

            // print action name to the first column
            XMLStorage.generateFolderStart (sb, "tr", attribs, "      ");
            XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
            sb.append (actionName);
            XMLStorage.generateFolderEnd (sb, "td", "        ");
            
            for (Map<ShortcutAction, Set<String>> keymap: keymaps.values ()) {
                Set<String> shortcuts = keymap.get (action);

                XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
                printShortcuts (shortcuts, sb, displayHumanReadibleShortcuts);
                XMLStorage.generateFolderEnd (sb, "td", "        ");
            }
            
            XMLStorage.generateFolderEnd (sb, "tr", "      ");
        }
    }
    
    private static void printShortcuts (Set<String> shortcuts, StringBuffer sb, boolean displayHumanReadibleShortcuts) {
        if (shortcuts == null) {
            sb.append ('-');
            return;
        }
        Iterator<String> it = shortcuts.iterator ();
        while (it.hasNext ()) {
            String shortcut = it.next ();
	    if (displayHumanReadibleShortcuts) {
		//show system specific shortcuts like CTRL-SHIFT-ALT
		sb.append (portableRepresentationToShortcut(shortcut));
	    } else {
		//default: show portable shortcuts like D-O
		sb.append (shortcut);
	    }
            if (it.hasNext ()) sb.append (", ");
        }
    }
    
    /**
     * Converts the portable shortcut representation to a human-readable shortcut
     * @param portable portable representation (the storage format for shortcuts)
     * @return human-readable string
     */
    static String portableRepresentationToShortcut(String portable) {
        assert portable != null : "The parameter must not be null"; //NOI18N

        StringBuilder buf = new StringBuilder();
        String delimiter = " "; //NOI18N

        for(StringTokenizer st = new StringTokenizer(portable, delimiter); st.hasMoreTokens();) { //NOI18N
            String ks = st.nextToken().trim();

            KeyStroke keyStroke = Utilities.stringToKey(ks);

            if (keyStroke != null) {
                buf.append(KeyStrokeUtils.getKeyStrokeAsText(keyStroke));
                if (st.hasMoreTokens())
                    buf.append(' ');
            } else {
                return null;
            }
        }

        return buf.toString();
    }
    
    private static void generateLayersXML (
        LayersBridge layersBridge, 
        Map<String, Map<String, ShortcutAction>> categoryToActions
    ) {
        Writer fw = null;
        try {
            fw = openWriter ();
            if (fw == null) return;

            StringBuffer sb = XMLStorage.generateHeader ();
            Attribs attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "filesystem", attribs, "");
            attribs.add ("name", "Keymaps");
                XMLStorage.generateFolderStart (sb, "folder", attribs, "    ");
                    generateShadowsToXML (layersBridge, sb, categoryToActions, "        ");
                XMLStorage.generateFolderEnd (sb, "folder", "    ");
            XMLStorage.generateFolderEnd (sb, "filesystem", "");
            fw.write (sb.toString ());
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (e);
        } finally {
            try {
                if (fw != null) {
                    fw.flush ();
                    fw.close ();
                }
            } catch (IOException e) {}
        }
    }
    
    private static void generateEditorXML (
        Map<ShortcutAction, Set<String>> actionToShortcuts
    ) {
        Writer fw = null;
        try {
            fw = openWriter ();
            if (fw == null) return;

            StringBuffer sb = XMLStorage.generateHeader ();
            Attribs attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "bindings", attribs, "");
            
            Map<String, Set<String>> sortedMap = new TreeMap<String, Set<String>> ();
            for (Map.Entry<ShortcutAction, Set<String>> entry : actionToShortcuts.entrySet()) {
                sortedMap.put(entry.getKey().getDisplayName(), entry.getValue());
            }
            for (Map.Entry<String, Set<String>> entry: sortedMap.entrySet ()) {
                String actionName = entry.getKey ();
                Set<String> shortcuts = entry.getValue();
                for (String shortcut: shortcuts) {
                    attribs = new Attribs (true);
                    attribs.add ("actionName", actionName);
                    attribs.add ("key", shortcut);
                    XMLStorage.generateLeaf (sb, "bind", attribs, "  ");
                }
            }
            
            XMLStorage.generateFolderEnd (sb, "bindings", "");
            fw.write (sb.toString ());
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (e);
        } finally {
            try {
                if (fw != null) {
                    fw.flush ();
                    fw.close ();
                }
            } catch (IOException e) {}
        }
    }
    
    private static Map<String, Map<String, ShortcutAction>> resolveNames (Map<String, Set<ShortcutAction>> categoryToActions) {
        Map<String, Map<String, ShortcutAction>> result = new HashMap<String, Map<String, ShortcutAction>> ();
        for (Map.Entry<String, Set<ShortcutAction>> entry: categoryToActions.entrySet ()) {
            String category = entry.getKey();
            Set<ShortcutAction> actions = entry.getValue();
            Map<String, ShortcutAction> actionsMap = new HashMap<String, ShortcutAction> ();
            for (ShortcutAction action: actions) {
                actionsMap.put (action.getDisplayName (), action);
            }
            result.put (category, actionsMap);
        }
        return result;
    }
    
    /**
     * Converts:
     * Map (String (profile | category) > Map (String (category)) |
     *                                    ShortcutAction)
     * to xml. 
     *   (String > Map) is represented by folder and
     *   (String > DataObject) by ShadowDO
     */
    private static void generateShadowsToXML (
        LayersBridge        layersBridge,
        StringBuffer        sb,
        Map<String, Map<String, ShortcutAction>> shortcutToAction,
        String              indentation
    ) {
        Iterator<String> it = shortcutToAction.keySet ().iterator ();
        while (it.hasNext ()) {
            String key = it.next ();
            Map<String, ShortcutAction> value = shortcutToAction.get (key);
            Attribs attribs = new Attribs (true);
            attribs.add ("name", key);
            XMLStorage.generateFolderStart (sb, "folder", attribs, indentation);
            generateShadowsToXML2 (
                layersBridge,
                sb, 
                value, 
                "    " + indentation
            );
            XMLStorage.generateFolderEnd (sb, "folder", indentation);
        }
    }
    
    private static void generateShadowsToXML2 (
        LayersBridge        layersBridge,
        StringBuffer        sb,
        Map<String, ShortcutAction> shortcutToAction,
        String              indentation
    ) {
        Iterator<String> it = shortcutToAction.keySet ().iterator ();
        while (it.hasNext ()) {
            String key = it.next ();
            ShortcutAction value = shortcutToAction.get (key);

            DataObject dob = layersBridge.getDataObject (value);
            if (dob == null) {
                System.out.println("no Dataobject " + value);
                continue;
            }
            FileObject fo = dob.getPrimaryFile ();
            Attribs attribs = new Attribs (true);
            attribs.add ("name", key + ".shadow");
            XMLStorage.generateFolderStart (sb, "file", attribs, indentation);
                Attribs attribs2 = new Attribs (true);
                attribs2.add ("name", "originalFile");
                attribs2.add ("stringvalue", fo.getPath ());
                XMLStorage.generateLeaf (sb, "attr", attribs2, indentation + "    ");
            XMLStorage.generateFolderEnd (sb, "file", indentation);
        }
    }
    
    private static Writer openWriter () throws IOException {
        JFileChooser fileChooser = new JFileChooser ();
        int result = fileChooser.showSaveDialog 
            (WindowManager.getDefault ().getMainWindow ());
        if (result != JFileChooser.APPROVE_OPTION) return null;
        File f = fileChooser.getSelectedFile ();
        return new FileWriter (f);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (ExportShortcutsAction.class, key);
    }
}

