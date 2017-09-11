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

package org.netbeans.modules.editor.errorstripe.privatespi;

import java.awt.Color;

/**Provides description of a mark that should be displayed in the Error Stripe.
 *
 * @author Jan Lahoda
 */
public interface Mark {

    /**Mark that is error-like. The mark will be shown as
     * a thin horizontal line.
     */
    public static final int TYPE_ERROR_LIKE = 1;

    public static final int TYPE_CARET = 2;

    /**Default priority.
     */
    public static final int PRIORITY_DEFAULT = 1000;

    /**Return of what type is this mark. Currently only one type
     * exists: {@link #TYPE_ERROR_LIKE}. Other types may be
     * introduced later.
     *
     * @return {@link #TYPE_ERROR_LIKE}
     */
    public int getType();
    
    /**Returns status that represents this mark.
     *
     *@return status representing this mark
     */
    public Status getStatus();
    
    /**Returns priority of this mark. The priority prioritizes the marks in the same
     * status. The smaller number, the greater priority.
     *
     * @return priority of this mark
     * @see #PRIORITY_DEFAULT
     */
    public int getPriority();
    
    /**Returns enhanced (non-standard) color of this mark. If null, default color
     * for given status will be used.
     *
     * @return Color or null if default should be used.
     */
    public Color  getEnhancedColor();
    
    /**Returns line span which represents this mark.
     *
     * @return an array of size two, the first item represents starting line of the span,
     *         the second item ending line of the span. Both lines are inclusive.
     */
    public int[]  getAssignedLines();
    
    /**Return some human readable short description to be shown for
     * example in tooltips.
     *
     * @return a short description.
     */
    public String getShortDescription();
    
}
