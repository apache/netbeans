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

package org.netbeans.modules.cnd.asm.base.syntax;

import java.io.Reader;

import org.netbeans.modules.cnd.asm.base.att.ATTIdentResolver;
import org.netbeans.modules.cnd.asm.base.att.ATTParser;
import org.netbeans.modules.cnd.asm.model.AbstractAsmModel;
import org.netbeans.modules.cnd.asm.model.AsmSyntax;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmHighlightLexer;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmParser;

public abstract class BaseAsmSyntax implements AsmSyntax {
        
        final IdentResolver resolver;
        final ScannerFactory fact;
        
        public BaseAsmSyntax(AbstractAsmModel model, ScannerFactory fact) {                       
            this.fact = fact;
            resolver = new ATTIdentResolver(model);
        }

        public AsmParser createParser() {            
            ATTParser parser = new ATTParser(fact, resolver);
            patchAttParser(parser);
            
            return parser;
        }

        public AsmHighlightLexer createHighlightLexer(Reader input, Object state) {                                           
            return new AntlrLexer(fact.createScanner(input, state), 
                                  resolver);
        }    
        
        protected abstract void patchAttParser(ATTParser parser);
}
