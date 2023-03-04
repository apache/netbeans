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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.DefaultLocale;
import org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl, ads
 */

public class LocaleConfigImpl extends IdentifiableComponentImpl 
    implements LocaleConfig 
{
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(2);
    static { 
        SORTED_ELEMENTS.add(JSFConfigQNames.DEFAULT_LOCALE.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.SUPPORTED_LOCALE.getLocalName());
    }

    public LocaleConfigImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public LocaleConfigImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.LOCALE_CONFIG));
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    public DefaultLocale getDefaultLocale() {
        return getChild(DefaultLocale.class);
    }

    public void setDefaultLocale(DefaultLocale locale) {
        /* It seems wrong code. <code>locale</code> is not set here as child.
         * it is used ONLY as information conteiner. It's bad. 
         * setChildElementText(DEFAULT_LOCALE, locale.getLocale(), 
                JSFConfigQNames.DEFAULT_LOCALE.getQName(getNamespaceURI()));*/
        setChild( DefaultLocale.class, DEFAULT_LOCALE, locale, Collections.EMPTY_LIST);
        reorderChildren();
    }

    public List<SupportedLocale> getSupportedLocales() {
        return getChildren(SupportedLocale.class);
    }

    public void addSupportedLocales(SupportedLocale locale) {
        appendChild(SUPPORTED_LOCALE, locale);
    }

    public void addSupportedLocales(int index, SupportedLocale locale) {
        insertAtIndex(SUPPORTED_LOCALE, locale, index, SupportedLocale.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig#removeSupportedLocales(org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale)
     */
    public void removeSupportedLocale( SupportedLocale locale ) {
        removeChild( SUPPORTED_LOCALE, locale );
    }

    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }

}
