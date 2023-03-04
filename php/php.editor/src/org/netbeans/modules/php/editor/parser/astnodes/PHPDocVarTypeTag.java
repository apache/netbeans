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
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 * Represents tags like param, where is defined type of a variable.
 * @author Petr Pisl
 */
public class PHPDocVarTypeTag extends PHPDocTypeTag {

    private final PHPDocNode variable;

    public PHPDocVarTypeTag(int start, int end, AnnotationParsedLine kind, String value,
            List<PHPDocTypeNode> types, PHPDocNode variable) {
        super(start, end, kind, value, types);
        this.variable = variable;
    }

    /**
     *
     * @return can be null, if the variable is not defined (doesn't start with $)
     */
    public PHPDocNode getVariable() {
        return variable;
    }

    @Override
    public String getDocumentation() {
        if (documentation == null) {
            int index = getValue().trim().indexOf(variable.getValue());
            if (index > -1) {
                if (index == 0) {
                    // first space after type
                    String trimmedValue = getValue().trim();
                    int firstSpace = trimmedValue.indexOf(" "); //NOI18N
                    int firstTab = trimmedValue.indexOf("\t"); //NOI18N
                    int delimiterIndex = -1;
                    if (firstSpace > 0 && (firstSpace < firstTab || firstTab == -1)) {
                        delimiterIndex = firstSpace;
                    } else if (firstTab > 0 && (firstTab < firstSpace || firstSpace == -1)) {
                        delimiterIndex = firstTab;
                    }
                    if (delimiterIndex != -1) {
                        documentation = trimmedValue.substring(delimiterIndex).trim();
                    } else {
                        documentation = ""; //NOI18N
                    }
                } else {
                    documentation = getValue().trim().substring(index + variable.getValue().length()).trim();
                }
            }
        }
        return documentation;
    }


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
