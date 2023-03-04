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
import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.*;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class IconImpl extends JSFConfigComponentImpl implements Icon{
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(2);
    static {
        SORTED_ELEMENTS.add(JSFConfigQNames.SMALL_ICON.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.LARGE_ICON.getLocalName());
    }
    
    public IconImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public IconImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.ICON));
    }

    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    public String getSmallIcon() {
        return getChildElementText(JSFConfigQNames.SMALL_ICON.getQName(getNamespaceURI()));
    }

    public void setSmallIcon(String smallIcon) {
        setChildElementText(SMALL_ICON, smallIcon, JSFConfigQNames.SMALL_ICON.getQName(getNamespaceURI()));
    }

    public String getLargeIcon() {
        return getChildElementText(JSFConfigQNames.LARGE_ICON.getQName(getNamespaceURI()));
    }

    public void setLargeIcon(String largeIcon) {
        setChildElementText(LARGE_ICON, largeIcon, JSFConfigQNames.LARGE_ICON.getQName(getNamespaceURI()));
    }

    public String getLang() {
        return getAttribute(FacesAttributes.LANG);
    }

    public void setLang(String lang) {
        setAttribute(LangAttribute.LANG_ATTRIBUTE, FacesAttributes.LANG, lang);
    }
}
