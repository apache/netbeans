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

package lookformyself;

import org.openide.modules.ModuleInfo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;

public class Loder extends CallableSystemAction {
    
    public static boolean foundEarly;
    
    public Loder() {
        initialize();
    }
    
    @Override
    protected void initialize() {
        // Now the real stuff:
        foundEarly = foundNow();
    }
    
    public static boolean foundNow() {
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (m.getCodeNameBase().equals("lookformyself")) {
                return true;
            }
        }
        return false;
    }
    
    public void performAction () {
        throw new IllegalStateException("Never called");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return "LoderAction";
    }
    
}
