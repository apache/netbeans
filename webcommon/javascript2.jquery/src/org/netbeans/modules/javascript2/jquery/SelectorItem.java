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
package org.netbeans.modules.javascript2.jquery;

/**
 *
 *  @author Petr Hejl
 */
public final class SelectorItem {
    final String displayText;
    private final String insertTemplate;
    private final String helpId;
    private final String helpText;

    public SelectorItem(String displayText) {
        this(displayText, displayText, null, null);
    }

    public SelectorItem(String displayText, String insertTemplate) {
        this(displayText, insertTemplate, null, null);
    }

    public SelectorItem(String displayText, String insertTemplate, String helpId, String helpText) {
        this.displayText = displayText;
        this.insertTemplate = insertTemplate;
        this.helpId = helpId;
        this.helpText = helpText;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getInsertTemplate() {
        return insertTemplate;
    }

    public String getHelpId() {
        return helpId;
    }

    public String getHelpText() {
        return helpText;
    }

}
