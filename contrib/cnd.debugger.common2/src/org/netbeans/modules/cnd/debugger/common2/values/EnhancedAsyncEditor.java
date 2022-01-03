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


import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

abstract public class EnhancedAsyncEditor extends AsyncEditor
    implements ExPropertyEditor {

    // If we have tags and canEditAsText is TRUE there's problems with
    // focus. See IZ 76522.

    @Override
    abstract public String[] getTags();

    // interface ExPropertyEditor
    @Override
    public void attachEnv(PropertyEnv env) {
	env.getFeatureDescriptor().setValue("canEditAsText", Boolean.TRUE); // NOI18N
    }
}

