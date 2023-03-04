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
package org.netbeans.lib.nbjshell;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.jshell.execution.StreamingExecutionControl;

public class NbExecutionControlBase extends StreamingExecutionControl implements NbExecutionControl {
    private static final Logger LOG = Logger.getLogger(NbExecutionControlBase.class.getName());

    private ObjectInput   remoteIn;
    private ObjectOutput  remoteOut;

    public NbExecutionControlBase(ObjectOutput out, ObjectInput in) {
        super(out, in);
        this.remoteOut = out;
        this.remoteIn = in;
    }

    public ObjectOutput getRemoteOut() {
        return remoteOut;
    }
    
    protected ObjectInput  getRemoteIn() {
        return remoteIn;
    }
    
    @Override
    public Map<String, String> commandVersionInfo() {
        Map<String, String> result = new HashMap<>();
        try {
            Object o = extensionCommand("nb_vmInfo", null);
            if (!(o instanceof Map)) {
                return Collections.emptyMap();
            }
            result = (Map<String, String>)o;
        } catch (RunException | InternalException ex) {
            LOG.log(Level.INFO, "Error invoking JShell agent", ex.toString());
        } catch (EngineTerminationException ex) {
            shutdown();
        }
        return result;
    }

    protected void shutdown() {
        if (remoteIn == null) {
            return;
        }
        try {
            remoteIn.close();
            remoteOut.close();
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Error closing streams", ex);
        }
    }
    
}
