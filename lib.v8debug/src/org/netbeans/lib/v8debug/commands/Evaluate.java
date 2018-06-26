/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.lib.v8debug.commands;

import org.netbeans.lib.v8debug.PropertyBoolean;
import org.netbeans.lib.v8debug.PropertyLong;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin Entlicher
 */
public final class Evaluate {
    
    private Evaluate() {}
    
    public static V8Request createRequest(long sequence, String expression) {
        return new V8Request(sequence, V8Command.Evaluate, new Arguments(expression));
    }
    
    public static V8Request createRequest(long sequence, String expression,
                                          Long frame, Boolean global,
                                          Boolean disableBreak,
                                          Arguments.Context[] additionalContext) {
        return new V8Request(sequence, V8Command.Evaluate,
                             new Arguments(expression, frame, global, disableBreak,
                                           additionalContext));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final String expression;
        private final PropertyLong frame;
        private final PropertyBoolean global;
        private final PropertyBoolean disableBreak;
        private final Context[] additionalContext;
        
        public Arguments(String expression) {
            this(expression, null, null, null, null);
        }
        
        public Arguments(String expression, Long frame, Boolean global,
                         Boolean disableBreak, Context[] additionalContext) {
            this.expression = expression;
            this.frame = new PropertyLong(frame);
            this.global = new PropertyBoolean(global);
            this.disableBreak = new PropertyBoolean(disableBreak);
            this.additionalContext = additionalContext;
        }

        public String getExpression() {
            return expression;
        }

        public PropertyLong getFrame() {
            return frame;
        }

        public PropertyBoolean isGlobal() {
            return global;
        }

        public PropertyBoolean isDisableBreak() {
            return disableBreak;
        }

        public Context[] getAdditionalContext() {
            return additionalContext;
        }
        
        public static final class Context {
            
            private final String name;
            private final long handle;
            
            public Context(String name, long handle) {
                this.name = name;
                this.handle = handle;
            }

            public String getName() {
                return name;
            }

            public long getHandle() {
                return handle;
            }
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final V8Value value;
        
        public ResponseBody(V8Value value) {
            this.value = value;
        }

        public V8Value getValue() {
            return value;
        }
    }
    
}
