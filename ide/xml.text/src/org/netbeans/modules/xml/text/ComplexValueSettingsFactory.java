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

package org.netbeans.modules.xml.text;

import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.modules.xml.text.indent.DTDIndentEngine;
import org.netbeans.modules.xml.text.indent.XMLIndentEngine;
import org.openide.text.IndentEngine;

/**
 *
 * @author Vita Stejskal
 */
public final class ComplexValueSettingsFactory {

    // -----------------------------------------------------------------------
    // XML settings factory methods
    // -----------------------------------------------------------------------
    
    // XXX: use new editor.indent API
    public static IndentEngine getXMLIndentEngine() {
        return new XMLIndentEngine();
    }
    public static Acceptor getXMLIdentifierAcceptor() {
        return xmlIdentifierAcceptor;
    }
    public static Acceptor getXMLAbbrevResetAcceptor() {
        return abbrevResetAcceptor;
    }
    
    // -----------------------------------------------------------------------
    // DTD settings factory methods
    // -----------------------------------------------------------------------
    
    // XXX: use new editor.indent API
    public static IndentEngine getDTDIndentEngine() {
        return new DTDIndentEngine();
    }
    public static Acceptor getDTDAbbrevResetAcceptor() {
        return abbrevResetAcceptor;
    }

    // -----------------------------------------------------------------------
    // private stuff
    // -----------------------------------------------------------------------
    
    private static final Acceptor xmlIdentifierAcceptor = new Acceptor() {
        public boolean accept(char ch) {
            switch (ch) {
                case ' ': case '\t': case '\n': case '\r':          //NOI18N WS
                case '>': case '<': case '&': case '\'': case '"': case '/': //NOI18N
                case '\\': //NOI18N markup
                    return false;
            }

            return true;
        }
    };

    private static final Acceptor abbrevResetAcceptor = new Acceptor() {
        public boolean accept(char ch) {
          return AcceptorFactory.NON_JAVA_IDENTIFIER.accept(ch) && ch != '!' && ch != '?'; //NOI18N
        }
    };

    private ComplexValueSettingsFactory() {
        //no-op
    }
}
