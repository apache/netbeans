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

package org.netbeans.modules.editor.options;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/** BeanInfo for AnnotationTypeOptions
 *
 * @author David Konecny
 * @since 07/2001
 */
public class AnnotationTypeOptionsBeanInfo extends SimpleBeanInfo {

    /* PropertyDescriptotrs
    * @return Returns an array of PropertyDescriptors
    * describing the editable properties supported by this bean.
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        PropertyDescriptor[] descriptors;
        
        try {
            descriptors = new PropertyDescriptor[] {
                              new PropertyDescriptor("highlightColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("useHighlightColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("foregroundColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("inheritForegroundColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("waveUnderlineColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("useWaveUnderlineColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("wholeLine", AnnotationTypeOptions.class, "isWholeLine", null) // NOI18N
                          };
            ResourceBundle bundle;
            bundle = NbBundle.getBundle(AnnotationTypeOptionsBeanInfo.class);

            descriptors[0].setDisplayName(bundle.getString("PROP_AT_HIGHLIGHT")); // NOI18N
            descriptors[0].setShortDescription(bundle.getString("HINT_AT_HIGHLIGHT")); // NOI18N
            descriptors[1].setDisplayName(bundle.getString("PROP_AT_USE_HIGHLIGHT")); // NOI18N
            descriptors[1].setShortDescription(bundle.getString("HINT_AT_USE_HIGHLIGHT")); // NOI18N
            descriptors[2].setDisplayName(bundle.getString("PROP_AT_FOREGROUND")); // NOI18N
            descriptors[2].setShortDescription(bundle.getString("HINT_AT_FOREGROUND")); // NOI18N
            descriptors[3].setDisplayName(bundle.getString("PROP_AT_INHERIT_FOREGROUND")); // NOI18N
            descriptors[3].setShortDescription(bundle.getString("HINT_AT_INHERIT_FOREGROUND")); // NOI18N
            descriptors[4].setDisplayName(bundle.getString("PROP_AT_WAVEUNDERLINE")); // NOI18N
            descriptors[4].setShortDescription(bundle.getString("HINT_AT_WAVEUNDERLINE")); // NOI18N
            descriptors[5].setDisplayName(bundle.getString("PROP_AT_USE_WAVEUNDERLINE")); // NOI18N
            descriptors[5].setShortDescription(bundle.getString("HINT_AT_USE_WAVEUNDERLINE")); // NOI18N
            descriptors[6].setDisplayName(bundle.getString("PROP_AT_WHOLELINE")); // NOI18N
            descriptors[6].setShortDescription(bundle.getString("HINT_AT_WHOLELINE")); // NOI18N

        } catch (Exception e) {
            descriptors = new PropertyDescriptor[0];
        }
        return descriptors;
    }

}

