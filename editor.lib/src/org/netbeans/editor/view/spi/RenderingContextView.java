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
