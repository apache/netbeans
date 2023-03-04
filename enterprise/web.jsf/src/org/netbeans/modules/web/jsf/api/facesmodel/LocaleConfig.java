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

import java.util.List;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "locale-config" element allows the app developer to
 * declare the supported locales for this application.
 *
 * @author Petr Pisl
 */
public interface LocaleConfig extends ApplicationElement, IdentifiableElement {

    /**
     * Property name of &lt;default-locale&gt; element.
     */
    public static final String DEFAULT_LOCALE = JSFConfigQNames.DEFAULT_LOCALE.getLocalName();
    /**
     * Property name of &lt;supported-locale&gt; element.
     */
    public static final String SUPPORTED_LOCALE = JSFConfigQNames.SUPPORTED_LOCALE.getLocalName();

    /**
     * The "default-locale" element declares the default locale
     * for this application instance.
     *
     * @return the default locale
     */
    DefaultLocale getDefaultLocale();

    /**
     * The "default-locale" element declares the default locale
     * for this application instance.
     *
     * It must be specified as :language:[_:country:[_:variant:]]
     * without the colons, for example "ja_JP_SJIS".  The
     * separators between the segments may be '-' or '_'.
     * @param locale the default locale
     */
    void setDefaultLocale(DefaultLocale locale);

    /**
     * The "supported-locale" element allows authors to declare
     * which locales are supported in this application instance.
     *
     * @return a list of supported locales
     */
    List<SupportedLocale> getSupportedLocales();

    /**
     * The "supported-locale" element allows authors to declare
     * which locales are supported in this application instance.
     *
     * It must be specified as :language:[_:country:[_:variant:]]
     * without the colons, for example "ja_JP_SJIS".  The
     * separators between the segments may be '-' or '_'.
     * @param locale the supported locale
     */
    void addSupportedLocales(SupportedLocale locale);

    /**
     * The "supported-locale" element allows authors to declare
     * which locales are supported in this application instance.
     *
     * It must be specified as :language:[_:country:[_:variant:]]
     * without the colons, for example "ja_JP_SJIS".  The
     * separators between the segments may be '-' or '_'.
     * @param index where will be putted
     * @param locale the supported locale
     */
    void addSupportedLocales(int index, SupportedLocale locale);
    
    void removeSupportedLocale(SupportedLocale locale);
}
