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

import java.io.CharConversionException;
import javax.swing.UIManager;
import org.openide.xml.XMLUtil;

final class ProjectsLaF {
    private ProjectsLaF() {
    }

    /** Computes customized, or defaulted indentation prefix for
     * {@link ProjectsRootNode}. Reads following {@link UIManager} properties:
     * <ul>
     *   <li><code>nb.project.indentation.repeat</code> - the text to
     *       insert on each indentation level (including necessary spaces)
     *       - a reasonable default is provided if the property isn't specified
     *   <li>
     *   <li><code>nb.project.indentation.prefix</code> - optional indentation prefix
     *   <li><code>nb.project.indentation.postfix</code> - optional indentation postfix
     *   <li>
     *   <li><code>controlShadow</code> - a color to use
     *      when rendering the indentation text in HTML
     *      the color is defined and used by other modules as well
     * </ul>
     *
     * @param html should we use or avoid HTML formatting
     * @param depth the depth of indentation level
     * @return the text to represent the indentation, returns {@code ""}
     *    when indentation is disabled
     */
    static String indentationPrefix(boolean html, int depth) {
        String prefix = UIManager.getString("nb.project.indentation.prefix"); // NOI18N
        if (prefix == null) {
            prefix = "";
        }
        String repeat = UIManager.getString("nb.project.indentation.repeat"); // NOI18N
        if (repeat == null) {
            repeat = "\u00bb ";
        }
        String postfix = UIManager.getString("nb.project.indentation.postfix"); // NOI18N
        if (postfix == null) {
            postfix = "";
        }
        if (depth <= 0 || repeat.isEmpty()) {
            return "";
        }
        if (html) {
            try {
                String xml = XMLUtil.toElementContent(prefix + repeat.repeat(depth) + postfix);
                return "<font color='!controlShadow'>" + xml + "</font>";
            } catch (CharConversionException ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            return prefix + repeat.repeat(depth) + postfix;
        }
    }}
