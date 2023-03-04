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

package org.netbeans.modules.groovy.grailsproject.ui.wizards.impl;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.extexecution.input.LineProcessor;

/**
 *
 * @author Petr Hejl
 */
public class ProgressLineProcessor implements LineProcessor {

    private final ProgressHandle progress;

    private final int max;

    private final int step;

    private int value;

    public ProgressLineProcessor(ProgressHandle progress, int max, int step) {
        this.progress = progress;
        this.max = max;
        this.step = step;
    }

    public void processLine(String line) {
        value += step;
        if (value > max) {
            value = max;
        }

        progress.progress(value);
    }

    public void reset() {
        // noop
    }

    public void close() {
        value = max;
        progress.progress(max);
    }
}
