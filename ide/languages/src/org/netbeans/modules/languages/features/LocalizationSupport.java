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

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.Utils;
import org.openide.util.NbBundle;


/**
 *
 * @author Dan
 */
public class LocalizationSupport {
    
    private static Map<Language,ResourceBundle> languageToBundle = new HashMap<Language,ResourceBundle> ();
    
    public static String localize (Language language, String text) {
        if (text == null) return null;
        if (!languageToBundle.containsKey (language)) {
            Feature bundleFeature = language.getFeatureList ().getFeature ("BUNDLE");
            if (bundleFeature != null) {
                String baseName = (String) bundleFeature.getValue ();
                if (baseName != null) {
                    try {
                        languageToBundle.put (language, NbBundle.getBundle (baseName));
                    } catch (MissingResourceException e) {
                        Utils.notify (e);
                        languageToBundle.put (language, null);
                    }
                } else
                    languageToBundle.put (language, null);
            }
        }
        ResourceBundle bundle = languageToBundle.get (language);
        if (bundle != null)
            try {
                return bundle.getString (text);
            } catch (MissingResourceException ex) {
            }
        return text;
    }
}






