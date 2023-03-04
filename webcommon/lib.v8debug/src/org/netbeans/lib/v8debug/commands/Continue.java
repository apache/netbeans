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
package org.netbeans.lib.v8debug.commands;

import org.netbeans.lib.v8debug.PropertyLong;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8StepAction;

/**
 *
 * @author Martin Entlicher
 */
public final class Continue {
    
    private Continue() {}
    
    public static V8Request createRequest(long sequence) {
        return new V8Request(sequence, V8Command.Continue, null);
    }
    
    public static V8Request createRequest(long sequence, V8StepAction stepAction) {
        return new V8Request(sequence, V8Command.Continue, new Arguments(stepAction));
    }
    
    public static V8Request createRequest(long sequence, V8StepAction stepAction, long stepCount) {
        return new V8Request(sequence, V8Command.Continue, new Arguments(stepAction, stepCount));
    }
    
    public static final class Arguments extends V8Arguments {

        private final V8StepAction stepAction;
        private final PropertyLong stepCount;

        public Arguments(V8StepAction stepAction) {
            this(stepAction, null);
        }

        public Arguments(V8StepAction stepAction, Long stepCount) {
            this.stepAction = stepAction;
            this.stepCount = new PropertyLong(stepCount);
        }

        public V8StepAction getStepAction() {
            return stepAction;
        }

        public PropertyLong getStepCount() {
            return stepCount;
        }
    }
}
