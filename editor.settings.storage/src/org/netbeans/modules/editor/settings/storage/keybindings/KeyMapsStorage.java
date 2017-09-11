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

package org.netbeans.modules.editor.settings.storage.keybindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.netbeans.modules.editor.settings.storage.spi.StorageReader;
import org.netbeans.modules.editor.settings.storage.spi.StorageWriter;
import org.netbeans.modules.editor.settings.storage.spi.support.StorageSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * @author Jan Jancura, Vita Stejskal
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.settings.storage.spi.StorageDescription.class)
public final class KeyMapsStorage implements StorageDescription<Collection<KeyStroke>, MultiKeyBinding> {

    // -J-Dorg.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage.level=FINE
    private static final Logger LOG = Logger.getLogger(KeyMapsStorage.class.getName());

    public static final String ID = "Keybindings"; //NOI18N
    /* test */ static final String MIME_TYPE = "text/x-nbeditor-keybindingsettings"; //NOI18N
        
    // ---------------------------------------------------------
    // StorageDescription implementation
    // ---------------------------------------------------------
    
    public KeyMapsStorage() {
    }

    public String getId() {
        return ID;
    }

    public boolean isUsingProfiles() {
        return true;
    }

    public String getMimeType() {
        return MIME_TYPE;
    }

    public String getLegacyFileName() {
        return "keybindings.xml"; //NOI18N
    }

    public StorageReader<Collection<KeyStroke>, MultiKeyBinding> createReader(FileObject f, String mimePath) {
        return new KeyMapsReader(f, mimePath);
    }

    public StorageWriter<Collection<KeyStroke>, MultiKeyBinding> createWriter(FileObject f, String mimePath) {
        return new KeyMapsWriter();
    }

    // ---------------------------------------------------------
    // Private implementation
    // ---------------------------------------------------------
    
    private static final String ROOT = "bindings"; //NOI18N
    private static final String E_BIND = "bind"; //NOI18N
    private static final String A_ACTION_NAME = "actionName"; //NOI18N
    private static final String A_KEY = "key"; //NOI18N
    private static final String A_REMOVE = "remove"; //NOI18N
    private static final String V_TRUE = "true"; //NOI18N
        
    private static final String PUBLIC_ID = "-//NetBeans//DTD Editor KeyBindings settings 1.1//EN"; //NOI18N
    private static final String SYSTEM_ID = "http://www.netbeans.org/dtds/EditorKeyBindings-1_1.dtd"; //NOI18N

    private static class KeyMapsReader extends StorageReader<Collection<KeyStroke>, MultiKeyBinding> {
        private Map<Collection<KeyStroke>, MultiKeyBinding> keyMap = new HashMap<Collection<KeyStroke>, MultiKeyBinding>();
        private Set<Collection<KeyStroke>> removedShortcuts = new HashSet<Collection<KeyStroke>>();

        public KeyMapsReader(FileObject f, String mimePath) {
            super(f, mimePath);
            LOG.log(Level.FINEST, "Processing file: {0}", f.getPath());
        }
        
        public Map<Collection<KeyStroke>, MultiKeyBinding> getAdded() {
            return keyMap;
        }
        
        public Set<Collection<KeyStroke>> getRemoved() {
            return removedShortcuts;
        }
        
        public @Override void startElement(
            String uri,
            String localName,
            String name,
            Attributes attributes
        ) throws SAXException {
            try {
                if (name.equals(ROOT)) {
                    // We don't read anything from the root element
                    
                } else if (name.equals(E_BIND)) {
                    String key = attributes.getValue(A_KEY);
                    
                    if (isModuleFile() && isDefaultProfile() && key != null && key.length() > 0) {
                        // check the key, it should never start with 'A' or 'C', because
                        // these characters do not work on MAC, Alt should be coded as 'O'
                        // and Ctrl as 'D'
                        int idx = key.indexOf('-'); //NOI18N
                        String proccessedFilePath = getProcessedFile().getPath();
                        if (idx != -1 && (key.charAt(0) == 'A' || key.charAt(0) == 'C') && !proccessedFilePath.endsWith("-mac.xml")) { //NOI18N
                            LOG.warning("The keybinding '" + key + //NOI18N
                                "' in " + proccessedFilePath + " may not work correctly on Mac. " + //NOI18N
                                "Keybindings starting with Alt or Ctrl should " + //NOI18N
                                "be coded with latin capital letters 'O' " + //NOI18N
                                "or 'D' respectively. For details see org.openide.util.Utilities.stringToKey()."); //NOI18N
                        }
                    }
                    
                    KeyStroke[] shortcut = Utilities.stringToKeys(key.replaceAll("\\$", " ")); // NOI18N
                    String remove = attributes.getValue(A_REMOVE);
                    
                    if (Boolean.valueOf(remove)) {
                        removedShortcuts.add(Arrays.asList(shortcut));
                    } else {
                        String actionName = attributes.getValue(A_ACTION_NAME);
                        if (actionName != null) {
                            MultiKeyBinding mkb = new MultiKeyBinding(shortcut, actionName);
                            LOG.fine("Adding: Key: '" + key + "' Action: '" + mkb.getActionName() + "'");
                            MultiKeyBinding duplicate = keyMap.put(mkb.getKeyStrokeList(), mkb);
                            if (duplicate != null && !duplicate.getActionName().equals(mkb.getActionName())) {
                                LOG.warning("Duplicate shortcut '" + key + "' definition; rebound from '" + duplicate.getActionName() //NOI18N
                                        + "' to '" + mkb.getActionName() + "' in (" + getProcessedFile().getPath() + ")."); //NOI18N
                            }
                        } else {
                            LOG.warning("Ignoring keybinding '" + key + "' with no action name."); //NOI18N
                        }
                    }
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Can't parse keybindings file " + getProcessedFile().getPath(), ex); //NOI18N
            }
        }
    } // End of KeyMapsReader class

    private static final class KeyMapsWriter extends StorageWriter<Collection<KeyStroke>, MultiKeyBinding> {
        
        public KeyMapsWriter() {
        }
        
        public Document getDocument() {
            Document doc = XMLUtil.createDocument(ROOT, null, PUBLIC_ID, SYSTEM_ID);
            Node root = doc.getElementsByTagName(ROOT).item(0);

            List<MultiKeyBinding> added = new ArrayList<MultiKeyBinding>(getAdded().values());
            Collections.sort(added, ACTION_NAME_COMPARATOR);
            for(MultiKeyBinding mkb : added) {
                Element bind = doc.createElement(E_BIND);
                root.appendChild(bind);

                bind.setAttribute(A_ACTION_NAME, mkb.getActionName());
                bind.setAttribute(A_KEY, StorageSupport.keyStrokesToString(mkb.getKeyStrokeList(), true));
            }

            for(Collection<KeyStroke> keyStrokes : getRemoved()) {
                String shortcut = StorageSupport.keyStrokesToString(keyStrokes, true);
                Element bind = doc.createElement(E_BIND);
                root.appendChild(bind);

                bind.setAttribute(A_KEY, shortcut);
                bind.setAttribute(A_REMOVE, V_TRUE);
            }
            
            return doc;
        }

        private static final Comparator<MultiKeyBinding> ACTION_NAME_COMPARATOR = new Comparator<MultiKeyBinding>() {
            public int compare(MultiKeyBinding mkb1, MultiKeyBinding mkb2) {
                String actionName1 = mkb1.getActionName();
                String actionName2 = mkb2.getActionName();
                return actionName1.compareToIgnoreCase(actionName2);
            }
        };
    } // End of KeyMapsWriter class
    
}
