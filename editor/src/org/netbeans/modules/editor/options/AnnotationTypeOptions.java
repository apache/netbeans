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

package org.netbeans.modules.editor.options;

import org.netbeans.editor.AnnotationType;
import java.awt.Color;

/** Customizable bean which delegates to AnnotationType
 *
 * @author David Konecny
 * @since 07/2001
 */
public class AnnotationTypeOptions {

    private AnnotationType delegate;

    public AnnotationTypeOptions(AnnotationType delegate) {
        this.delegate = delegate;
    }

    public boolean isVisible() {
        return delegate.isVisible();
    }

    public boolean isWholeLine() {
        return delegate.isWholeLine();
    }
    
    public Color getHighlightColor() {
        return delegate.getHighlight();
    }

    public void setHighlightColor(Color col) {
        delegate.setHighlight(col);
    }
    
    public boolean isUseHighlightColor() {
        return delegate.isUseHighlightColor();
    }

    public void setUseHighlightColor(boolean use) {
        delegate.setUseHighlightColor(use);
    }

    public Color getForegroundColor() {
        return delegate.getForegroundColor();
    }

    public void setForegroundColor(Color col) {
        delegate.setForegroundColor(col);
    }

    public boolean isInheritForegroundColor() {
        return delegate.isInheritForegroundColor();
    }

    public void setInheritForegroundColor(boolean inherit) {
        delegate.setInheritForegroundColor(inherit);
    }

    public Color getWaveUnderlineColor() {
        return delegate.getWaveUnderlineColor();
    }

    public void setWaveUnderlineColor(Color col) {
        delegate.setWaveUnderlineColor(col);
    }

    public boolean isUseWaveUnderlineColor() {
        return delegate.isUseWaveUnderlineColor();
    }

    public void setUseWaveUnderlineColor(boolean use) {
        delegate.setUseWaveUnderlineColor(use);
    }

}
