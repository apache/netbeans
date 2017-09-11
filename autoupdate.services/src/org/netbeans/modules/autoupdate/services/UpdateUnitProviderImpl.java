/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.services;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.autoupdate.*;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogFactory;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.netbeans.modules.autoupdate.updateprovider.LocalNBMsProvider;
import org.netbeans.modules.autoupdate.updateprovider.ProviderCategory;
import org.netbeans.spi.autoupdate.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


/** <code>UpdateProvider</code> providers items for Autoupdate infrastructure. The items
 * are available on e.g. Update Center. Items can represents NetBeans Module,
 * its Localization, Feature as group of NetBeans Modules or special
 * components which needs own native installer to make them accessible in NetBeans product.
 *
 * @author Jiri Rechtacek
 */
public final class UpdateUnitProviderImpl {

    private UpdateProvider provider;
    private static final Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.services.UpdateUnitProviderImpl");
    private static final String REMOVED_MASK ="_removed";
    private static final String URL = "url";
    private static final String DISPLAY_NAME = "displayName";    
    private static final String ENABLED = "enabled";
    private static final String CATEGORY_NAME = "categoryName";
    private static final LookupListenerImpl UPDATE_PROVIDERS = new LookupListenerImpl();
            
    public UpdateUnitProviderImpl (UpdateProvider provider) {
        this.provider = provider;
    }
    
    /** Name of provider, this name is used by Autoupdate infrastructure for manimulating
     * of providers.
     * 
     * @return name of provider
     */
    public String getName () {
        return getUpdateProvider ().getName ();
    }
    
    public String getDescription () {
        return getUpdateProvider ().getDescription ();
    }

    public CATEGORY getCategory() {
        return getUpdateProvider().getCategory();
    }
    public Image getSourceIcon() {
        UpdateProvider up = getUpdateProvider();
        if (up instanceof AutoupdateCatalogProvider) {
            return ((AutoupdateCatalogProvider)up).getProviderCategory().getIcon();
        }
        return ProviderCategory.forValue(CATEGORY.COMMUNITY).getIcon();
    }
    public String getSourceDescription() {
        UpdateProvider up = getUpdateProvider();
        if (up instanceof AutoupdateCatalogProvider) {
            return ((AutoupdateCatalogProvider) up).getProviderCategory().getDisplayName();
        }
        return ProviderCategory.forValue(CATEGORY.COMMUNITY).getDisplayName();
    }

    public String getContentDescription() {
        UpdateProvider up = getUpdateProvider();
        if (up instanceof AutoupdateCatalogProvider) {
            return ((AutoupdateCatalogProvider) up).getContentDescription();
        }
        return null;
    }

    /** Display name of provider. This display name can be visualized in UI.
     * 
     * @return display name of provider
     */
    public String getDisplayName () {
        return loadDisplayName (getUpdateProvider ());
    }
    
    public void setDisplayName (String name) {
        storeDisplayName (getUpdateProvider (), name);
    }
    
    public URL getProviderURL () {
        return loadUrl (getUpdateProvider ());
    }
    
    public void setProviderURL (URL url) {
        storeUrl (getUpdateProvider (), url);
    }
    
    public List<UpdateUnit> getUpdateUnits (UpdateManager.TYPE... types) {
        return UpdateManagerImpl.getUpdateUnits (getUpdateProvider (), types);
    }
    
    /** Make refresh of content of the provider. The content can by read from
     * a cache. The <code>force</code> parameter forces reading content from
     * remote server.
     * 
     * @param force if true then forces to reread the content from server
     * @return true if refresh succeed
     * @throws java.io.IOException when any network problem is encountered
     */
    public boolean refresh (ProgressHandle handle, boolean force) throws IOException {
        boolean res = false;
        if (handle != null) {
            handle.progress (NbBundle.getMessage (UpdateUnitProviderImpl.class, "UpdateUnitProviderImpl_FormatCheckingForUpdates", getDisplayName ()));
        }
        final UpdateProvider updateProvider = getUpdateProvider();
        updateProvider.refresh (force);
        if (force) {
            // store time of the last check
            AutoupdateSettings.setLastCheck (new Date ());
            AutoupdateSettings.setLastCheck (updateProvider.getName(),new Date ());
        }
        // don't remember update units while refreshing the content
        UpdateManagerImpl.getInstance().clearCache ();
        return res;
    }
    
    public void setEnable (boolean state) {
        // don't remember clean-up update units
        UpdateManagerImpl.getInstance().clearCache ();
        storeState (getUpdateProvider (), state);
    }
    
    public boolean isEnabled () {
        return loadState (getUpdateProvider ().getName ());
    }
    
    public UpdateProvider getUpdateProvider () {
        assert provider != null : "UpdateProvider found.";
        return provider;
    }
    
    public static void remove (UpdateUnitProvider unitProvider) {
        UpdateUnitProviderImpl impl = Trampoline.API.impl(unitProvider);
        impl.remove();
    }   
    
    private void remove() {                        
        try {
            if (getPreferences().nodeExists(getName())) {
                getPreferences().node(getName()).removeNode();
                getPreferences().node(getName()+REMOVED_MASK).putBoolean(REMOVED_MASK, true);
            } else {
                getPreferences().node(getName()+REMOVED_MASK).putBoolean(REMOVED_MASK, true);
            }
            
        } catch(BackingStoreException bsx) {
            Exceptions.printStackTrace(bsx);
        }
        // don't remember clean-up update units
        UpdateManagerImpl.getInstance().clearCache ();
    }

    public static UpdateUnitProvider createUpdateUnitProvider (String codeName, String displayName, URL url) {
        return createUpdateUnitProvider(codeName, displayName, url, ProviderCategory.forValue(CATEGORY.COMMUNITY));
    }
    
    // static factory methods
    public static UpdateUnitProvider createUpdateUnitProvider (String codeName, String displayName, URL url, ProviderCategory category) {
        codeName = normalizeCodeName (codeName);
        // store to Preferences
        storeProvider(codeName, displayName, url, category);
        
        AutoupdateCatalogProvider catalog = new AutoupdateCatalogProvider (codeName, displayName, url, category);
        
        return Trampoline.API.createUpdateUnitProvider (new UpdateUnitProviderImpl (catalog));
    }

    public static UpdateUnitProvider createUpdateUnitProvider (String name, File... files) {
        name = normalizeCodeName (name);
        LocalNBMsProvider provider = new LocalNBMsProvider (name, files);
        return Trampoline.API.createUpdateUnitProvider (new UpdateUnitProviderImpl (provider));
    }
    
    public static List<UpdateUnitProvider> getUpdateUnitProviders (boolean onlyEnabled) {
        
        // try to load Update Center from old autoupdate for backward compatibility
        FileObject auTypeFolder = FileUtil.getConfigFile("Services/AutoupdateType");
        if (auTypeFolder != null) {
            for (FileObject auType : auTypeFolder.getChildren()) {
                // check first if already exist
                try {
                    if (!getPreferences().nodeExists(auType.getName()) &&
                            !getPreferences().nodeExists(auType.getName() + REMOVED_MASK)) {
                        try {
                            UpdateProvider p = AutoupdateCatalogFactory.createUpdateProvider(auType);
                            if (p != null) {
                                getPreferences().node(auType.getName()).putBoolean("loaded", true);
                                err.log(Level.FINEST, "{0} loaded", auType);
                            } else {
                                err.log(Level.INFO, "{0} cannot be loaded (missing url or url_key)", auType);
                                getPreferences().node(auType.getName()).putBoolean("loaded", false);
                            }
                        } catch (Exception x) {
                            Exceptions.printStackTrace (x);
                        }
                    }
                } catch (BackingStoreException bse) {
                    err.log(Level.INFO, bse.getMessage() + " while loading " + auType, bse);
                }
            }
        }
        
        Collection<? extends UpdateProvider> col = UPDATE_PROVIDERS.allInstances ();
        Map<String, UpdateProvider> providerMap = new HashMap<String, UpdateProvider> ();
        for (UpdateProvider provider : col) {
            try {
                if (getPreferences ().nodeExists(provider.getName()+REMOVED_MASK)) {
                    continue;
                }
            } catch(BackingStoreException bsx) {
                Exceptions.printStackTrace(bsx);
            }
            providerMap.put (provider.getName (), provider);
        }
        
        try {
            Preferences p = getPreferences ();
            String[] children = p.childrenNames ();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    UpdateProvider provider = loadProvider (children [i]);
                    if (err.isLoggable(Level.FINE)) {
                        if (provider != null && providerMap.containsKey (provider.getName ())) {
                            err.log (Level.FINE, "Customized Provider " + provider.getName ());
                        }
                    }
                    if (provider != null) {
                        providerMap.put (provider.getName (), provider);
                    }
                    
                    // check its state
                    boolean enabled = loadState (children [i]);
                    if (onlyEnabled && !enabled) {
                        providerMap.remove (children [i]);
                    }
                }
            }
        } catch (BackingStoreException bse) {
            err.log(Level.INFO, bse.getMessage(), bse);
        }
        
        List<UpdateUnitProvider> unitProviders = new ArrayList<UpdateUnitProvider> (providerMap.values ().size ());
        for (UpdateProvider p : providerMap.values ()) {
            UpdateUnitProviderImpl impl = new UpdateUnitProviderImpl (p);
            unitProviders.add (Trampoline.API.createUpdateUnitProvider (impl));
        }
        return unitProviders;
    }
    
    public static void refreshProviders (ProgressHandle handle, boolean force) throws IOException {
        List<UpdateUnitProvider> providers = getUpdateUnitProviders (true);
        for (UpdateUnitProvider p : providers) {
            p.refresh (handle, force);
            AutoupdateSettings.setLastCheck (p.getName(),new Date ());//NOI18N
        }
        if (force) {
            // store time of the last check
            AutoupdateSettings.setLastCheck (new Date ());            
        }
        // don't remember clean-up update units
        UpdateManagerImpl.getInstance().clearCache ();
    }
    
    private static void storeProvider(
        String codeName, String displayName, URL url, ProviderCategory c
    ) {
        if (codeName.contains ("/")) {
            codeName = codeName.replaceAll ("/", "_");
        }
        Preferences providerPreferences = getPreferences ().node (codeName);
        assert providerPreferences != null : "Preferences node " + codeName + " found.";
        
        providerPreferences.put (URL, url.toString ());
        providerPreferences.put (DISPLAY_NAME, displayName);
        providerPreferences.put(CATEGORY_NAME, c.getDisplayName());
        providerPreferences.put(AutoupdateCatalogFactory.ORIGINAL_CATEGORY_ICON_BASE, c.getIconBase());
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate"); // NOI18N
    }    
    
    /*private because tests*/ static UpdateProvider loadProvider (String codeName) {
        try {
            if(getPreferences ().nodeExists(codeName + REMOVED_MASK)) {
                return null;
            }
        } catch (BackingStoreException e) {
            err.log(Level.INFO, e.getMessage(), e);
        }
        Preferences providerPreferences = getPreferences ().node (codeName);
        assert providerPreferences != null : "Preferences node " + codeName + " found.";
        
        String toUrl = providerPreferences.get (URL, providerPreferences.get (AutoupdateCatalogFactory.ORIGINAL_URL, null));
        String displayName = providerPreferences.get (DISPLAY_NAME, providerPreferences.get (AutoupdateCatalogFactory.ORIGINAL_DISPLAY_NAME, codeName));
        String categoryName = providerPreferences.get (CATEGORY_NAME, providerPreferences.get (AutoupdateCatalogFactory.ORIGINAL_CATEGORY_NAME, CATEGORY.COMMUNITY.name()));
        CATEGORY c;
        try {
            c = CATEGORY.valueOf(categoryName);
        } catch (IllegalArgumentException ex) {
            c = null;
        }
        String categoryIconBase = providerPreferences.get(AutoupdateCatalogFactory.ORIGINAL_CATEGORY_ICON_BASE, null);
        ProviderCategory pc;
        if (c != null) {
            pc = ProviderCategory.forValue(c);
        } else {
            pc = ProviderCategory.create(categoryIconBase, categoryName);
        }
        
        // filter Providers which store only its state
        if (toUrl == null) {
            return null;
        }
        
        URL url = null;
        try {
            url = new URL (toUrl);
        } catch (MalformedURLException mue) {
            assert false : mue;
        }
        return new AutoupdateCatalogProvider (codeName, displayName, url, pc);
    }
    
    private static boolean loadState (String codename) {
        Preferences providerPreferences = getPreferences ().node (codename);
        assert providerPreferences != null : "Preferences node " + codename + " found.";
        
        String enabled = providerPreferences.get (ENABLED, providerPreferences.get (AutoupdateCatalogFactory.ORIGINAL_ENABLED, null));
        
        return ! Boolean.FALSE.toString ().equals (enabled);
    }
    
    private static void storeState (UpdateProvider p, boolean isEnabled) {
        Preferences providerPreferences = getPreferences ().node (p.getName ());
        assert providerPreferences != null : "Preferences node " + p.getName () + " found.";
        
        providerPreferences.put (ENABLED, Boolean.valueOf (isEnabled).toString ());
        try {
        if(getPreferences().nodeExists(p.getName() + REMOVED_MASK)) {
            getPreferences().node(p.getName() + REMOVED_MASK).removeNode();
        }
        } catch (BackingStoreException x) {
            err.log (Level.INFO, x.getMessage(), x);
        }
    }
    
    private static String loadDisplayName (UpdateProvider p) {
        Preferences providerPreferences = getPreferences ().node (p.getName ());
        assert providerPreferences != null : "Preferences node " + p.getName () + " found.";
        
        return providerPreferences.get (DISPLAY_NAME, p.getDisplayName ());
    }
    
    private static void storeDisplayName (UpdateProvider p, String displayName) {
        Preferences providerPreferences = getPreferences ().node (p.getName ());
        assert providerPreferences != null : "Preferences node " + p.getName () + " found.";
        
        // store only if differs
        if (displayName == null) {
            providerPreferences.remove (DISPLAY_NAME);
        } else if (! displayName.equals (p.getDisplayName ())) {
            providerPreferences.put (DISPLAY_NAME, displayName);
        }
    }
    
    private static URL loadUrl (UpdateProvider p) {
        Preferences providerPreferences = getPreferences ().node (p.getName ());
        assert providerPreferences != null : "Preferences node " + p.getName () + " found.";
        
        String urlSpec = null;
        if (p instanceof AutoupdateCatalogProvider) {
            urlSpec = ((AutoupdateCatalogProvider) p).getUpdateCenterURL ().toExternalForm ();
        }
        urlSpec = providerPreferences.get (URL, urlSpec);
        if (urlSpec == null || urlSpec.length () == 0) {
            return null;
        } else {
            int idx = urlSpec.indexOf ("?unique");
            if (idx != -1) {
                urlSpec = urlSpec.substring (0, idx);
            }
        }
        err.log (Level.FINE, "Use urlSpec " + urlSpec + " for Provider " + p);
        URL url = null;
        try {
            url = new URL (urlSpec);
        } catch (MalformedURLException x) {
            err.log (Level.INFO, x.getMessage(), x);
        }
        return url;
    }
    
    private static void storeUrl (UpdateProvider p, URL url) {
        Preferences providerPreferences = getPreferences ().node (p.getName ());
        assert providerPreferences != null : "Preferences node " + p.getName () + " found.";
        
        // store only if differs
        if (url == null) {
            providerPreferences.remove (URL);
        } else {
            URL orig = null;
            if (p instanceof AutoupdateCatalogProvider) {
                orig = ((AutoupdateCatalogProvider) p).getUpdateCenterURL ();
            }
            if (! url.toExternalForm ().equals (orig.toExternalForm ())) {
                providerPreferences.put (URL, url.toExternalForm ());
                if (p instanceof AutoupdateCatalogProvider) {
                    ((AutoupdateCatalogProvider) p).setUpdateCenterURL (url);
                }
            }
        }
    }

    private static class LookupListenerImpl implements LookupListener {
        final Lookup.Result<UpdateProvider> result = Lookup.getDefault ().lookupResult(UpdateProvider.class);
        
        @SuppressWarnings("LeakingThisInConstructor")
        public LookupListenerImpl() {
            result.addLookupListener(this);
        }
        
        @Override
        public void resultChanged(LookupEvent ev) {
            err.log (Level.FINE, "Lookup.Result changed " + ev);
            try {
                refreshProviders (null, false);
            } catch (IOException ioe) {
                err.log (Level.INFO, ioe.getMessage (), ioe);
            }
        }
        
        public Collection<? extends UpdateProvider> allInstances() {
            Collection<? extends UpdateProvider> res = result.allInstances();
            return res;
        }
    }

    private static String normalizeCodeName (String codeName) {
        Collection<Character> illegalChars = Arrays.asList (new Character [] {'"', '*', '/', ':', '<', '>', '?', '\\', '|'}); // NOI18N
        StringBuilder buf = new StringBuilder ();
        for (char ch : codeName.toCharArray ()) {
            if (illegalChars.contains (ch)) {
                ch = '_'; // NOI18N
            }
            buf.append (ch);
        }
        return buf.toString ();
    }

    @Override
    public String toString() {
        return provider.toString();
    }
}
