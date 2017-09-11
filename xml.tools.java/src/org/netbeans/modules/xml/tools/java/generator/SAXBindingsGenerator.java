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

import org.netbeans.modules.xml.tools.generator.*;
import org.netbeans.modules.xml.tools.java.generator.ElementBindings;
import java.util.*;

/**
 * Utility class generating XML document content holding model data.
 *
 * @author  Petr Kuzel
 * @version
 */
final class SAXBindingsGenerator {


    public static String toXML(SAXGeneratorModel model) {
        StringBuffer s = new StringBuffer();

        s.append("<?xml version='1.0' encoding='UTF-8'?>\n"); // NOI18N
        s.append("<!DOCTYPE SAX-bindings PUBLIC \"-//XML Module//DTD SAX Bindings 1.0//EN\" \"\">\n"); // NOI18N
        s.append("<SAX-bindings version='1'>\n"); // NOI18N
        s.append(elementBindings(model));
        s.append(parsletBindings(model));
        s.append("</SAX-bindings>"); // NOI18N
        
        return s.toString();
    }
    
    private static String elementBindings(SAXGeneratorModel model) {
        StringBuffer s = new StringBuffer();
        
        Iterator it = model.getElementBindings().values().iterator();
        while (it.hasNext()) {
            ElementBindings.Entry next = (ElementBindings.Entry) it.next();
            s.append("\t<bind element='" + next.getElement() + "' method='" + next.getMethod() + "' "); // NOI18N
            s.append("type='" + next.getType() + "' "); // NOI18N
            if (next.getParslet() != null) {
                s.append("parslet='" + next.getParslet() + "' "); // NOI18N
            }
            s.append("></bind>\n"); // NOI18N
        }
        return s.toString();
    }
    
      private static String parsletBindings(SAXGeneratorModel model) {
        
        StringBuffer s = new StringBuffer();
        
        Iterator it = model.getParsletBindings().values().iterator();
        while (it.hasNext()) {
            ParsletBindings.Entry next = (ParsletBindings.Entry) it.next();
            s.append("\t<parslet parslet='" + next.getId() + "' return='" + next.getType() + "' />\n"); // NOI18N
        }
        
        return s.toString();
    }
        
}
