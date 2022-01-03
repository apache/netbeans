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
package org.netbeans.modules.cnd.api.model.support;

import org.netbeans.modules.cnd.api.model.CsmFile;

/**
 *
 */
public interface CsmFileLanguageProvider {
    // constant to be used for registration of provider
    // i.e. @ServiceProvider(path=CsmFileLanguageProvider.REGISTRATION_PATH, service=CsmFileLanguageProvider.class, position=100)
    public static final String REGISTRATION_PATH = "CND/CsmFileLanguageProvider"; // NOI18N
    
    /**
     * returns language which can be used to obtain language specific filter using APTLanguageSupport.getFilter
     * predefined languages can be found as constants in APTLanguageSupport
     * @param file
     * @return name of language or null if file is not recognized by this provider
     */
    String getLanguage(CsmFile.FileType type, String filename);
}
