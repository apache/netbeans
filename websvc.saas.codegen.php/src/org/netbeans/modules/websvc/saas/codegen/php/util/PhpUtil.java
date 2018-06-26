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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.saas.codegen.php.util;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.util.Util;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author ayubkhan
 */
public class PhpUtil {
    
    public static boolean isPhp(Document doc) {
        if(doc == null)
            return false;
        Object mimeType = doc.getProperty("mimeType"); //NOI18N
        if (mimeType != null && ("text/x-php5".equals(mimeType))) { //NOI18N
            return true;
        }
        return false;
    }
    
    public static String wrapWithTag(String content, Document doc, int insertStart) {
        String str = "";
        boolean addTag = !isWithinTag(doc, 0, insertStart);
        if(addTag)
            str += "\n<?php\n";
        str += content;
        if(addTag)
            str += "\n?>\n";
        return str;
    }
    
    public static boolean isWithinTag(Document doc, int start, int end) {
        try {
            String str = doc.getText(start, end - start);
            return str.lastIndexOf("<?php") > str.lastIndexOf("?>");
        } catch (BadLocationException ex) {
            return false;
        }
    }

    public static int findText(Document document, String searchText, boolean firstToLast) throws BadLocationException {
        int len = document.getLength();
        String content = document.getText(0, len);
        if(firstToLast)
            return content.indexOf(searchText);
        else
            return content.lastIndexOf(searchText);
    }
    
    public static String findParamValue(ParameterInfo param) {
        String paramVal = null;
        if (param.isApiKey()) {
            paramVal = "apiKey";
        } else {
            paramVal = Util.findParamValue(param);
        }
        return paramVal;
    }
}
