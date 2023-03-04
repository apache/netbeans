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
