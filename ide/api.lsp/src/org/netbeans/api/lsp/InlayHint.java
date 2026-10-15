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
package org.netbeans.api.lsp;

/**
 * An inlay hint.
 *
 * @since 1.34
 */
public class InlayHint {
    private final Position position;
    private final String text;

    /**
     * Create a new instance of InlayHint
     *
     * @param position the position where the hint should appear
     * @param text the text fo the hint
     */
    public InlayHint(Position position, String text) {
        this.position = position;
        this.text = text;
    }

    /**
     * Returns the position of the hint
     *
     * @return the position of the hint
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns the text of the hint
     *
     * @return the text of the hint
     */

    public String getText() {
        return text;
    }

}
