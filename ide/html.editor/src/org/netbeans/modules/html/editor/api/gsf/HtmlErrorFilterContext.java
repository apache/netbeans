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

package org.netbeans.modules.html.editor.api.gsf;

import org.netbeans.modules.csl.api.RuleContext;

/**
 * Extended RuleContext, which provides a flag extension can test to speed up the processing.
 * The error rules are processed during indexing. Unless tasklist is displayed and the file in question
 * is in its scope, the extension should check {@link #isOnlyBadging} and avoid any rules that
 * do just hinting and not error badging to improve performance.
 * <p/>
 * See defect #
 * 
 * @author sdedic
 */
public final class HtmlErrorFilterContext extends RuleContext {
    private boolean onlyBadging;

    public HtmlErrorFilterContext() {
    }
    
    public void setOnlyBadging(boolean enable) {
        this.onlyBadging = enable;
    }

    public boolean isOnlyBadging() {
        return onlyBadging;
    }
}
