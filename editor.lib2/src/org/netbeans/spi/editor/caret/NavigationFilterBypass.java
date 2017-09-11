/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.spi.editor.caret;

import javax.swing.text.NavigationFilter;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.caret.MoveCaretsOrigin;

/**
 * Enhanced FilterBypass which understands multicaret. 
 * <p>
 * Implementations of
 * {@link NavigationFilter} may check if the FilterBypass is instanceof this class,
 * and if so, they can access extended information.
 * </p><p>
 * If the caret move operation is initiated by new caret APIs, the FilterBypass passed
 * to NavigationFilters always satisfies this interface.
 * </p>
 * @author sdedic
 * @since 2.10
 */
public abstract class NavigationFilterBypass extends NavigationFilter.FilterBypass {
    /**
     * Returns the currently changing CaretItem.
     * 
     * @return CaretItem the caret instance being changed
     */
    public abstract @NonNull CaretInfo           getCaretItem();
    
    /**
     * Access to the entire EditorCaret abstraction
     * @return the editor caret
     */
    public abstract @NonNull EditorCaret         getEditorCaret();
    
    /**
     * Describes the origin / reason of the movement.
     * @return The origin object provided by the caret movement initiator.
     */
    public abstract @NonNull MoveCaretsOrigin    getOrigin();
}
