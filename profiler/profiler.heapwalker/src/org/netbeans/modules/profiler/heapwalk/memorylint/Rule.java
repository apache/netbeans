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

package org.netbeans.modules.profiler.heapwalk.memorylint;

import javax.swing.JComponent;


/**
 *
 * @author nenik
 */
public abstract class Rule {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JComponent customizer;
    private String description;
    private String displayName;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    protected Rule(String name, String desc) {
        displayName = name;
        description = desc;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public final String getDescription() {
        return description;
    }

    public final String getDisplayName() {
        return displayName;
    }

    /**
     * The rule can provide a long description in the form of HTML page.
     * This method should directly return the HTML page content.
     * If there are any relative URLs in the html code (images, style sheet),
     * they are interpretted as relative to the Rule's class file.
     *
     * @return the HTML description code or null if the rule has no
     * HTML description.
     */
    public String getHTMLDescription() {
        return null;
    }

    public abstract void perform();

    public abstract void prepare(MemoryLint context);

    public JComponent getCustomizer() {
        if (customizer == null) {
            customizer = createCustomizer();
        }

        return customizer;
    }

    /** Factory method to create customizer for adjusting
     * rule parameters.
     * @return UI component or <code>null</code>
     */
    protected abstract JComponent createCustomizer();

    protected String resultsHeader() {
        return "<h2>" + getDisplayName() + "</h2>"; // NOI18N
    }
}
