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

package org.netbeans.modules.web.jsf.api.facesmodel;

import org.netbeans.modules.web.jsf.impl.facesmodel.FacesAttributes;

/**
 * The xml:lang attribute defines the language that the
 * elements are provided in. Its value is "en" (English)
 * by default
 * @author Petr Pisl
 */
public interface LangAttribute extends JSFConfigComponent {

    public static String LANG_ATTRIBUTE  = FacesAttributes.LANG.getName();
    
    /**
     * The xml:lang attribute defines the language that the
     * elements are provided in. Its value is "en" (English)
     * by default.
     * @return the lang attribute.
     */
    public String getLang();
    
    /**
     * The lang attribute defines the language that the
     * elements are provided in. Its value is "en" (English)
     * by default.
     * @param lang the value of lang attribute.
     */
    public void setLang(String lang);
}
