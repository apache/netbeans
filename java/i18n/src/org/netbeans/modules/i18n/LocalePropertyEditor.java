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


package org.netbeans.modules.i18n;


import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.Locale;

import org.netbeans.modules.properties.LocalePanel;


/**
 * Property editor for editing <code>Locale</code> instance.
 *
 * @author  Peter Zavadsky
 */
public class LocalePropertyEditor extends PropertyEditorSupport {

    /** Value to edit. */
    private Locale locale;


    /** Creates new LocalePropertyEditor */
    public LocalePropertyEditor() {
    }

    
    /** Sets value. Overrides superclass method. Overrides superclass method. */
    public void setValue(Object value) {
        if(!(value instanceof Locale))
            throw new IllegalArgumentException("I18N module: Bad class type of value:"+value.getClass().getName()); // NOI18N

        if(locale != null && locale.equals(value))
            return;
        
        locale = (Locale)value;
        firePropertyChange();
    }
    
    /** Gets value. Overrides superclass method. Overrides superclass method. */
    public Object getValue() {
        return locale;
    }
    
    /** Gets string representation of value. Overrides superclass method. */
    public String getAsText() {
        if(locale == null)
            return super.getAsText(); // NOI18N
        
        return locale.toString();
    }
    
    /** Sets as text. Overrides superclass method. */
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(createLocaleFromText(text));
    }

    /** Gets java initialization string. Overrides superclass method. */
    public String getJavaInitializationString() {
        if(locale == null)
            return super.getJavaInitializationString();
        
        StringBuffer localeInit = new StringBuffer("new Locale("); // NOI18N
        
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();

        if(language == null)
            localeInit.append("\"\""); // NOI18N
        else
            localeInit.append("\""+language+"\""); // NOI18N
        
        if(country == null)
            localeInit.append(",\"\""); // NOI18N
        else
            localeInit.append(",\""+country+"\""); // NOI18N
        
        if(variant == null)
            localeInit.append(")"); // NOI18N
        else
            localeInit.append(",\""+variant+"\")"); // NOI18N
            
        return localeInit.toString();
    }
    
    /** Overrides superclass method.
     * @return true */
    public boolean supportsCustomEditor() {
        return true;
    }
    
    /** Gets custom editor. Overrides superclass method. */
    public Component getCustomEditor() {
        final LocalePanel localePanel = new LocalePanel(locale);
        
        localePanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(LocalePanel.PROP_CUSTOMIZED_LOCALE.equals(evt.getPropertyName()))
                    setValue(localePanel.getLocale());
            }
        });
        
        return localePanel;
    }

    /** Creates <code>Locale</code> from text. Utility method. */
    private static Locale createLocaleFromText(String text) {
        if(text == null || "".equals(text)) // NOI18N
            return new Locale("", ""); // NOI18N

        // Get language code.
        int underscore = text.indexOf('_');

        String language;
        
        if(underscore == -1)
            return new Locale(text, ""); // NOI18N
        else if(underscore == 0) 
            language = ""; // NOI18N
        else
            language = text.substring(0, underscore);
        
        if(text.length() <= underscore + 1)
            return new Locale(language, ""); // NOI18N
        
        text = text.substring(underscore + 1);

        // Get country code.
        underscore = text.indexOf('_');

        String country;        
        
        if(underscore == -1)
            return new Locale(language, text);
        else if(underscore == 0)
            country = ""; // NOI18N
        else
            country = text.substring(0, underscore);

        // Get variant.
        if(text.length() <= underscore + 1)
            return new Locale(language, country); // NOI18N
        
        String variant = text.substring(underscore + 1);
        
        return new Locale(language, country, variant);
    }
}
