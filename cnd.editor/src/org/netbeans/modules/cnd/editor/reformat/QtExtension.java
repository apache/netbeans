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

package org.netbeans.modules.cnd.editor.reformat;

import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;

/**
 *
 */
class QtExtension {
    private boolean isQtObject = false;
    QtExtension() {
    }
    boolean isQtObject(){
        return isQtObject;
    }
    void checkQtObject(Token<CppTokenId> token){
        if (!isQtObject) {
            isQtObject = token.id() == IDENTIFIER && CharSequenceUtilities.equals(token.text(), "Q_OBJECT"); // NOI18N
        }
    }
    boolean isSignals(Token<CppTokenId> token){
        return token.id() == IDENTIFIER && CharSequenceUtilities.equals(token.text(), "signals"); // NOI18N
    }
    boolean isSlots(Token<CppTokenId> token){
        return token.id() == IDENTIFIER && CharSequenceUtilities.equals(token.text(), "slots"); // NOI18N
    }
}
