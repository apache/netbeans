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
import org.netbeans.lib.v8debug.V8Breakpoint;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;

/**
 *
 * @author Martin Entlicher
 */
public final class SetBreakpoint {
    
    private SetBreakpoint() {}
    
    public static V8Request createRequest(long sequence, V8Breakpoint.Type type,
                                          String target, Long line, Long column) {
        return new V8Request(sequence, V8Command.Setbreakpoint, new Arguments(type, target, line, column, null, null, null, null));
    }
    
    public static V8Request createRequest(long sequence, V8Breakpoint.Type type,
                                          String target, Long line, Long column,
                                          Boolean enabled, String condition, Long ignoreCount, Long groupId) {
        return new V8Request(sequence, V8Command.Setbreakpoint, new Arguments(type, target, line, column, enabled, condition, ignoreCount, groupId));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final V8Breakpoint.Type type;
        private final String target;
        private final PropertyLong line;
        private final PropertyLong column;
        private final PropertyBoolean enabled;
        private final String condition;
        private final PropertyLong ignoreCount;
        private final PropertyLong groupId;
        
        public Arguments(V8Breakpoint.Type type, String target,
                         Long line, Long column, Boolean enabled,
                         String condition, Long ignoreCount, Long groupId) {
            this.type = type;
            this.target = target;
            this.line = new PropertyLong(line);
            this.column = new PropertyLong(column);
            this.enabled = new PropertyBoolean(enabled);
            this.condition = condition;
            this.ignoreCount = new PropertyLong(ignoreCount);
            this.groupId = new PropertyLong(groupId);
        }

        public V8Breakpoint.Type getType() {
            return type;
        }

        public String getTarget() {
            return target;
        }

        public PropertyLong getLine() {
            return line;
        }

        public PropertyLong getColumn() {
            return column;
        }

        public PropertyBoolean isEnabled() {
            return enabled;
        }

        public String getCondition() {
            return condition;
        }

        public PropertyLong getIgnoreCount() {
            return ignoreCount;
        }

        public PropertyLong getGroupId() {
            return groupId;
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final V8Breakpoint.Type type;
        private final long breakpoint;
        private final String scriptName;
        private final PropertyLong line;
        private final PropertyLong column;
        private final V8Breakpoint.ActualLocation[] actualLocations;
        
        public ResponseBody(V8Breakpoint.Type type, long breakpoint,
                            String scriptName, Long line, Long column,
                            V8Breakpoint.ActualLocation[] actualLocations) {
            this.type = type;
            this.breakpoint = breakpoint;
            this.scriptName = scriptName;
            this.line = new PropertyLong(line);
            this.column = new PropertyLong(column);
            this.actualLocations = actualLocations;
        }

        public V8Breakpoint.Type getType() {
            return type;
        }

        public long getBreakpoint() {
            return breakpoint;
        }

        public String getScriptName() {
            return scriptName;
        }

        public PropertyLong getLine() {
            return line;
        }

        public PropertyLong getColumn() {
            return column;
        }

        public V8Breakpoint.ActualLocation[] getActualLocations() {
            return actualLocations;
        }
    }
}
