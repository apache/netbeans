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
package org.netbeans.modules.editor.lib2.caret;

import java.awt.Point;
import java.util.List;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Editor caret may request a possible fold(s) expansion upon dot or mark position setting.
 *
 * @author Miloslav Metelka
 */
public abstract class CaretFoldExpander {
    
    private static CaretFoldExpander caretFoldExpander;
    
    public static CaretFoldExpander get() {
        return caretFoldExpander;
    }
    
    public static void register(CaretFoldExpander caretFoldExpander) {
        CaretFoldExpander.caretFoldExpander = caretFoldExpander;
    }
    
    /**
     * Check whether any of the given positions points to a collapsed fold and if so
     * then expand these folds.
     *
     * @param c non-null text component.
     * @param posList non-null list of positions to check.
     */
    public abstract void checkExpandFolds(@NonNull JTextComponent c, @NonNull List<Position> posList);
    
    /**
     * Check whether there's an unexpanded fold at the point that caret points to
     * and if so then expand it.
     *
     * @param c non-null text component.
     * @param p point (relative to the component) to which the caret points.
     */
    public abstract boolean checkExpandFold(@NonNull JTextComponent c, @NonNull Point p);

}
