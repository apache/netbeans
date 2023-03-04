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
import javax.swing.SwingConstants;
import org.netbeans.lib.profiler.ui.Formatters;

/**
 *
 * @author Jiri Sedlacek
 */
public class NumberRenderer extends FormattedLabelRenderer implements RelativeRenderer {
    
    private final Format outputFormat;
    
    protected boolean renderingDiff;
    
    public NumberRenderer() {
        this(null);
    }
    
    public NumberRenderer(Format outputFormat) {
        super(Formatters.numberFormat());
        
        this.outputFormat = outputFormat;
        
        setHorizontalAlignment(SwingConstants.TRAILING);
    }
    
    public void setDiffMode(boolean diffMode) {
        renderingDiff = diffMode;
    }

    public boolean isDiffMode() {
        return renderingDiff;
    }
    
    protected String getValueString(Object value, int row, Format format) {
        if (value == null) return "-"; // NOI18N
        String s = super.getValueString(value, row, format);
        s = outputFormat == null ? s : formatImpl(outputFormat, s);
        if (renderingDiff && value instanceof Number)
            if (((Number)value).doubleValue() >= 0) s = '+' + s; // NOI18N
        return s;
    }
    
}
