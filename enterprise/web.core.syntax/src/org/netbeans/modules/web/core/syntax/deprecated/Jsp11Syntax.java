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

package org.netbeans.modules.web.core.syntax.deprecated;

import org.netbeans.editor.Syntax;
import org.netbeans.modules.web.core.api.JspColoringData;

/** Handles syntax coloring for JSP 1.1. This involves handling custom tags.
 * This class relies on an external source of data, which provides information
 * about tag libraries. The information necessary is:
 * <ul>
 *   <li>Prefixes of tag libraries imported by the page (and its included pages !)</li>
 *   <li>For individual tags inside the tag libraries, it's <code>bodyContent</code> property</li>
 * </ul>
 * This class is able to deal with cases when this information is incomplete, 
 * i.e. if the information for individual tags is missing (for example in the case when the
 * .tld descriptor of the library was not found). In such a case the tags for which the information
 * is missing are treated as if they had bodycontent set to JSP.
 *
 * // PENDING - handle TAG_DEPENDENT tags correctly, change JspMultiSyntax and JspTagSyntax accordingly
 *
 * @author  petr.jiricka@netbeans.com
 * @deprecated Use {@link JspLexer} instead.
 *
 */
@Deprecated
public class Jsp11Syntax extends JspMultiSyntax {

    /** Creates new Jsp11Syntax */
    public Jsp11Syntax() {
        super();
    }

    public Jsp11Syntax(Syntax contentSyntax, Syntax scriptingSyntax) {
        super(contentSyntax, scriptingSyntax);
    }

    /** Only keep reference to listener which listens on the JSP DataObject so 
     * it's not garbage collected. */
    public Object listenerReference;

    /** Data providing the information about tag libraries. */
    public JspColoringData data;

    protected boolean isJspTag(String tagName) {
        // not calling super() for performance reasons
        if (tagName.startsWith("jsp:")) {   // NOI18N
            // standard JSP tag
            return true;
        }
        if (data == null)
            return false;
        
        int colonIndex = tagName.indexOf(':');
        if (colonIndex == -1) {
            // not a JSP tag
            return false;
        }

        // return true if there is information for a library with our prefix
        return data.isTagLibRegistered(tagName.substring(0, colonIndex));
    }

    
    /** Determines whether any EL expressions should be colored as expressions, 
     * or ignored. Returna the correct value per section  JSP.3.3.2
     * of the specification.
     * @param whether this expression is inside the JSP tag value, or just in template text
     * @return true if the expression should be ignored, false if it should be treated as an expression
     */
    protected boolean isELIgnored(boolean inJspTag) {
        if (data == null) {
            return false;
        }
        // PENDING: what we could do is the following:
        // for a 2.3 application, see if the page uses a tag library that hacks
        // EL support (JSTL or JSF) and if it does, enable EL expressions inside
        // JSP tag attribute values for this page.
        if (inJspTag) {
            return false;
        }
        return data.isELIgnored();
    }
  
    
    protected boolean isXMLSyntax(){
        if (data == null) {
            return false;
        }
        return data.isXMLSyntax();
    }
}
