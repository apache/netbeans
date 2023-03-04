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
package org.netbeans.modules.options.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.options.editor.spi.OptionsFilter;
import org.netbeans.modules.options.util.LanguagesComparator;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Jancura, Dusan Balek
 */
public final class FolderBasedController extends OptionsPanelController implements PropertyChangeListener {

    private static final String OPTIONS_SUB_FOLDER = "optionsSubFolder"; //NOI18N
    private static final String HELP_CTX_ID = "helpContextId"; //NOI18N
    private static final String ALLOW_FILTERING = "allowFiltering"; //NOI18N
    private static final String BASE_FOLDER = "OptionsDialog/Editor/"; //NOI18N
    private static FolderBasedController hintsController;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final String folder;
    private final HelpCtx helpCtx;
    private Lookup masterLookup;
    private FolderBasedOptionPanel panel;
    private Map<String, OptionsPanelController> mimeType2delegates;
    private final boolean allowFiltering;
    private final Set<String> supportFiltering = new HashSet<String>();
    private final Document filterDocument = new PlainDocument();

    @OptionsPanelController.SubRegistration(
	id="Hints",
        displayName="#CTL_Hints_DisplayName",
        location=OptionsDisplayer.EDITOR,
        keywords="#KW_Hints",
        keywordsCategory="Editor/Hints",
        position=400
//        toolTip="#CTL_Hints_ToolTip"
    )
    public static OptionsPanelController hints() {
        if (hintsController == null) {
            hintsController = new FolderBasedController("Hints/", "netbeans.optionsDialog.editor.hints", true);
        }
        return hintsController;
    }

    @OptionsPanelController.SubRegistration(
	id="MarkOccurrences",
        displayName="#CTL_MarkOccurences_DisplayName",
        location=OptionsDisplayer.EDITOR,
        keywords="#KW_Mark",
        keywordsCategory="Editor/MarkOccurrences",
        position=500
//        toolTip="#CTL_MarkOccurences_ToolTip"
    )
    public static OptionsPanelController markOccurrences() {
        return new FolderBasedController("MarkOccurrences/", "netbeans.optionsDialog.editor.markOccurences", false);
    }

    @OptionsPanelController.SubRegistration(
	id="InlineHints",
        displayName="#CTL_InlineHints_DisplayName",
        location=OptionsDisplayer.EDITOR,
        keywords="#KW_InlineHints",
        keywordsCategory="Editor/InlineHints",
        position=500
//        toolTip="#CTL_MarkOccurences_ToolTip"
    )
    public static OptionsPanelController inlineHints() {
        return new FolderBasedController("InlineHints/", "netbeans.optionsDialog.editor.inlineHints", false);
    }

    public static OptionsPanelController create (Map args) {
        FolderBasedController folderBasedController = new FolderBasedController(
                (String) args.get (OPTIONS_SUB_FOLDER),
                (String) args.get (HELP_CTX_ID),
                (Boolean) args.get (ALLOW_FILTERING)
        );

        return folderBasedController;
    }

    private FolderBasedController(String subFolder, String helpCtxId, boolean allowFiltering) {
        folder = subFolder != null ? BASE_FOLDER + subFolder : BASE_FOLDER;
        helpCtx = helpCtxId != null ? new HelpCtx(helpCtxId) : null;
        this.allowFiltering = allowFiltering;
    }    
    
    private void saveSelectedLanguage() {
        String selectedLanguage = panel.getSelectedLanguage();
        if(selectedLanguage != null) {
            NbPreferences.forModule(FolderBasedController.class).put(folder, selectedLanguage);
        }
    }
    
    String getSavedSelectedLanguage() {
        return NbPreferences.forModule(FolderBasedController.class).get(folder, null);
    }
    
    public final synchronized void update() {
        for (Entry<String, OptionsPanelController> e : getMimeType2delegates ().entrySet()) {
            OptionsFilter f = OptionsFilter.create(filterDocument, new FilteringUsedCallback(e.getKey()));
            Lookup innerLookup = new ProxyLookup(masterLookup, Lookups.singleton(f));
            OptionsPanelController c = e.getValue();
            c.getComponent(innerLookup);
            c.update();
        }

        assert panel != null;
        panel.update ();
    }
    
    public final synchronized void applyChanges() {
        Collection<? extends OptionsPanelController> controllers = getMimeType2delegates ().values();
        for(OptionsPanelController c : controllers) {
            c.applyChanges();
        }

        mimeType2delegates = null;
        saveSelectedLanguage();
    }
    
    public final synchronized void cancel() {
        Collection<? extends OptionsPanelController> controllers = getMimeType2delegates ().values();
        for(OptionsPanelController c : controllers) {
            c.cancel();
        }
        
        mimeType2delegates = null;
        saveSelectedLanguage();
    }
    
    public final synchronized boolean isValid() {
        Collection<? extends OptionsPanelController> controllers = getMimeType2delegates ().values();
        for(OptionsPanelController c : controllers) {
            if (!c.isValid()) {
                return false;
            }
        }
        return true;
    }
    
    public final synchronized boolean isChanged() {
        Collection<? extends OptionsPanelController> controllers = getMimeType2delegates ().values();
        for(OptionsPanelController c : controllers) {
            if (c.isChanged()) {
                return true;
            }
        }
        return false;
    }
    
    public final HelpCtx getHelpCtx() {
        return helpCtx;
    }

    @Override
    public synchronized JComponent getComponent(Lookup masterLookup) {
        if (panel == null) {
            this.masterLookup = masterLookup;
            for (Entry<String, OptionsPanelController> e : getMimeType2delegates ().entrySet()) {
                OptionsFilter f = OptionsFilter.create(filterDocument, new FilteringUsedCallback(e.getKey()));
                Lookup innerLookup = new ProxyLookup(masterLookup, Lookups.singleton(f));
                OptionsPanelController controller = e.getValue();
                controller.getComponent(innerLookup);
                controller.addPropertyChangeListener(this);
            }
            panel = new FolderBasedOptionPanel(this, filterDocument, allowFiltering);
        }
        return panel;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }
        
    @Override
    public Lookup getLookup() {
        return super.getLookup();
    }

    @Override
    protected void setCurrentSubcategory(String subpath) {
        for (Entry<String, OptionsPanelController> e : getMimeType2delegates().entrySet()) {
            if (subpath.startsWith(e.getKey())) {
                panel.setCurrentMimeType(e.getKey());
                subpath = subpath.substring(e.getKey().length());

                if (subpath.length() > 0 && subpath.startsWith("/")) {
                    e.getValue().setSubcategory(subpath.substring(1));
                }

                return ;
            }
        }

        Logger.getLogger(FolderBasedController.class.getName()).log(Level.WARNING, "setCurrentSubcategory: cannot open: {0}", subpath);
    }

    /**
     * @return Copy of the list of mime types sorted by display name
     */
    List<String> getMimeTypes() {
        List<String> mimeTypes = new ArrayList<>(getMimeType2delegates().keySet());
        mimeTypes.sort(LanguagesComparator.INSTANCE);
        return mimeTypes;
    }
    
    OptionsPanelController getController(String mimeType) {
        return getMimeType2delegates ().get(mimeType);
    }

    private Map<String, OptionsPanelController> getMimeType2delegates () {
        if (mimeType2delegates == null) {
            mimeType2delegates = new LinkedHashMap<String, OptionsPanelController>();
            for (String mimeType : EditorSettings.getDefault().getAllMimeTypes()) {
                Lookup l = Lookups.forPath(folder + mimeType);
                OptionsPanelController controller = l.lookup(OptionsPanelController.class);
                if (controller != null) {
                    mimeType2delegates.put(mimeType, controller);
                }
            }
        }
        return mimeType2delegates;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        pcs.firePropertyChange(evt);
    }

    boolean supportsFilter(String mimeType) {
        return supportFiltering.contains(mimeType);
    }

    private final class FilteringUsedCallback implements Runnable {
        private final String mimeType;

        public FilteringUsedCallback(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        public void run() {
            FolderBasedController.this.supportFiltering.add(mimeType);
            FolderBasedOptionPanel panel;
            
            synchronized (FolderBasedController.this) {
                panel = FolderBasedController.this.panel;
            }

            if (panel != null) {
                panel.searchEnableDisable();
            }
        }
    }

}
