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
package org.netbeans.modules.php.blade.editor.completion;

import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.php.blade.csl.elements.NamedElement;
import org.netbeans.modules.php.blade.csl.elements.PhpFunctionElement;
import org.netbeans.modules.php.blade.editor.path.BladePathUtils;

/**
 * @TODO update doc representation
 * 
 * @author bogdan
 */
public class TooltipDoc {

    public static Documentation generateDoc(NamedElement elementHandle) {
        Documentation result = null;
        switch (elementHandle.getType()) {
            case PATH:
                String filePath = "";
                if (elementHandle.getFileObject() != null){
                    filePath = BladePathUtils.getRelativeProjectPath(elementHandle.getFileObject());
                }
                return Documentation.create(String.format("<div align=\"right\"><font size=-1>%s</font></div>", "blade path")
                        + "<div><b>" + filePath + "</b></div>", null);
            case CUSTOM_DIRECTIVE:
                String docInfo = String.format("<div align=\"right\"><font size=-1>%s</font></div>", "custom directive")
                        + "<div>" + elementHandle.getFileObject().getNameExt() + "</div>";
                return Documentation.create(docInfo, null);
        }

        return result;
    }
    
    public static Documentation generateFunctionDoc(PhpFunctionElement elementHandle) {
        String info = "<div align=\"left\"><b>" + elementHandle.getName() + elementHandle.getParamsAsString() + "</b></div>";
        if (elementHandle.getNamespace() != null){
            info += "<div>" + elementHandle.getNamespace() + "</div>";
        }
        info += "<div>" + elementHandle.getFileObject().getNameExt() + "</div>";
        info += String.format("<div align=\"right\"><font size=-1>%s</font></div>", "php function");
        return Documentation.create(info, null);
    }
}
