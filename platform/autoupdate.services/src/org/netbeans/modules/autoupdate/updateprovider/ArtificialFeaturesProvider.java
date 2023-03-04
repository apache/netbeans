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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.modules.autoupdate.services.FeatureUpdateElementImpl;
import org.netbeans.modules.autoupdate.services.ModuleUpdateElementImpl;
import org.netbeans.modules.autoupdate.services.UpdateElementImpl;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class ArtificialFeaturesProvider implements UpdateProvider {
    private static final String UNSORTED_CATEGORY = NbBundle.getMessage (ArtificialFeaturesProvider.class, "ArtificialFeaturesProvider_Unsorted_Category");
    private static final String LIBRARIES_CATEGORY = NbBundle.getMessage (ArtificialFeaturesProvider.class, "ArtificialFeaturesProvider_Libraries_Category");
    private static final String BRIDGES_CATEGORY = NbBundle.getMessage (ArtificialFeaturesProvider.class, "ArtificialFeaturesProvider_Bridges_Category");
    private static final String FEATURES_CATEGORY = NbBundle.getMessage (ArtificialFeaturesProvider.class, "ArtificialFeaturesProvider_Features_Category");
        
    private final Collection<UpdateItem> originalItems;
    private static final Logger log = Logger.getLogger (ArtificialFeaturesProvider.class.getName ());
    
    private static ArtificialFeaturesProvider DUMMY;
    
    public static ArtificialFeaturesProvider getDummy () {
        if (DUMMY == null) {
            DUMMY = new ArtificialFeaturesProvider (null);
        }
        return DUMMY;
    }
    
    public ArtificialFeaturesProvider (final Collection<UpdateItem> items) {
        originalItems = items;
    }

    @Override
    public String getName () {
        return "artificial-module-provider"; // NOI18N
    }

    @Override
    public String getDisplayName () {
        return getName ();
    }
    
    @Override
    public String getDescription () {
        return null;
    }

    private static boolean generateArtificialFeatures () {
        String tmp = System.getProperty ("autoupdate.services.generate.features");
        return tmp != null && Boolean.valueOf (tmp);
    }

    @Override
    public Map<String, UpdateItem> getUpdateItems () throws IOException {
        if (! generateArtificialFeatures ()) {
            return Collections.emptyMap ();
        }
        Map<String, UpdateItem> res = new HashMap<String, UpdateItem> ();
        
        // crate features built on installed modules
        Map<String, Set<ModuleInfo>> categoryToModules = new HashMap<String, Set<ModuleInfo>> ();
        for (UpdateItem item : originalItems) {
            UpdateItemImpl impl = Utilities.getUpdateItemImpl (item);
            if (impl instanceof InstalledModuleItem) {
                InstalledModuleItem installedModule = (InstalledModuleItem) impl;
                String category = (String) installedModule.getModuleInfo ().getLocalizedAttribute ("OpenIDE-Module-Display-Category");
                Module module = Utilities.toModule (installedModule.getModuleInfo ().getCodeNameBase (), installedModule.getModuleInfo ().getSpecificationVersion ());
                assert module != null : "Module found for " + installedModule.getModuleInfo ().getCodeNameBase () + ", " + installedModule.getModuleInfo ().getSpecificationVersion ();
                if (module.isAutoload () || module.isFixed ()) {
                    continue;
                } else if (module.isEager ()) {
                    continue;
                } else if (category == null || category.length () == 0) {
                    category = UNSORTED_CATEGORY;
                }
                if (! categoryToModules.containsKey (category)) {
                    categoryToModules.put (category, new HashSet<ModuleInfo> ());
                }
                categoryToModules.get (category).add (installedModule.getModuleInfo ());
            } else if (impl instanceof ModuleItem) {
                ModuleItem updateModule = (ModuleItem) impl;
                String category = (String) updateModule.getModuleInfo ().getLocalizedAttribute ("OpenIDE-Module-Display-Category");
                if (LIBRARIES_CATEGORY.equals (category) || BRIDGES_CATEGORY.equals (category) || FEATURES_CATEGORY.equals (category)) {
                    continue;
                }
                if (category == null || category.length () == 0) {
                    String dn = (String) updateModule.getModuleInfo ().getLocalizedAttribute ("OpenIDE-Module-Display-Category");
                    if (dn == null || dn.length () == 0) {
                        category = UNSORTED_CATEGORY;
                    } else {
                        category = dn;
                    }
                }
                if (! categoryToModules.containsKey (category)) {
                    categoryToModules.put (category, new HashSet<ModuleInfo> ());
                }
                categoryToModules.get (category).add (updateModule.getModuleInfo ());
            } else {
                // XXX: ignore other types now
            }
        }
        
        // make a feature for each one category
        for (String category : categoryToModules.keySet ()) {
            if (true) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            FeatureItem featureItemImpl = createFeatureItem (category, null /*categoryToModules.get (category)*/, null, null, null);
            log.log (Level.FINE, "Create FeatureItem[" + category + ", " + featureItemImpl.getSpecificationVersion ().toString () +
                    "] containing modules " + featureItemImpl.getDependenciesToModules ());
            UpdateItem featureItem = Utilities.createUpdateItem (featureItemImpl);
            res.put (featureItemImpl.getCodeName () + '_' + featureItemImpl.getSpecificationVersion (), featureItem);
        }
        
        return res;
    }

    @Override
    public boolean refresh (boolean force) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static FeatureItem createFeatureItem (String codeName,
            Set<ModuleUpdateElementImpl> modules,
            Set<FeatureUpdateElementImpl> features,
            UpdateElementImpl original,
            String additionalDescription) {
        Set<String> containsModulesOrFeatures = new HashSet<String>();
        String versionN = "";
        for (ModuleUpdateElementImpl impl : modules) {
            ModuleInfo info = impl.getModuleInfo ();
            containsModulesOrFeatures.add(info.getCodeName() + " > " + info.getSpecificationVersion());
            SpecificationVersion spec = info.getSpecificationVersion ();
            versionN = addVersion (versionN, spec);
        }
        for (FeatureUpdateElementImpl impl : features) {
            containsModulesOrFeatures.add(impl.getCodeName() + " > " + impl.getSpecificationVersion());
            SpecificationVersion spec = impl.getSpecificationVersion();
            versionN = addVersion(versionN, spec);
        }
        
        String description = original == null || original.getDescription () == null || original.getDescription ().length () == 0 ? "" :
            original.getDescription ();
        description = additionalDescription == null || additionalDescription.length () == 0 ? description :
            description + additionalDescription;
        
        String displayName = original == null || original.getDisplayName () == null || original.getDisplayName ().length () == 0 ? codeName :
            original.getDisplayName ();

        String version = original == null || original.getSpecificationVersion() == null ? versionN :
            original.getSpecificationVersion ().toString ();

        return new FeatureItem(codeName, version, containsModulesOrFeatures, displayName, description, null);
    }
    
    // XXX: should be move somewhere into utils
    public static String createVersion (Collection<ModuleInfo> modules) {
        String version = "";
        for (ModuleInfo info : modules) {
            SpecificationVersion spec = info.getSpecificationVersion ();
            version = addVersion (version, spec);
        }
        return version;
    }


    private static String addVersion (String version, SpecificationVersion spec) {
        int [] addend1 = getDigitsInVersion (version);
        int [] addend2 = getDigitsInVersion (spec.toString ());
        
        int length = Math.max (addend1.length, addend2.length);
        int [] result = new int [length];
        
        for (int i = 0; i < result.length; i++) {
            assert i < addend1.length || i < addend2.length;
            int digit = 0;
            if (i < addend1.length) {
                digit += addend1 [i];
            }
            if (i < addend2.length) {
                digit += addend2 [i];
            }
            result [i] = digit;
        }
        
        StringBuilder buf = new StringBuilder ((result.length * 3) + 1);

        for (int i = 0; i < result.length; i++) {
            if (i > 0) {
                buf.append ('.'); // NOI18N
            }

            buf.append (result [i]);
        }

        return buf.toString();
    }
    
    private static int [] getDigitsInVersion (String version) {
        if (version.length () == 0) {
            return new int [0];
        }
        StringTokenizer tok = new StringTokenizer (version, ".", true); // NOI18N
        
        int len = tok.countTokens ();
        assert (len % 2) != 0 : "Even number of pieces in a spec version: `" + version + "`";
        
        int[] digits = new int[len / 2 + 1];
        int i = 0;

        boolean expectingNumber = true;

        while (tok.hasMoreTokens ()) {
            String toParse = tok.nextToken ();
            if (expectingNumber) {
                expectingNumber = false;

                try {
                    int piece = Integer.parseInt (toParse);
                    assert piece >= 0 : "Spec version component < 0: " + piece;
                    digits[i++] = piece;
                } catch (NumberFormatException nfe) {
                    log.log (Level.INFO, "NumberFormatException while parsing " + version, nfe);
                }
                
            } else {
                assert ".".equals (toParse) : "Expected dot in spec version: `" + version + "'";

                expectingNumber = true;
            }
        }
        
        return digits;
    }

    @Override
    public CATEGORY getCategory() {
        return CATEGORY.COMMUNITY;
    }

}
