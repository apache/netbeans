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

package org.netbeans.modules.refactoring.java.ui;

import org.netbeans.modules.refactoring.java.plugins.InstantRefactoringPerformer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
public class HighlightsLayerFactory implements org.netbeans.spi.editor.highlighting.HighlightsLayerFactory{
    public HighlightsLayer[] createLayers(org.netbeans.spi.editor.highlighting.HighlightsLayerFactory.Context context) {
        return new HighlightsLayer[] {
            //"above" mark occurrences, "below" search layers:
            HighlightsLayer.create(InstantRefactoringPerformer.class.getName(), ZOrder.SHOW_OFF_RACK.forPosition(26), true, InstantRefactoringPerformer.getHighlightsBag(context.getDocument())),
        };
    }
}
