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

package org.netbeans.modules.php.analysis.ui.options;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Collection;

public final class AnalysisCategoryPanels {

    private AnalysisCategoryPanels() {
    }

    /**
     * Get a list of new category panels.
     * <p>
     * This method must be called in the UI thread.
     * @return a list of new category panels, never {@code null}
     */
    public static Collection<AnalysisCategoryPanel> getCategoryPanels() {
        assert EventQueue.isDispatchThread();
        // can be easily improved (e.g. Lookup.forPath())
        return Arrays.asList(
                new CodeSnifferOptionsPanel(),
                new MessDetectorOptionsPanel(),
                new CodingStandardsFixerOptionsPanel(),
                new PHPStanOptionsPanel(),
                new PsalmOptionsPanel());
    }

}
