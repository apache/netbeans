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
package org.netbeans.modules.javascript2.react;


import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Named;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;


/**
 *
 * @author Petr Pisl
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/html", service = HtmlExtension.class), //NOI18N
    @MimeRegistration(mimeType = "text/javascript", service = HtmlExtension.class) //NOI18N
})
public class ReactHtmlExtension extends HtmlExtension {
    private static String CLASSNAME = "classname"; //NOI18N
    private static String REACT_MIMETYPE = JsTokenId.JAVASCRIPT_MIME_TYPE + "/text/html"; //NOI18N
    @Override
    public boolean isCustomAttribute(Attribute attribute, HtmlSource source) {
        if (CLASSNAME.equalsIgnoreCase(attribute.name().toString())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isCustomTag(Named element, HtmlSource source) {
        if (source.getSnapshot().getMimePath().getPath().endsWith(REACT_MIMETYPE)) {
            return true;
        }
        return false;
    }
    
    
    
    
}
