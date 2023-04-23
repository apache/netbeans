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

package org.netbeans.modules.languages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;

import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.features.ActionCreator;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.modules.languages.features.ColorsManager;
import org.netbeans.modules.languages.features.LocalizationSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesManager extends org.netbeans.api.languages.LanguagesManager {
    
    private static LanguagesManager languagesManager;
   private static final RequestProcessor RP = new RequestProcessor(LanguagesManager.class.getName(), 1);
    
    public static LanguagesManager getDefault () {
        if (languagesManager == null)
            languagesManager = new LanguagesManager ();
        return languagesManager;
    }

    public boolean isSupported (String mimeType) {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        return fs.findResource ("Editors/" + mimeType + "/language.nbs") != null;
    }

    public boolean createDataObjectFor (String mimeType) {
        if(!isSupported(mimeType)) {
            return false;
        }
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        FileObject fo = fs.findResource ("Editors/" + mimeType);
        if (fo == null) return false;
        Boolean b = (Boolean) fo.getAttribute ("createDataObject");
        if (b == null) return true;
        return b.booleanValue ();
    }
    
    private Language parsingLanguage = Language.create ("parsing...");
    
    private Map<String,Language> mimeTypeToLanguage = new HashMap<String,Language> ();
    
    // [PENDING, XXX, HACK] workaround for internal mime type set by options for coloring preview
    public static String normalizeMimeType(String mimeType) {
        if (mimeType.startsWith("test")) { //NOI18N
            int idx = mimeType.indexOf('_'); //NOI18N
            if (idx < 0) return mimeType;
            mimeType = mimeType.substring(idx + 1);
        }
        return mimeType;
    }
    
    public synchronized Language getLanguage (String mimeType) 
    throws LanguageDefinitionNotFoundException {
        mimeType = normalizeMimeType(mimeType);
        if (!mimeTypeToLanguage.containsKey (mimeType)) {
            mimeTypeToLanguage.put (mimeType, parsingLanguage);
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject fo = fs.findResource ("Editors/" + mimeType + "/language.nbs");
            if (fo == null) {
                mimeTypeToLanguage.remove (mimeType);
                throw new LanguageDefinitionNotFoundException 
                    ("Language definition for " + mimeType + " not found.");
            }
            addListener (fo);
            try {
                NBSLanguageReader reader = NBSLanguageReader.create (fo, mimeType);
                final LanguageImpl language = new LanguageImpl (mimeType, reader);
                language.addPropertyChangeListener (new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        initLanguage (language);
                        language.removePropertyChangeListener (this);
                    }
                });
                final String mimeType2 = mimeType;
                RP.post(new Runnable() {
                    public void run () {
                        try {
                            language.read ();
                        } catch (ParseException ex) {
                            Utils.message ("Editors/" + mimeType2 + "/language.nbs: " + ex.getMessage ());
                        } catch (IOException ex) {
                            Utils.message ("Editors/" + mimeType2 + "/language.nbs: " + ex.getMessage ());
                        }
                    }
                }, 2000);
                mimeTypeToLanguage.put (mimeType, language);
            } catch (IOException ex) {
                mimeTypeToLanguage.put (mimeType, Language.create (mimeType));
                Utils.message ("Editors/" + mimeType + "/language.nbs: " + ex.getMessage ());
            }
        }
        if (parsingLanguage == mimeTypeToLanguage.get (mimeType))
            throw new IllegalArgumentException ();
        return mimeTypeToLanguage.get (mimeType);
    }
    
    public void addLanguage (Language l) {
        mimeTypeToLanguage.put (l.getMimeType (), l);
    }

    
    // helper methods .....................................................................................................
    
    private void languageChanged (String mimeType) {
        Language language = mimeTypeToLanguage.get (mimeType);
        if (language instanceof LanguageImpl) {
            try {
                FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
                FileObject fo = fs.findResource ("Editors/" + mimeType + "/language.nbs");
                if (fo == null) {
                    mimeTypeToLanguage.remove (mimeType);
                    throw new LanguageDefinitionNotFoundException 
                        ("Language definition for " + mimeType + " not found.");
                }
                NBSLanguageReader reader = NBSLanguageReader.create (fo, mimeType);
                ((LanguageImpl) language).read (reader);
            } catch (ParseException ex) {
                Utils.message ("Editors/" + mimeType + "/language.nbs: " + ex.getMessage ());
            } catch (IOException ex) {
                Utils.message ("Editors/" + mimeType + "/language.nbs: " + ex.getMessage ());
            }
        }
        
        // HACK
        ParserManagerImpl.refreshHack ();
    }

    private Set<FileObject> listeningOn = new HashSet<FileObject> ();
    private Listener listener;
    
    private void addListener (FileObject fo) {
        if (!listeningOn.contains (fo)) {
            if (listener == null)
                listener = new Listener ();
            fo.addFileChangeListener (listener);
            listeningOn.add (fo);
        }
    }
    
    private static void initLanguage (final Language l) {
        try {
            final FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            final FileObject root = fs.findResource ("Editors/" + l.getMimeType ());
            fs.runAtomicAction (new AtomicAction () {
                public void run () {
                    try {
                        // init code folding bar
                        if (root.getFileObject ("SideBar/org-netbeans-modules-languages-features-CodeFoldingSideBarFactory.instance") == null &&
                            l.getFeatureList ().getFeatures ("FOLD").size () > 0
                        ) {
                            FileUtil.createData (root, "FoldManager/org-netbeans-modules-languages-features-LanguagesFoldManager$Factory.instance");
                            FileUtil.createData(root, "SideBar/org-netbeans-modules-languages-features-CodeFoldingSideBarFactory.instance").
                                    // Can tune position to whatever seems right; at least put after org-netbeans-editor-GlyphGutter.instance:
                                    setAttribute("position", 1000);
                        }

                        // init error stripe
                        if (root.getFileObject ("UpToDateStatusProvider/org-netbeans-modules-languages-features-UpToDateStatusProviderFactoryImpl.instance") == null
                            //l.supportsCodeFolding ()  does not work if you first open language without folding than no languages will have foding.
                        )
                            FileUtil.createData (root, "UpToDateStatusProvider/org-netbeans-modules-languages-features-UpToDateStatusProviderFactoryImpl.instance");


                        initPopupMenu (root, l);

                        // init navigator
                        if (l.getFeatureList ().getFeatures ("NAVIGATOR").size () > 0) {
                            String foldFileName = "Navigator/Panels/" + l.getMimeType () + 
                                "/org-netbeans-modules-languages-features-LanguagesNavigator.instance";
                            if (fs.findResource (foldFileName) == null)
                                FileUtil.createData (fs.getRoot (), foldFileName);
                        }

                        // init tooltips
                        FileUtil.createData (root, "ToolTips/org-netbeans-modules-languages-features-ToolTipAnnotation.instance");

                        if (l.getFeatureList ().getFeature ("COMMENT_LINE") != null) {
                            // init editor toolbar
                            FileObject toolbarDefault = FileUtil.createFolder (root, "Toolbars/Default");
                            createSeparator(
                                    toolbarDefault,
                                    "Separator-before-comment",
                                    30000 // can tune to whatever; want after stop-macro-recording
                            );
                            FileUtil.createData(toolbarDefault, "comment").setAttribute("position", 31000);
                            FileUtil.createData(toolbarDefault, "uncomment").setAttribute("position", 32000);

                            if (root.getFileObject("Keybindings/NetBeans/Defaults/keybindings.xml") == null) {
                                InputStream is = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/languages/resources/DefaultKeyBindings.xml");
                                try {
                                    FileObject bindings = FileUtil.createData(root, "Keybindings/NetBeans/Defaults/keybindings.xml");
                                    OutputStream os = bindings.getOutputStream();
                                    try {
                                        FileUtil.copy(is, os);
                                    } finally {
                                        os.close();
                                    }
                                } finally {
                                    is.close();
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Utils.notify (ex);
                    }
                }
            });
        } catch (IOException ex) {
            Utils.notify (ex);
        }
        
        // init coloring
        ColorsManager.initColorings (l);
    }

    private static void initPopupMenu (FileObject root, Language l) throws IOException {
            List<Feature> actions = l.getFeatureList ().getFeatures("ACTION");
            // Could probably use fixed anchor points if these positions settle down:
            int selectInPos = findPositionOfDefaultPopupAction("org-netbeans-modules-editor-NbSelectInPopupAction.instance", 1000);
            int increment = (findPositionOfDefaultPopupAction("org-openide-actions-CutAction.instance", 2000) - selectInPos) / (actions.size() + 3);
            FileObject popup = FileUtil.createFolder (root, "Popup");
            int pos = selectInPos + increment;
            createSeparator(popup, "SeparatorAfterSelectInPopupAction", pos);
            boolean actionAdded = false;
            //if (l.getFeatureList ().getFeatures("SEMANTIC_USAGE").size() > 0) {
                actionAdded = true;
                pos += increment;
                FileUtil.createData (popup, "org-netbeans-modules-languages-features-GoToDeclarationAction.instance").setAttribute("position", pos);
            //}
            if (l.getFeatureList ().getFeatures("INDENT").size() > 0) {
                actionAdded = true;
                pos += increment;
                FileUtil.createData (popup, "format").setAttribute("position", pos);
            }
            for (Feature action : actions) {
                if (action.getBoolean ("explorer", false))
                    continue;
                actionAdded = true;
                pos += increment;
                String name = action.getSelector ().getAsString ();
                String displayName= LocalizationSupport.localize (l, (String)action.getValue ("name"));
                String performer = action.getMethodName ("performer");
                String enabler = action.getMethodName ("enabled");
                /* XXX disabled for now; could use numeric position key if desired:
                String installAfter = (String) action.getValue ("install_after");
                String installBefore = (String) action.getValue ("install_before");
                 */
                boolean separatorBefore = action.getBoolean ("separator_before", false);
                boolean separatorAfter = action.getBoolean ("separator_after", false);
                FileObject fobj = FileUtil.createData (popup, name + ".instance"); // NOI18N
                fobj.setAttribute("instanceCreate", new ActionCreator (new Object[] {displayName, performer, enabler})); // NOI18N
                fobj.setAttribute("instanceClass", "org.netbeans.modules.languages.features.GenericAction"); // NOI18N
                fobj.setAttribute("position", pos);
                if (separatorBefore) {
                    createSeparator(popup, name + "_separator_before", pos - increment / 3);
                }
                if (separatorAfter) {
                    createSeparator(popup, name + "_separator_after", pos + increment / 3);
                }
            }
            //FileUtil.createData (popup, "org-netbeans-modules-languages-features-FormatAction.instance").setAttribute("position", ...);
            if (actionAdded) {
                createSeparator(popup, "SeparatorBeforeCut", pos + increment);
            }
            if (l.getFeatureList ().getFeatures("FOLD").size() > 0) {
                FileUtil.createData (popup, "generate-fold-popup").setAttribute(
                    "position",
                    findPositionOfDefaultPopupAction("org-openide-actions-PasteAction.instance", 3000) + 50
                );
            }
            // init actions
    }
    
    private static int findPositionOfDefaultPopupAction(String name, int fallback) {
        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource("Editors/Popup/" + name);
        if (f != null) {
            Object pos = f.getAttribute("position");
            if (pos instanceof Integer) {
                return (Integer) pos;
            }
        }
        return fallback;
    }
    
    private static void createSeparator (
        FileObject      folder,
        String          name,
        int position
    ) throws IOException {
        FileObject separator = FileUtil.createData(folder, name + ".instance");
        separator.setAttribute ("instanceClass", "javax.swing.JSeparator");
        separator.setAttribute("position", position);
    }
    
    
    // innerclasses ............................................................
    
    private class Listener implements FileChangeListener {
        
        public void fileAttributeChanged (FileAttributeEvent fe) {
        }
        public void fileChanged (FileEvent fe) {
            FileObject fo = fe.getFile ();
            String mimeType = fo.getParent ().getParent ().getName () + 
                '/' + fo.getParent ().getName ();
            languageChanged (mimeType);
        }
        public void fileDataCreated (FileEvent fe) {
        }
        public void fileDeleted (FileEvent fe) {
            FileObject fo = fe.getFile ();
            String mimeType = fo.getParent ().getName ();
            languageChanged (mimeType);
        }
        public void fileFolderCreated (FileEvent fe) {
        }
        public void fileRenamed (FileRenameEvent fe) {
        }
    }
    
}



