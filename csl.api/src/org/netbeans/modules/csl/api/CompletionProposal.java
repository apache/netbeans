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
package org.netbeans.modules.csl.api;

import java.util.Set;

import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;


/**
 * An item to be added to the code completion dialog
 *
 * @author Tor Norbye
 */
public interface CompletionProposal {
    /**
     * The offset at which the completion item substitution should begin
     * @return The anchor offset
     */
    int getAnchorOffset();

    @CheckForNull
    ElementHandle getElement();

    @NonNull
    String getName();

    @NonNull
    String getInsertPrefix();

    @CheckForNull
    String getSortText();

    @NonNull
    String getLhsHtml(@NonNull HtmlFormatter formatter);

    @CheckForNull
    String getRhsHtml(@NonNull HtmlFormatter formatter);

    @NonNull
    ElementKind getKind();

    @CheckForNull
    ImageIcon getIcon();

    @NonNull
    Set<Modifier> getModifiers();
    
    /**
     * Return true iff this is a "smart" completion item - one that should be emphasized
     * (currently the IDE flushes these to the top and separates them with a line)
     * @return True iff this item is a smart completion item and should be shown first
     */
    boolean isSmart();

    /**
     * Most modules should return 0 here. If you return non-0, this is a sorting priority
     * for use by GSF when ordering the completion proposals. You can use this to override
     * the default behavior (which generally moves smart items to the top, and within each
     * smart/nonsmart section, orders for example variables higher than methods.
     * 
     * @return 0 to use normal sorting, or some other number to override the sorting priority
     *  of this item. Smaller items (including negative numbers) go nearer the top of the list.
     */
    int getSortPrioOverride();

    /**
     * Provide a custom live code template that will be inserted when
     * this item is chosen for insertion. 
     *
     * @return A live code template to be inserted into the document
     *   at the anchor offset. Return null to get the default behavior
     *   where the insert prefix, the insert params and param list delimiters
     *   are used instead.
     */
    @CheckForNull
    String getCustomInsertTemplate();
}
