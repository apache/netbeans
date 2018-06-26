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
