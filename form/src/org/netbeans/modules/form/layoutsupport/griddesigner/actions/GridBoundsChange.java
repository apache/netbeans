/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.form.layoutsupport.griddesigner.actions;

/**
 * Information about column/row changes.
 *
 * @author Jan Stola
 */
public class GridBoundsChange {
    /** Old column bounds. */
    private int[] oldColumnBounds;
    /** Old row bounds. */
    private int[] oldRowBounds;
    /** New column bounds. */
    private int[] newColumnBounds;
    /** New row bounds. */
    private int[] newRowBounds;

    /**
     * Creates new {@code GridBoundsChange}.
     *
     * @param oldColumnBounds old column bounds.
     * @param oldRowBounds old row bounds.
     * @param newColumnBounds new column bounds.
     * @param newRowBounds new row bounds.
     */
    public GridBoundsChange(int[] oldColumnBounds, int[] oldRowBounds,
            int[] newColumnBounds, int[] newRowBounds) {
        this.oldColumnBounds = oldColumnBounds;
        this.oldRowBounds = oldRowBounds;
        this.newColumnBounds = newColumnBounds;
        this.newRowBounds = newRowBounds;
    }

    /**
     * Returns new column bounds.
     *
     * @return new column bounds.
     */
    public int[] getNewColumnBounds() {
        return newColumnBounds;
    }

    /**
     * Returns new row bounds.
     *
     * @return new row bounds.
     */
    public int[] getNewRowBounds() {
        return newRowBounds;
    }

    /**
     * Returns old column bounds.
     *
     * @return old column bounds.
     */
    public int[] getOldColumnBounds() {
        return oldColumnBounds;
    }

    /**
     * Returns old row bounds.
     *
     * @return old row bounds.
     */
    public int[] getOldRowBounds() {
        return oldRowBounds;
    }

}
