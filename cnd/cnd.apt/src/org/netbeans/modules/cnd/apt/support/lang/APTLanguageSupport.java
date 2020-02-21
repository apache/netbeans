/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
