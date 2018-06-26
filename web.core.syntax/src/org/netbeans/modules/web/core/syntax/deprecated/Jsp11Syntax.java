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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
