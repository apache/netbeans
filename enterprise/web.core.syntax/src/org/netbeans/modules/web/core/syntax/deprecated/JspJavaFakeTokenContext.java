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

package org.netbeans.modules.web.core.syntax.deprecated;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.java.JavaTokenContext;

/**
 * Fake token contexts for JSP scriptlets, declarations and expressions
 *
 * @author Marek Fukala
 * @version 1.00
 * @deprecated Use JSP lexer instead
 */
@Deprecated
public class JspJavaFakeTokenContext {

    public static class JavaScriptletTokenContext extends TokenContext {

        // Context instance declaration
        public static final JavaScriptletTokenContext context = new JavaScriptletTokenContext();
        
        /** Token path for embeded java token context */
        public static final TokenContextPath contextPath =
            context.getContextPath(JavaTokenContext.contextPath);
        
        private JavaScriptletTokenContext() {
            super("", new TokenContext[] {  // NOI18N
                JavaTokenContext.context
            }
            );
            
            try {
                addDeclaredTokenIDs();
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
        }
        
    }
    
    public static class JavaDeclarationTokenContext extends TokenContext {
        
        // Context instance declaration
        public static final JavaDeclarationTokenContext context = new JavaDeclarationTokenContext();
        
        /** Token path for embeded java token context */
        public static final TokenContextPath contextPath =
            context.getContextPath(JavaTokenContext.contextPath);
        
        private JavaDeclarationTokenContext() {
            super("", new TokenContext[] {  // NOI18N
                JavaTokenContext.context
            }
            );
            
            try {
                addDeclaredTokenIDs();
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
        }
        
    }
    
    public static class JavaExpressionTokenContext extends TokenContext {
        
        // Context instance declaration
        public static final JavaExpressionTokenContext context = new JavaExpressionTokenContext();
        
        /** Token path for embeded java token context */
        public static final TokenContextPath contextPath =
            context.getContextPath(JavaTokenContext.contextPath);
        
        private JavaExpressionTokenContext() {
            super("", new TokenContext[] {  // NOI18N
                JavaTokenContext.context
            }
            );
            
            try {
                addDeclaredTokenIDs();
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
        }
        
    }
    
    
}

