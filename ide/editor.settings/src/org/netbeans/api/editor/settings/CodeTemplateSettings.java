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

package org.netbeans.api.editor.settings;

import java.util.List;
import javax.swing.KeyStroke;

/**
 * The list of code templates available for particular mime paths. Instances of
 * this class can be retrieved from <code>MimeLookup</code> as shown on the example
 * below.
 * 
 * <pre>
 * Lookup l = MimeLookup.getLookup(MimePath.parse(mimePath));
 * CodeTemplateSettings cds = l.lookup(CodeTemplateSettings.class);
 * List<CodeTemplateDescription> codeTemplates = cds.getCodeTemplateDescriptions();
 * </pre>
 * 
 * <p><b>IMPORTANT</b>: There is a much more powerful API for working with editor
 * code templates in
 * <a href="@org-netbeans-lib-editor-codetemplates@/overview-summary.html">Editor Code Templates</a>
 * module. If you are retrieving this class from <code>MimeLookup</code> you should
 * should probably use the Editor Code Templates API instead.
 * 
 * <p><font color="red">This class must NOT be extended by any API clients.</font>
 *
 * @author Martin Roskanin
 */
public abstract class CodeTemplateSettings {

    /**
     * Construction prohibited for API clients.
     */
    public CodeTemplateSettings() {
        // Control instantiation of the allowed subclass only
        if (!getClass().getName().startsWith("org.netbeans.lib.editor.codetemplates")) { // NOI18N
            throw new IllegalStateException("Instantiation prohibited. " + getClass().getName()); // NOI18N
        }
    }
    
    /**
     * Gets the list of code template descriptions.
     *
     * @return An unmodifiable list of code template descriptions.
     */
    public abstract List<CodeTemplateDescription> getCodeTemplateDescriptions();
    
    /**
     * Gets the keystroke that expands the code templates abbreviations.
     *
     * @return A keystroke that expands code template abbreviations to
     *   its code text.
     */
    public abstract KeyStroke getExpandKey();
    
}
