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
package org.netbeans.modules.php.latte.semantic;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.php.latte.parser.LatteParserResult;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteSemanticAnalyzer extends SemanticAnalyzer<LatteParserResult> {

    public LatteSemanticAnalyzer() {
    }

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return Collections.<OffsetRange, Set<ColoringAttributes>>emptyMap();
    }

    @Override
    public void run(LatteParserResult result, SchedulerEvent event) {
    }

    @Override
    public int getPriority() {
        return 300;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return null;
    }

    @Override
    public void cancel() {
    }

}
