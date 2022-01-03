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

package org.netbeans.modules.cnd.highlight.hints;

import java.util.List;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 9100)
public final class FixProvider extends CsmErrorInfoHintProvider {

    @Override
    protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
        if (info instanceof DisableHintFix.CodeAuditInfo) {
            alreadyFound.add(new DisableHintFix((DisableHintFix.CodeAuditInfo) info));
        }
        return alreadyFound;
    }
}
