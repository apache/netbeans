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
@ServiceProvider(path=NamedOption.MAKE_PROJECT_CATEGORY, service=NamedOption.class, position=2000)
public class ResolveSymbolicLinks extends NamedOption {
    // Reuse
    public static final String RESOLVE_SYMBOLIC_LINKS = "resolveSymbolicLinks";  // NOI18N
    
    @Override
    public String getName() {
        return RESOLVE_SYMBOLIC_LINKS;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ReuseOutputTab.class, "RESOLVE_SYMBOLIC_LINKS_CHECKBOX_TXT"); //NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ReuseOutputTab.class, "RESOLVE_SYMBOLIC_LINKS_CHECKBOX_AD"); //NOI18N
    }

    @Override
    public NamedOption.OptionKind getKind() {
        return NamedOption.OptionKind.Boolean;
    }

    @Override
    public Object getDefaultValue() {
        return Boolean.getBoolean("cnd.project.resolve.symlinks");
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}
