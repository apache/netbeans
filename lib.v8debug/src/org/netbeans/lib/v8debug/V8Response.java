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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 * A command response.
 * 
 * @author Martin Entlicher
 */
public final class V8Response extends V8Packet {
    
    private final long requestSequence;
    private final V8Command command;
    private final V8Body body;
    private final ReferencedValue[] referencedValues;
    private Map<Long, V8Value> valuesByReferences;
    private final boolean running;
    private final boolean success;
    private final String errorMessage;
    
    V8Response(long sequence, long requestSequence, V8Command command, V8Body body,
               ReferencedValue[] referencedValues, boolean running, boolean success,
               String errorMessage) {
        super(sequence, V8Type.response);
        this.requestSequence = requestSequence;
        this.command = command;
        this.body = body;
        this.referencedValues = referencedValues;
        this.running = running;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public long getRequestSequence() {
        return requestSequence;
    }

    public V8Command getCommand() {
        return command;
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
                valuesByReferences = createValuesByReference(referencedValues);
            }
            return valuesByReferences.get(reference);
        }
    }
    static Map<Long, V8Value> createValuesByReference(ReferencedValue[] referencedValues) {
        Map<Long, V8Value> valuesByReferences = new HashMap<>();
        for (int i = 0; i < referencedValues.length; i++) {
            valuesByReferences.put(referencedValues[i].getReference(), referencedValues[i].getValue());
        }
        return valuesByReferences;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
}
