/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.debugger.common2.values;

import java.awt.Color;

public class VariableValue {
    public String text;
    public boolean bold;

    public VariableValue(String text, boolean bold) {
	this.text = text;
	this.bold = bold;
    }

    @Override
    public String toString() {
	if (!bold)
	    return text;
	else {
	    return toHTML(text, true, false, null);
	}
    }
    
    public static String bold(String text) {
        return toHTML(text, true, false, null);
    }

    public static String toHTML(String text, boolean bold,  boolean italics, Color color) {
        if (text == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html>"); // NOI18N
        if (bold) {
            sb.append("<b>"); // NOI18N
        }
        if (italics) {
            sb.append("<i>"); // NOI18N
        }
        if (color != null) {
            sb.append("<font color="); // NOI18N
            sb.append(Integer.toHexString ((color.getRGB () & 0xffffff)));
            sb.append('>'); // NOI18N
        }
        text = text.replaceAll("&", "&amp;"); // NOI18N
        text = text.replaceAll("<", "&lt;"); // NOI18N
        text = text.replaceAll(">", "&gt;"); // NOI18N
        sb.append(text);
        if (color != null) {
            sb.append("</font>"); // NOI18N
        }
        if (italics) {
            sb.append("</i>"); // NOI18N
        }
        if (bold) {
            sb.append("</b>"); // NOI18N
        }
        sb.append("</html>"); // NOI18N
        return sb.toString();
    }
}
