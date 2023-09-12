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
package org.netbeans.modules.cnd.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public final class CndLanguageStandards {
    public enum CndLanguageStandard {
        C89("C89"), //NOI18N
        C99("C99"), //NOI18N
        C11("C11"), //NOI18N
        C17("C17"), //NOI18N
        C23("C23"), //NOI18N
        CPP98("C++98"), //NOI18N
        CPP11("C++11"), //NOI18N
        CPP14("C++14"), //NOI18N
        CPP17("C++17"), //NOI18N
        CPP20("C++20"), //NOI18N
        CPP23("C++23"), //NOI18N
        UNKNOWN("Unknown"); // NOI18N
        
        private final String id;
        CndLanguageStandard(String id) {
            this.id=id;
        }

        public String getID() {
            return id;
        }

        @Override
        public String toString() {
            return id;
        }
    }
    
    public static CndLanguageStandard StringToLanguageStandard(String st) {
        for(CndLanguageStandard standard : CndLanguageStandard.values()) {
            if (standard.id.equalsIgnoreCase(st)) {
                return standard;
            }
        }
        return null;
    }

    public static Collection<CndLanguageStandard> getSupported(String mime) {
        if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
            return Arrays.asList(CndLanguageStandard.CPP98, CndLanguageStandard.CPP11, CndLanguageStandard.CPP14, CndLanguageStandard.CPP17, CndLanguageStandard.CPP20, CndLanguageStandard.CPP23);
        } else if (MIMENames.C_MIME_TYPE.equals(mime)) {
            return Arrays.asList(CndLanguageStandard.C89, CndLanguageStandard.C99, CndLanguageStandard.C11, CndLanguageStandard.C17, CndLanguageStandard.C23);
        } if (MIMENames.HEADER_MIME_TYPE.equals(mime)) {
            return Arrays.asList(CndLanguageStandard.C89, CndLanguageStandard.C99, CndLanguageStandard.C11, CndLanguageStandard.C17, CndLanguageStandard.C23,
                    CndLanguageStandard.CPP98, CndLanguageStandard.CPP11, CndLanguageStandard.CPP14, CndLanguageStandard.CPP17, CndLanguageStandard.CPP20, CndLanguageStandard.CPP23);
        } 
        return Collections.<CndLanguageStandard>emptyList();
    }
}
