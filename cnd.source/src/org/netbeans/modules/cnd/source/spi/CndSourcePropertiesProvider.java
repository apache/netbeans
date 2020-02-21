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
package org.netbeans.modules.cnd.source.spi;

import javax.swing.text.StyledDocument;
import org.openide.loaders.DataObject;

/**
 *
 */
public interface CndSourcePropertiesProvider {
    // constant to be used for registration of provider
    // i.e. @ServiceProvider(path=CndSourcePropertiesProvider.REGISTRATION_PATH, service=CndSourcePropertiesProvider.class, position=100)
    public static final String REGISTRATION_PATH = "CND/CndSourcePropertiesProvider"; // NOI18N

    /**
     * Add extra document properties if needed. Method is called out of EDT
     * Language and InputAttributes are already in document's properties.
     * <code>
     * Language<?> language = (Language<?>) doc.getProperty(Language.class);
     * InputAttributes lexerAttrs = (InputAttributes)doc.getProperty(InputAttributes.class);
     * </code>
     * For instance, if someone need to set extra lexing attributes it can be done like
     * <code>
     * Filter<?> flt = CndLexerUtilities.getGccCpp11Filter();
     * lexerAttrs.setValue(language, CndLexerUtilities.LEXER_FILTER, flt, true);  // NOI18N
     * </code>
     * @param dob C/C++/Fortran data object
     * @param doc C/C++/Fortran document associated with data object
     */
    public void addProperty(DataObject dob, StyledDocument doc);
}
