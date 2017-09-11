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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.modules.i18n.form;

import java.util.Map;
import org.netbeans.modules.form.I18nValue;
import org.netbeans.modules.form.FormDesignValue;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.ResourceHolder;
import org.netbeans.modules.i18n.java.JavaI18nString;
import org.netbeans.modules.i18n.java.JavaI18nSupport;
import org.netbeans.modules.i18n.java.JavaResourceHolder;
import org.openide.loaders.DataObject;

/**
 * This class extends the capability of <code>JavaI18nString</code> to be
 * <code>FormDesignValue</code> to be used in form property sheets.
 *
 * @author  Peter Zavadsky
 * @see org.netbeans.modules.i18n.java.JavaI18nString
 * @see ResourceBundleStringFormEditor
 * @see org.netbeans.modules.form.FormDesignValue
 */
public class FormI18nString extends JavaI18nString implements I18nValue {

    String bundleName; // to be saved again if file can't be found after form is loaded

    Object allData; // complete data for given key across all locales
                    // stored here for undo/redo purposes

    /** Creates new <code>FormI18nString</code>. */
    public FormI18nString(I18nSupport i18nSupport) {
        super(i18nSupport);
    }

    /** Cretaes new <code>FormI18nString</code> from <code>JavaI18nString</code>. 
     * @param source source which is created new <code>FormI18nString</code> from. */
    public FormI18nString(JavaI18nString source) {
        this(createNewSupport(source.getSupport()), 
             source.getKey(),
             source.getValue(),
             source.getComment(), 
             source.getArguments(), 
             source.getReplaceFormat(),
             (source instanceof FormI18nString) ? ((FormI18nString)source).bundleName : null);
        ((JavaResourceHolder)support.getResourceHolder()).setLocalization(
                ((JavaResourceHolder)source.getSupport().getResourceHolder()).getLocalization());
    }

    FormI18nString(DataObject srcDataObject) {
        super(new FormI18nSupport.Factory().createI18nSupport(srcDataObject));

        boolean nbBundle = I18nServiceImpl.isNbBundleAvailable(srcDataObject.getPrimaryFile());
        if (I18nUtil.getDefaultReplaceFormat(!nbBundle).equals(getReplaceFormat())) {
            setReplaceFormat(I18nUtil.getDefaultReplaceFormat(nbBundle));
        }
    }

    private FormI18nString(I18nSupport i18nSupport, String key, String value, String comment, String[] arguments, String replaceFormat, String bundleName) {
        super(i18nSupport);

        this.key = key;
        this.value = value;
        this.comment = comment;
        
        this.arguments = arguments;
        this.replaceFormat = replaceFormat;
        this.bundleName = bundleName;
    }

    public Object copy(FormProperty formProperty) {
        FormModel form = formProperty.getPropertyContext().getFormModel();
        if (form == null)
            return getValue();

        DataObject sourceDO = FormEditor.getFormDataObject(form);
        if (sourceDO == null)
            return getValue();

        FormI18nString newI18nString;
        if (form.getSettings().isI18nAutoMode()) { // target form is in auto-i18n mode
            // need new key (auto-generated; form module must provide)
            newI18nString = new FormI18nString(createNewSupport(sourceDO, null, ((FormI18nSupport)support).getIdentifier()),
                                COMPUTE_AUTO_KEY, getValue(), getComment(),
                                getArguments(), getReplaceFormat(), bundleName);
            JavaResourceHolder jrh = (JavaResourceHolder) support.getResourceHolder();
            newI18nString.allData = jrh.getAllData(getKey());
        }
        else { // same key, same properties file
            I18nSupport newSupport = createNewSupport(sourceDO, support.getResourceHolder().getResource(),
                    ((FormI18nSupport)support).getIdentifier());
            newI18nString = new FormI18nString(newSupport,
                                getKey(), getValue(), getComment(),
                                getArguments(), getReplaceFormat(), bundleName);
            if (sourceDO != support.getSourceDataObject()) { // different form target
                // make sure the value is actual according to the target locale
                ResourceHolder rh = newSupport.getResourceHolder();
                newI18nString.value = rh.getValueForKey(getKey());
                newI18nString.comment = rh.getCommentForKey(getKey());
            }
        }
        return newI18nString;
    }

    public String toString() {
        return getValue();
    }

    private static I18nSupport createNewSupport(I18nSupport support) {
        return createNewSupport(support.getSourceDataObject(), support.getResourceHolder().getResource(),
                support instanceof JavaI18nSupport ? ((JavaI18nSupport)support).getIdentifier() : null);        
    }     

    private static I18nSupport createNewSupport(DataObject sourceDataObject, DataObject resource, String identifier) {
        FormI18nSupport newSupport = (FormI18nSupport) new FormI18nSupport.Factory().createI18nSupport(sourceDataObject);                
        newSupport.setIdentifier(identifier);
        if(resource != null) {
            newSupport.getResourceHolder().setResource(resource);            
        }                
        return newSupport;        
    }

    /**
     * Implements <code>FormDesignValue</code> interface. Gets design value. 
     * @see org.netbeans.modules.form.FormDesignValue#getDesignValue(RADComponent radComponent) */
    public Object getDesignValue() {
        String designValue = getValue(); //getSupport().getResourceHolder().getValueForKey(getKey());

        if(designValue == null)
            return FormDesignValue.IGNORED_VALUE;
        else
            return designValue;
    }

    public Object getDesignValue(Object target) {
        return null;
    }
    
    /** Gets description of the design value. Implements <code>FormDesignValue</code> interface.
     * @return key value */
    public String getDescription() {
        return "<" + getKey() + ">"; // NOI18N
    }

    @Override
    protected void fillFormatMap(Map<String, String> map) {
        super.fillFormatMap(map);
        if ((getSupport().getResourceHolder().getResource() == null) && (bundleName != null)) { // Issue 150287
            String base = bundleName;
            if (base.endsWith(".properties")) { // NOI18N
                base = base.substring(0, base.length() - 11);
            }
            map.put("bundleNameSlashes", base); // NOI18N
            map.put("bundleNameDots", base.replace('/', '.')); // NOI18N
        }
    }

}
