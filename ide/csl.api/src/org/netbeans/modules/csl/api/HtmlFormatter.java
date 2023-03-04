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

package org.netbeans.modules.csl.api;

import org.netbeans.api.annotations.common.NonNull;


/**
 * A formatter used to format items for navigation, code completion, etc.
 * Language plugins should build up HTML strings by calling logical
 * methods on this class, and suitable HTML will be constructed (using
 * whatever colors and attributes are appropriate for the different logical 
 * sections and so on). This places formatting logic within the IDE such that
 * it can be theme sensitive (and changed without replicating logic in the plugins).
 *
 * @author Tor Norbye
 */
public abstract class HtmlFormatter {
    protected int textLength;
    protected int maxLength = Integer.MAX_VALUE;
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    public abstract void reset();
    public abstract void appendHtml(String html);
    public void appendText(@NonNull String text) {
        appendText(text, 0, text.length());
    }
    public abstract void appendText(@NonNull String text, int fromInclusive, int toExclusive);

    public abstract void emphasis(boolean start);
    public abstract void name(@NonNull ElementKind kind, boolean start);
    public abstract void parameters(boolean start);
    public abstract void active(boolean start);
    public abstract void type(boolean start);
    public abstract void deprecated(boolean start);
    
    public abstract @NonNull String getText();
}
