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
