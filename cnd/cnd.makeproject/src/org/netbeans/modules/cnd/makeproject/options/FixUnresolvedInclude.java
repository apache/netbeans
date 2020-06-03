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
package org.netbeans.modules.cnd.makeproject.options;

import org.netbeans.modules.cnd.utils.NamedOption;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(path=NamedOption.OTHER_CATEGORY, service=NamedOption.class, position=800)
public class FixUnresolvedInclude extends NamedOption {
    public static final String FIX_UNRESOLVED_INCLUDE = "fixUnresolvedInclude"; // NOI18N

    @Override
    public String getName() {
        return FIX_UNRESOLVED_INCLUDE;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(FixUnresolvedInclude.class, "fixUnresolvedInclude"); //NOI18N
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public OptionKind getKind() {
        return OptionKind.Boolean;
    }

    @Override
    public Object getDefaultValue() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return Boolean.getBoolean(NamedOption.EXTRA_OPTIONS_FLAG);
    }
        
}
