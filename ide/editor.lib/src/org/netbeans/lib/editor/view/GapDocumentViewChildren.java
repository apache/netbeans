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

package org.netbeans.lib.editor.view;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.LayoutQueue;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.editor.view.spi.ViewLayoutState;

//import org.netbeans.spi.lexer.util.GapObjectArray;

/**
 * Maintainer of the children of the {@link GapBoxView}.
 * <br>
 * It also acts as a runnable task for flushing requirement
 * changes of the view it works for.
 *
 * Besides the current implementation there could be
 * an implementation for small number of children (e.g. up to 20)
 * which would not have to use index gap and which would use indexes
 * &lt;= 127 that could be merged into shorts by pairs.
 * This approach could save about 40 bytes.
 * <br>
 * However the GapBoxView is able to unload its children
 * dynamically which can save much more memory
 * than the described simplified implementation.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

class GapDocumentViewChildren extends GapBoxViewChildren {

    GapDocumentViewChildren(GapBoxView view) {
        super(view);
    }

}
