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

import javax.swing.text.AbstractDocument;
import javax.swing.text.View;
import org.netbeans.editor.view.spi.FlyView;
import org.netbeans.editor.view.spi.LockView;
import org.netbeans.editor.view.spi.ViewLayoutState;

/**
 * Extension of {@link SimpleViewLayoutState}
 * that thoroughly handles minimum and maximum spans.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class DefaultViewLayoutState extends SimpleViewLayoutState {

    private float layoutMinorAxisMinimumSpan;

    private float layoutMinorAxisMaximumSpan;


    public DefaultViewLayoutState(View v) {
        super(v);
    }

    protected boolean minorAxisUpdateLayout(int minorAxis) {
        View view = getView();
        boolean minorAxisPreferenceChanged = false;
        float val;
        
        val = view.getMaximumSpan(minorAxis);
        if (val != getLayoutMinorAxisMaximumSpan()) {
            setLayoutMinorAxisMaximumSpan(val);
            minorAxisPreferenceChanged = true;
        }
        
        val = view.getMinimumSpan(minorAxis);
        if (val != getLayoutMinorAxisMinimumSpan()) {
            setLayoutMinorAxisMinimumSpan(val);
            minorAxisPreferenceChanged = true;
        }
        
        return minorAxisPreferenceChanged;
    }
    
    public float getLayoutMinorAxisMaximumSpan() {
        return layoutMinorAxisMaximumSpan;
    }
    
    public void setLayoutMinorAxisMaximumSpan(float layoutMinorAxisMaximumSpan) {
        this.layoutMinorAxisMaximumSpan = layoutMinorAxisMaximumSpan;
    }

    public float getLayoutMinorAxisMinimumSpan() {
        return layoutMinorAxisMinimumSpan;
    }
    
    public void setLayoutMinorAxisMinimumSpan(float layoutMinorAxisMinimumSpan) {
        this.layoutMinorAxisMinimumSpan = layoutMinorAxisMinimumSpan;
    }

}
