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

package org.netbeans.lib.editor.codetemplates.spi;

import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Filter accepting code templates being displayed in a code completion popup.
 * It is also used for editor hints (code templates) over a text selection.
 *
 * @author Dusan Balek
 */
public interface CodeTemplateFilter {
  
    /**
     * Accept or reject the given code template.
     * 
     * @param template non-null template to accept or reject.
     * @return true to accept the given code template or false to reject it.
     */
    boolean accept(CodeTemplate template);
    
    /**
     * Factory for producing of the code template filters.
     * <br/>
     * It should be registered in the MimeLookup for a given mime-type.
     */
    @MimeLocation(subfolderName="CodeTemplateFilterFactories")
    public interface Factory {
        
        /**
         * Create code template filter for the given context.
         * 
         * @param component non-null component for which the filter is being created.
         * @param offset &gt;=0 offset for which the filter is being created.
         * @return non-null code template filter instance.
         */
        CodeTemplateFilter createFilter(JTextComponent component, int offset);
    }
    
    /**
     * Factory for producing of the code template filters that filter templates
     * based on their contexts.
     * <br/>
     * It should be registered in the MimeLookup for a given mime-type.
     * 
     * @since 1.34
     */
    @MimeLocation(subfolderName="CodeTemplateFilterFactories")
    public interface ContextBasedFactory extends Factory {
        
        /**
         * Get the list of all code template contexts supported by filters
         * created by the factory.
         * @return non-null list of supported contexts.
         */
        List<String> getSupportedContexts();
    }
}
