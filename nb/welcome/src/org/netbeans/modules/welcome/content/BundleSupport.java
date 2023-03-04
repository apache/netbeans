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

package org.netbeans.modules.welcome.content;

import java.text.MessageFormat;
import javax.swing.JComponent;
import org.openide.util.NbBundle;

public class BundleSupport {

    public static final String BUNDLE_NAME = "org.netbeans.modules.welcome.resources.Bundle"; // NOI18N

    private static final String LABEL_PREFIX = "LBL_"; // NOI18N
    private static final String URL_PREFIX = "URL_"; // NOI18N
    private static final String CATEGORY_PREFIX = "CATEGORY_"; // NOI18N
    private static final String TEMPLATE_PREFIX = "TEMPLATE_"; // NOI18N
    private static final String ACN_PREFIX = "ACN_"; // NOI18N
    private static final String ACD_PREFIX = "ACD_"; // NOI18N
    private static final String MNM_PREFIX = "MNM_"; // NOI18N
    
    public static String getLabel(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(LABEL_PREFIX + bundleKey);
    }
    
    public static String getURL(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(URL_PREFIX + bundleKey);
    }
    
    public static char getMnemonic(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(MNM_PREFIX + bundleKey).charAt(0);
    }
    
    public static String getSampleCategory(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(CATEGORY_PREFIX + bundleKey);
    }

    public static String getSampleTemplate(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(TEMPLATE_PREFIX + bundleKey);
    }

    public static String getAccessibilityName(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(ACN_PREFIX + bundleKey);
    }
    
    public static String getAccessibilityName(String bundleKey, String param) {
        return MessageFormat.format( NbBundle.getBundle(BUNDLE_NAME).getString(ACN_PREFIX + bundleKey), param );
    }
    
    public static String getAccessibilityDescription(String bundleKey, String param) {
        return MessageFormat.format( NbBundle.getBundle(BUNDLE_NAME).getString(ACD_PREFIX + bundleKey), param );
    }
    
    public static void setAccessibilityProperties(JComponent component, String bundleKey) {
        String aName = NbBundle.getBundle(BUNDLE_NAME).getString(ACN_PREFIX + bundleKey);  
        String aDescr = NbBundle.getBundle(BUNDLE_NAME).getString(ACD_PREFIX + bundleKey);  
      
        component.getAccessibleContext().setAccessibleName(aName);
        component.getAccessibleContext().setAccessibleDescription(aDescr);
    }
    
    public static String getMessage( String key, Object param ) {
        return MessageFormat.format( NbBundle.getBundle(BUNDLE_NAME).getString(key), param );
    }
}
