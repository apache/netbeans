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

import java.util.Collections;
import java.util.Map;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;

/**
 *
 * @author Martin Entlicher
 */
public final class Flags {
    
    public static final String FLAG_BREAK_POINTS_ACTIVE = "breakPointsActive";
    public static final String FLAG_BREAK_ON_CAUGHT_EXCEPTION = "breakOnCaughtException";
    public static final String FLAG_BREAK_ON_UNCAUGHT_EXCEPTION = "breakOnUncaughtException";
    
    private Flags() {}
    
    public static V8Request createRequest(long sequence) {
        return new V8Request(sequence, V8Command.Flags, new Arguments());
    }
    
    public static V8Request createRequest(long sequence, String name, boolean value) {
        return new V8Request(sequence, V8Command.Flags, new Arguments(name, value));
    }
    
    public static V8Request createRequest(long sequence, Map<String, Boolean> flags) {
        return new V8Request(sequence, V8Command.Flags, new Arguments(flags));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final Map<String, Boolean> flags;
        
        public Arguments() {
            this(null);
        }
        
        public Arguments(String name, boolean value) {
            this(Collections.singletonMap(name, value));
        }
        
        public Arguments(Map<String, Boolean> flags) {
            this.flags = flags;
        }

        public Map<String, Boolean> getFlags() {
            return flags;
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final Map<String, Boolean> flags;
        
        public ResponseBody() {
            this(Collections.EMPTY_MAP);
        }
        
        public ResponseBody(Map<String, Boolean> flags) {
            this.flags = flags;
        }

        public Map<String, Boolean> getFlags() {
            return flags;
        }
    }
    
}
