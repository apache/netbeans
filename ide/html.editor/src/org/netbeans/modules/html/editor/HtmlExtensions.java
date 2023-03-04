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
package org.netbeans.modules.html.editor;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.web.common.api.WebPageMetadata;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class HtmlExtensions {
    
    public static HtmlExtension TEST_EXTENSION;
    
    public static Collection<? extends HtmlExtension> getRegisteredExtensions(String mimeType) {
        if(TEST_EXTENSION != null) {
            return Collections.singleton(TEST_EXTENSION);
        }
        Lookup lookup = MimeLookup.getLookup(mimeType);
        return lookup.lookupAll(HtmlExtension.class);
    }
    
    /**
     * Determines whether the given HTML source represents a piece of web javascript-html application (Knockout, AngularJS,...).
     * 
     * @see HtmlExtension#isApplicationPiece(org.netbeans.modules.html.editor.api.gsf.HtmlParserResult) 
     * @param result
     * @return 
     */
    public static boolean isApplicationPiece(HtmlParserResult result) {
        String mimeType = WebPageMetadata.getContentMimeType(result, true);
        for(HtmlExtension ex : getRegisteredExtensions(mimeType)) {
            if(ex.isApplicationPiece(result)) {
                return true;
            }
        }
        return false;
    }

    
}
