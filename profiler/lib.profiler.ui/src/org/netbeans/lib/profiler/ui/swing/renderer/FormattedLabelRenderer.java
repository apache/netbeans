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
import java.text.MessageFormat;

/**
 *
 * @author Jiri Sedlacek
 */
public class FormattedLabelRenderer extends LabelRenderer {
    
    private final Format format;
    
    public FormattedLabelRenderer(Format format) {
        this.format = format;
    }
    
    public void setValue(Object value, int row) {
        super.setValue(getValueString(value, row, format), row);
    }
    
    protected String getValueString(Object value, int row, Format format) {
        if (format != null) return formatImpl(format, value);
        else return value == null ? "null" : value.toString(); // NOI18N
    }
    
    protected static String formatImpl(Format format, Object value) {
        if (format instanceof MessageFormat)
            if (!(value instanceof Object[]))
                value = new Object[] { value };
        return format.format(value);
    }
    
}
