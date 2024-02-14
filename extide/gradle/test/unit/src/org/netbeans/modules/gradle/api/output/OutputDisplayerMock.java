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
package org.netbeans.modules.gradle.api.output;

import java.util.ArrayList;
import org.openide.windows.IOColors;

/**
 *
 * @author lkishalmi
 */
public class OutputDisplayerMock extends OutputDisplayer {

    private final ArrayList<OutputItem> items = new ArrayList<>();

    @Override
    protected void doPrint(CharSequence text, Runnable action, IOColors.OutputType type) {
        items.add(new OutputItem(text.toString(), action, type));
    }

    public OutputItem[] getOutputs() {
        return items.toArray(new OutputItem[0]);
    }

    public String getOutput() {
        StringBuilder sb = new StringBuilder();
        for (OutputItem item : items) {
            sb.append(item);
        }
        return sb.toString();
    }

    public class OutputItem {
        public final String text;
        public final Runnable action;
        public final IOColors.OutputType type;

        public OutputItem(String text, Runnable action, IOColors.OutputType type) {
            this.text = text;
            this.action = action;
            this.type = type;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
