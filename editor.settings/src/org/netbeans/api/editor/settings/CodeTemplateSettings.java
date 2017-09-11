/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
