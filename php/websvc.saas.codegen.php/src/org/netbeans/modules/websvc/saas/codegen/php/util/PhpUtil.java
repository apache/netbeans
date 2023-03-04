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
