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

package org.netbeans.lib.profiler.ui.swing.renderer;

import java.text.Format;
import org.netbeans.lib.profiler.ui.Formatters;

/**
 *
 * @author Jiri Sedlacek
 */
public class PercentRenderer extends FormattedLabelRenderer implements RelativeRenderer {
    
    private static final String NUL = Formatters.percentFormat().format(0);
    private static final String NAN = NUL.replace('0', '-');  // NOI18N
    
    private long maxValue = 100;
    
    protected boolean renderingDiff;

    
    public PercentRenderer() {
        super(Formatters.percentFormat());
    }
    
    
    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }
    
    public long getMaxValue() {
        return maxValue;
    }
    
    
    public void setDiffMode(boolean diffMode) {
        renderingDiff = diffMode;
    }

    public boolean isDiffMode() {
        return renderingDiff;
    }
    
    
    protected String getValueString(Object value, int row, Format format) {
        if (value == null) return "-"; // NOI18N
        
        StringBuilder s = new StringBuilder();
        s.append("("); // NOI18N
        
        if (maxValue == 0) {
            s.append(NAN);
        } else {
            double number = ((Number)value).doubleValue();
            if (number == 0) {
                if (renderingDiff) s.append('+'); // NOI18N
                s.append(NUL);
            } else {
                number = number / maxValue;
                if (renderingDiff && number > 0) s.append('+'); // NOI18N
                s.append(format.format(number));
            }
        }
        
        s.append(")"); // NOI18N
        return s.toString();
    }
    
}
