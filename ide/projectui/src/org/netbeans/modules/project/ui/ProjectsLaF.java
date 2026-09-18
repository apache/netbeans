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
package org.netbeans.modules.project.ui;

import java.awt.Color;
import javax.swing.UIManager;

final class ProjectsLaF {
    private ProjectsLaF() {
    }

    /** Computes customized, or defaulted indentation prefix for
     * {@link ProjectsRootNode}. Reads two {@link UIManager} properties:
     * <ul>
     *   <li><code>nb.project.identation.char</code> - can be set to -1 to
     *      disable indentation altogether<li>
     *   <li><code>nb.project.indentation.color</code> - a color to use
     *      when rendering the indentation character in HTML
     * </ul>
     * both values come with reasonable defaults for L&Fs that don't specify them.
     *
     * @param html should we use or avoid HTML formatting
     * @return {@code null} when indentation is disabled, otherwise returns
     *    a text to insert on each indentation level
     */
    static String indentationPrefix(boolean html) {
        int indentChar = UIManager.getInt("nb.project.identation.char"); // NOI18N
        if (indentChar < 0) {
            // no indentation when explicitly set to negative
            return null;
        }
        if (indentChar == 0) {
            indentChar = 0xbb;
        }
        if (html) {
            Color indentColor = UIManager.getColor("nb.project.indentation.color"); // NOI18N
            if (indentColor == null) {
                indentColor = Color.gray;
            }
            int r = indentColor.getRed();
            int g = indentColor.getGreen();
            int b = indentColor.getBlue();

            return String.format("<font color='#%02x%02x%02x'>&#%d; </font>", r, g, b, indentChar); // NOI18N
        } else {
            return ((char) indentChar) + " "; // NOI18N
        }
    }

}
