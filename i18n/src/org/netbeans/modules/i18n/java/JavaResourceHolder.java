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

package org.netbeans.modules.i18n.java;

import java.io.IOException;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.ResourceHolder;
import org.netbeans.modules.properties.BundleStructure;
import org.netbeans.modules.properties.Element;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Resource holder java sources. Now supports .properties bundle files only.
 *
 * @author  Peter Zavadsky
 */
public class JavaResourceHolder extends ResourceHolder {

    private String selectedLocale;

    /** Constructor. */
    public JavaResourceHolder() {
        super(new Class[] {PropertiesDataObject.class});
    }

    /**
     * Sets design time localization - i.e. where to look for values
     * preferentially (methods getValueForKey and getCommentForKey). If not
     * found in the given file entry directly, then parent entries are tried
     * (just like ResourceBundle would do at runtime).
     * @param locale including initial underscore (e.g. _cs_CZ)
     */
    public void setLocalization(String locale) {
        selectedLocale = locale;
    }

    /**
     * @return design time locale used when asked for value or comment.
     */
    public String getLocalization() {
        return selectedLocale;
    }

    private String getLocalizationFileName() {
        return selectedLocale != null && !selectedLocale.equals("") ? // NOI18N
               resource.getName() + selectedLocale : resource.getName();
    }

    /** Implements superclass abstract method.
    /* Gets all keys which are stored in underlying resource object. */
    public String[] getAllKeys() {
        try {
            return ((PropertiesDataObject) resource).getBundleStructure().getKeys();
        } catch (IllegalStateException ise) {
            // #167356 ISE: "Resource Bundles: KeyList not initialized"
            // if a Bundle.properties is accidentally removed from project
        } catch (NullPointerException npe) {
            // NPE if resource == null
        }
        return new String[0];
    }

    /**
     * Finds a free key in the bundle for given suggested key name.
     */
    public String findFreeKey(String keySpec) {
        BundleStructure bundleStructure = ((PropertiesDataObject)resource).getBundleStructure();
        return bundleStructure != null ? bundleStructure.findFreeKey(keySpec) : null;
    }

    /** Implements superclass abstract method. Gets value for specified key. 
     * @return value for key or null if such key os not stored in resource */
    public String getValueForKey(String key) {
        if(resource == null)
            return null;

        Element.ItemElem item = getItem(key);
        return item == null ? null : item.getValue();
    }

    /** Implemenst superclass abstract method. Gets comment for specified key. 
     * @return value for key or null if such key os not stored in resource */
    public String getCommentForKey(String key) {
        if(resource == null)
            return null;

        Element.ItemElem item = getItem(key);
        return item == null ? null : item.getComment();
    }

    /** Helper method. */
    private Element.ItemElem getItem(String key) {
        BundleStructure bundleStructure = ((PropertiesDataObject)resource).getBundleStructure();
        if (bundleStructure == null)
            return null;

        return bundleStructure.getItem(getLocalizationFileName(), key);
    }

    /**
     * Gets all data (values, comments) for given key across all locales.
     */
    public Object getAllData(String key) {
        if (resource instanceof PropertiesDataObject) {
            BundleStructure bundleStructure = ((PropertiesDataObject)resource).getBundleStructure();
            if (bundleStructure != null) {
                return bundleStructure.getAllData(key);
            }
        }
        return null;
    }

    /**
     * Restores data for given key (obtained sooner from getAllData method).
     */
    public void setAllData(String key, Object data) {
        if (resource instanceof PropertiesDataObject) {
            BundleStructure bundleStructure = ((PropertiesDataObject)resource).getBundleStructure();
            if (bundleStructure != null) {
                bundleStructure.setAllData(key, (String[])data);
            }
        }
    }

    /** Implements superclass abstract method. Adds new property (key-valkue pair) to resource object. 
     * @param key key value, if it is <code>null</code> nothing is done
     * @param value 'value' value, can be <code>null</code>
     * @param comment comment, can be <code>null</code>
     * @param forceNewValue if there already exists a key forces to reset its value
     */
    public void addProperty(Object key, Object value, String comment, boolean forceNewValue) {
        if(resource == null || key == null) return;

        String keyValue     = key.toString();
        String valueValue   = value == null ? "" : value.toString(); // NOI18N
        String commentValue = comment;
        
        // write to bundle file(s)
        BundleStructure bundleStructure = ((PropertiesDataObject)resource).getBundleStructure();
        if (bundleStructure != null) {
            bundleStructure.addItem(getLocalizationFileName(),
                                    keyValue, valueValue, commentValue,
                                    forceNewValue);
        }
    }

    /**
     * Removes property of given key from all locale files of the bundle.
     */
    public void removeProperty(Object key) {
        if (resource != null) {
            BundleStructure bundleStructure = ((PropertiesDataObject)resource).getBundleStructure();
            if (bundleStructure != null) {
                bundleStructure.removeItem(key.toString());
            }
        }
    }

    /** Implements superclass abstract method. Creates template of type clazz 
     * which have to be of <code>PropertiesDataObject</code> type in our case. */
    protected DataObject createTemplate(Class clazz) throws IOException {
        return getTemplate();
    }

    /**
     * Returns template data object for properties file.
     */
    public static DataObject getTemplate() throws IOException {
        FileObject fileObject = FileUtil.getConfigFile("Templates/Other/properties.properties"); // NOI18N

        if(fileObject == null)
            throw new IOException(Util.getString("EXC_TemplateNotFound"));

        try {
            return DataObject.find(fileObject);
        } catch(DataObjectNotFoundException e) {
            throw new IOException(Util.getString("EXC_TemplateNotFound"));
        }
    }
}
