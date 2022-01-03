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

package org.netbeans.modules.cnd.debugger.common2.values;

/**
 * To be implemented by viewmodel instance objects (like NativeBreakpoint).
 * They usually just have to call update() to re-pull the value from the model.
 *
 * OLD:
 * To be implemented by our Nodes so they can undo any effects in the editor 
 * of the user setting a property value.
 * They usually just have to call firePropertyChange(property, null, null).
 */

public interface EditUndoable {
    public void undo(String property);
}
