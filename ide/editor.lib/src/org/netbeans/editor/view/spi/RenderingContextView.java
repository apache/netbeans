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

package org.netbeans.editor.view.spi;

import java.awt.Graphics;
import java.awt.Shape;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * View providing an access to the {@link ViewRenderingContext}
 * in the view hierarchy.
 *
 * <p>
 * Accessing of the <code>ViewRenderingContext</code>
 * from a <code>view</code> must always be done like this:<pre>
 *
 *      RenderingContextView rcView = RenderingContextView.get(view);
 *      if (rcView != null) {
 *          rcView.acquireRenderingContext();
 *          try {
 *              ...
 *          } finally {
 *              rcView.releaseRenderingContext();
 *          }
 *      }
 * </pre>
 * 
 * <p>
 * Only one thread at the time can safely access methods
 * of the <code>RenderingContextView</code>. It does not have
 * to be event dispatch thread.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface RenderingContextView {
    
    /**
     * Acquire an instance of rendering context.
     * After use it must be released by {@link #releaseContext()}.
     * @param v view for which the rendering context is being obtained.
     */
    public ViewRenderingContext acquireRenderingContext(View v);
    
    /**
     * Release rendering context previously acquired by
     * {@link #acquireRenderingContext(javax.swing.text.View)}.
     * @param vrc rendering context to be released.
     */
    public void releaseRenderingContext(ViewRenderingContext vrc);

 }
