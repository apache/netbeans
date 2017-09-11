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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
