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

package org.netbeans.modules.cnd.debugger.dbx;

import javax.swing.Action;

import org.netbeans.modules.cnd.debugger.common2.debugger.Variable;
import org.netbeans.modules.cnd.debugger.common2.debugger.ModelChangeDelegator;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;

public class DebuggerVariable extends Variable {

    public DebuggerVariable (ModelChangeDelegator updater, Variable parent, 
			    String name, String type, String value) {
	super(updater, parent, name, type, value);
    }

    public String getDebugInfo() {return null;}
    public NativeDebugger getDebugger(){return null;}
    public void removeAllDescendantFromOpenList(boolean isLocal){};

    public Action[] getActions(boolean isWatch){return null;}
    public boolean getDelta(){return true;}
    public void setVariableValue(String newValue){};

    // Can we use the array browser on this variable?
    public boolean isArrayBrowsable() {return true;}

    public void noteExpanded(boolean isWatch) {};
    public void noteCollapsed(boolean isWatch) {};

    public void postFormat(String format) {};
    public String getFormat(){return null;}

    public void createWatch() {}
}
