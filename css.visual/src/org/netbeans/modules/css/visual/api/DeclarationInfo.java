/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual.api;

import org.netbeans.modules.css.model.api.PropertyDeclaration;

/**
 * Provides additional information for a {@link Declaration}.
 * 
 * @author marekfukala
 */
public enum DeclarationInfo {
    
    /**
     * Indicates the declaration is overridden by another declaration.
     */
    OVERRIDDEN,
    
    /**
     * Flags inactive declaration.
     * 
     * CSS rule that affects an element can either match the element or may match one
     * of its parents (and be inherited). Moreover, not all properties (only those
     * marked as inherited by the corresponding CSS spec.) from an inherited rule
     * affect the element. Hence, I am marking properties (from an inherited rule)
     * that are not inherited by DeclarationInfo.INACTIVE to emphasize that they are
     * not affecting the selected element. In summary, a property marked by this flag
     * is not inherited property from an inherited rule.
     * 
     * For the sake of completeness I have to add that I mark by this flag also
     * another group of properties (but I don't think that this must be covered by the
     * tooltip): the ones that use star or underscore CSS hack to affect some versions
     * of Internet Explorer only. These properties also do not affect the rendered
     * element because the inspected page runs in Chrome or in WebView (i.e. not in
     * IE). 
     */
    INACTIVE,
    
    /**
     * Flags erroneous declaration.
     */
    ERRONEOUS;
    
}
