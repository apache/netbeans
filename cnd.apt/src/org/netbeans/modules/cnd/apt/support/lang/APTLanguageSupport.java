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

package org.netbeans.modules.cnd.apt.support.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * support for languages:
 *  - filters collection
 */
public final class APTLanguageSupport {
    private static APTLanguageSupport singleton = new APTLanguageSupport();

    public static final String STD_C    = "Std C Language"; // NOI18N
    public static final String GNU_C    = "Gnu C Language"; // NOI18N
    public static final String GNU_CPP  = "Gnu C++ Language"; // NOI18N
    public static final String STD_CPP  = "Std C++ Language"; // NOI18N
    public static final String FORTRAN  = "Fortran Language"; // NOI18N
    public static final String UNKNOWN  = "Unknown Language"; // NOI18N

    public static final String FLAVOR_CPP17  = "C++17"; // NOI18N
    public static final String FLAVOR_CPP14  = "C++14"; // NOI18N
    public static final String FLAVOR_CPP11  = "C++11"; // NOI18N
    public static final String FLAVOR_CPP98  = "C++98"; // NOI18N
    public static final String FLAVOR_UNKNOWN  = ""; // NOI18N
    public static final String FLAVOR_FORTRAN_FIXED  = "Fortran Fixed"; // NOI18N
    public static final String FLAVOR_FORTRAN_FREE  = "Fortran Free"; // NOI18N
    
    private APTLanguageSupport() {
    }
    
    public static APTLanguageSupport getInstance() {
        return singleton;
    }
    
    public boolean isLanguageC(String language) {
        return STD_C.equals(language) || GNU_C.equals(language);
    }
    
    public boolean isLanguageCpp(String language) {
        return STD_CPP.equals(language) || GNU_CPP.equals(language);
    }
    
    public boolean isCppFlavourSufficient(String actualFlavour, String necessaryFlavour) {
        if (FLAVOR_CPP17.equals(necessaryFlavour)) {
            return FLAVOR_CPP17.equals(actualFlavour);
        } else if (FLAVOR_CPP14.equals(necessaryFlavour)) {
            return FLAVOR_CPP14.equals(actualFlavour) ||
                   FLAVOR_CPP17.equals(actualFlavour);
        } else if (FLAVOR_CPP11.equals(necessaryFlavour)) {
            return FLAVOR_CPP11.equals(actualFlavour) ||
                   FLAVOR_CPP14.equals(actualFlavour) ||
                   FLAVOR_CPP17.equals(actualFlavour);
        } else if (FLAVOR_CPP98.equals(necessaryFlavour)) {
            return true;
        }
        return false;
    }
    
    public APTLanguageFilter getFilter(String lang) {
        return getFilter(lang, null);
    }

    public APTLanguageFilter getFilter(String lang, String flavor) {
        if(flavor == null) {
            flavor = "";
        }
        // no sync is needed here
        APTLanguageFilter filter = langFilters.get(lang + flavor);
        if (filter == null) {
            filter = createFilter(lang, flavor);
            if (filter != null) {
                addFilter(lang + flavor, filter);
            }
        }
        return filter;
    }
    
    public void addFilter(String lang, final APTLanguageFilter filter) {
        langFilters.put(lang, filter);
    }
    
    private Map<String, APTLanguageFilter> langFilters = new HashMap<String, APTLanguageFilter>();

    private static APTLanguageFilter createFilter(String lang, String flavor) {
        APTLanguageFilter filter = null;
        // Now support only few filters
        if (lang.equalsIgnoreCase(APTLanguageSupport.STD_C)) {
            filter = new APTStdCFilter();
        } else if (lang.equalsIgnoreCase(APTLanguageSupport.STD_CPP)) {
            filter = new APTStdCppFilter();
        } else if (lang.equalsIgnoreCase(APTLanguageSupport.GNU_C)) {
            filter = new APTGnuCFilter();
        } else if (lang.equalsIgnoreCase(APTLanguageSupport.GNU_CPP)) {
            if (flavor.equalsIgnoreCase(FLAVOR_CPP17)) {
                filter = new APTGnuCpp14Filter(true);
            } else if (flavor.equalsIgnoreCase(FLAVOR_CPP14)) {
                filter = new APTGnuCpp14Filter(false);
            } else if (flavor.equalsIgnoreCase(FLAVOR_CPP11)) {
                filter = new APTGnuCpp11Filter();
            } else if (flavor.equalsIgnoreCase(FLAVOR_CPP98)) {
                filter = new APTGnuCppFilter();
            } else {
                filter = new APTGnuCppFilter();
            }
        } else if (lang.equalsIgnoreCase(APTLanguageSupport.FORTRAN)) {
            filter = new APTFortranFilter(flavor);
        } else {
            APTUtils.LOG.log(Level.WARNING, "unsupported language {0}", lang); // NOI18N
        }
        return filter;
    }    
}
