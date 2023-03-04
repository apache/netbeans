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
package org.netbeans.modules.csl.hints.infrastructure;

import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;

/**
 *
 * @author Tor Norbye
 */
final class FixWrapper implements EnhancedFix {

    private org.netbeans.modules.csl.api.HintFix fix;
    private String sortText;

    FixWrapper(org.netbeans.modules.csl.api.HintFix fix, String sortText) {
        this.fix = fix;
        this.sortText = sortText;
    }

    public String getText() {
        return fix.getDescription();
    }

    public ChangeInfo implement() throws Exception {
        fix.implement();

        return null;
    }

    public CharSequence getSortText() {
        return sortText;
    }
}

