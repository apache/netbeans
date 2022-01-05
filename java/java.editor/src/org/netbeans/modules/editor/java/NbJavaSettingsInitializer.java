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

package org.netbeans.modules.editor.java;

import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;

/**
* Nb settings for Java.
*
* @author Miloslav Metelka
* @version 1.00
*/

public final class NbJavaSettingsInitializer {

    public static Acceptor getAbbrevResetAcceptor() {
        return AcceptorFactory.NON_JAVA_IDENTIFIER;
    }
    public static Acceptor getIdentifierAcceptor() {
        return AcceptorFactory.JAVA_IDENTIFIER;
    }
    public static Acceptor getIndentHotCharsAcceptor() {
        return indentHotCharsAcceptor;
    }
    private NbJavaSettingsInitializer() {
    }

    private static final Acceptor indentHotCharsAcceptor = new Acceptor() {
        public boolean accept(char ch) {
            switch (ch) {
                case '{': //NOI18N
                case '}': //NOI18N
                    return true;
            }
            return false;
        }
    };
    
}
