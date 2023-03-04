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
package org.netbeans.modules.javascript2.editor.hints;

import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class DuplicatePropertyName extends JsConventionHint {

    @Override
    public String getId() {
        return "jsduplicatepropertyname.hint"; // NOI18N
    }

    @Override
    @NbBundle.Messages("DuplicatePropertyNameDescription=Warns if there are defined more properties with the same name in a object literal.")
    public String getDescription() {
        return Bundle.DuplicatePropertyNameDescription();
    }

    @Override
    @NbBundle.Messages("DuplicatePropertyNameDisplayName=Duplicate Property Name")
    public String getDisplayName() {
        return Bundle.DuplicatePropertyNameDisplayName();
    }

}
