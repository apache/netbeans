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
package org.netbeans.lib.v8debug;

import java.util.Map;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 * An event that occurs in the debugger backend.
 * 
 * @author Martin Entlicher
 */
public final class V8Event extends V8Packet {
    
    public static enum Kind {
        Break,
        Exception,
        AfterCompile,
        ScriptCollected,
        CompileError;       // ES6

        @Override
        public String toString() {
            String str = super.toString();
            return Character.toLowerCase(str.charAt(0)) + str.substring(1);
        }
        
        static Kind fromString(String eventName) {
            eventName = Character.toUpperCase(eventName.charAt(0)) + eventName.substring(1);
            return Kind.valueOf(eventName);
        }
        
    }
    
    private final Kind eventKind;
    private final V8Body body;
    private final ReferencedValue[] referencedValues;
    private Map<Long, V8Value> valuesByReferences;
    private final PropertyBoolean running;
    private final PropertyBoolean success;
    private final String errorMessage;
    
    public V8Event(long sequence, Kind eventKind, V8Body body,
                   ReferencedValue[] referencedValues, Boolean running,
                   Boolean success, String errorMessage) {
        super(sequence, V8Type.event);
        this.eventKind = eventKind;
        this.body = body;
        this.referencedValues = referencedValues;
        this.running = new PropertyBoolean(running);
        this.success = new PropertyBoolean(success);
        this.errorMessage = errorMessage;
    }

    public Kind getKind() {
        return eventKind;
    }

    public V8Body getBody() {
        return body;
    }
    
    public ReferencedValue[] getReferencedValues() {
        return referencedValues;
    }
    
    public V8Value getReferencedValue(long reference) {
        if (referencedValues == null || referencedValues.length == 0) {
            return null;
        }
        synchronized (this) {
            if (valuesByReferences == null) {
                valuesByReferences = V8Response.createValuesByReference(referencedValues);
            }
            return valuesByReferences.get(reference);
        }
    }

    public PropertyBoolean isRunning() {
        return running;
    }

    public PropertyBoolean getSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
