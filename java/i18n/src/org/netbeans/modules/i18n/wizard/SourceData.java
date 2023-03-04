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


package org.netbeans.modules.i18n.wizard;


import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nString;

import org.netbeans.modules.i18n.I18nSupport;

import org.openide.loaders.DataObject;


/**
 * Object representing source dependent i18n data passed to i18n wizard
 * descriptor and its panels via readSettings and storeSettings methods.
 * It's the "value part" of <code>Map</code> (keyed by DataObject)
 * passed as settings for wizard descriptor and to individual panels.
 *
 * @author  Peter Zavadsky
 * @see ResourceWizardPanel where lifecycle begins.
 * @see org.openide.WizardDescriptor
 * @see org.openide.WizardDescriptor.Panel#readSettings
 * @see org.openide.WizardDecritptor.Panel#storeSettings
 */
final class SourceData {

    /** Resource where to put i18n string */
    private DataObject resource;

    /** Support used by i18n-zing. */
    private I18nSupport support;

    /** Mapping found hard coded strings to i18n strings. */
    private Map<HardCodedString,I18nString> stringMap
            = new TreeMap<HardCodedString,I18nString>(new HardStringComparator());
    
    private static class HardStringComparator implements Comparator<HardCodedString> {
        public int compare(HardCodedString hcs1, HardCodedString hcs2) {
            return hcs1.getStartPosition().getOffset() - 
                    hcs2.getStartPosition().getOffset();
        }
     }
    
    /** Hard coded strings user selected to non-proceed. */
    private Set<HardCodedString> removedStrings;

    
    /** Constructor. */
    public SourceData(DataObject resource) {
        this.resource = resource;
    }

    /** Constructor. */
    public SourceData(DataObject resource, I18nSupport support) {
        this.resource = resource;
        this.support = support;
        
        support.getResourceHolder().setResource(resource);
    }


    /** Getter for <code>resource</code> property. */
    public DataObject getResource() {
        return resource;
    }

    /** Getter for <code>resource</code> property. */
    public I18nSupport getSupport() {
        return support;
    }

    /** Getter for <code>stringMap</code> property. */
    public Map<HardCodedString,I18nString> getStringMap() {
        return stringMap;
    }
    
    /** Setter for <code>stringMap</code> prtoperty. */
    public void setStringMap(Map<HardCodedString,I18nString> stringMap) {
        this.stringMap.clear();
        this.stringMap.putAll(stringMap);
    }
    
    /** Getter for <code>removedStrings</code> property. */
    public Set<HardCodedString> getRemovedStrings() {
        return removedStrings;
    }
    
    /** Setter for <code>removedStrings</code> property. */
    public void setRemovedStrings(Set<HardCodedString> removedStrings) {
        this.removedStrings = removedStrings;
    }
    
}
