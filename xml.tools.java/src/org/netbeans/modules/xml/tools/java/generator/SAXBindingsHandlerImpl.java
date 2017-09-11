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
package org.netbeans.modules.xml.tools.java.generator;

import org.netbeans.modules.xml.tools.java.generator.ParsletBindings;
import org.netbeans.modules.xml.tools.java.generator.ElementBindings;
import org.xml.sax.*;

public class SAXBindingsHandlerImpl implements SAXBindingsHandler {

    private ParsletBindings parslets = new ParsletBindings();
    private ElementBindings elements = new ElementBindings();

    private static final String ATT_PARSLET = "parslet"; // NOI18N
    private static final String ATT_RETURN = "return"; // NOI18N

    private static final String ATT_ELEMENT = "element"; // NOI18N
    private static final String ATT_TYPE = "type"; // NOI18N
    private static final String ATT_METHOD = "method"; // NOI18N
    
    public ParsletBindings getParsletBindings() {
        if (parslets.isEmpty()) return null;
        return parslets;
    }
    
    public ElementBindings getElementBindings() {
        if (elements.isEmpty()) return null;
        return elements;
    }
    
    public void handle_parslet(final Attributes meta) throws SAXException {
        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("handle_parslet: " + meta); // NOI18N
        
        String parslet = meta.getValue(ATT_PARSLET);
        String back = meta.getValue(ATT_RETURN);
        
        parslets.put(parslet, back);
    }
    
    public void start_SAX_bindings(final Attributes meta) throws SAXException {
        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("start_SAX_bindings: " + meta); // NOI18N
                
    }
    
    public void end_SAX_bindings() throws SAXException {
        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("end_SAX_bindings()"); // NOI18N
    }
    
    public void start_bind(final Attributes meta) throws SAXException {
        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("start_bind: " + meta); // NOI18N
        
        String element = meta.getValue(ATT_ELEMENT);
        String method = meta.getValue(ATT_METHOD);
        String parslet = meta.getValue(ATT_PARSLET);
        String type = meta.getValue(ATT_TYPE);
        
        elements.put(element, method, parslet, type);
    }
    
    public void end_bind() throws SAXException {
        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("end_bind()"); // NOI18N
    }
    
    /**
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_attbind(Attributes meta) throws SAXException {
    }
    
}
