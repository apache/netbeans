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
/*
 * GuaranteedValue.java
 *
 * Created on March 13, 2004, 7:43 PM
 */

package org.netbeans.swing.plaf.util;

import javax.swing.*;
import java.awt.*;

/** A simple mechanism for guaranteeing the presence of a value in UIDefaults.
 * Some look and feels (GTK/Synth) don't necessarily provide these colors,
 * resulting in null pointer exceptions.  Since we want to make it easy to
 * write maintainable components, it is preferable to centralize guaranteeing
 * the value here, rather than have the codebase littered with null tests and
 * fallbacks for null colors.
 *
 * It will either take on the existing value if present, or used the passed 
 * value if not present.  If passed an array of UIManager keys, it will try
 * them in order, and use the first value that returns non-null from 
 * <code>UIManager.get()</code>.
 *
 * Usage:
 * <pre>
 * UIManager.put (new GuaranteedValue("controlShadow", Color.LIGHT_GRAY));
 *   or
 * UIManager.put (new GuaranteedValue(new String[] {"Tree.foreground", 
 *    List.foreground", "textText"}, Color.BLACK));
 * </pre>
 *
 * It can also be used to ensure a value matches another value, with a fallback
 * if it is not present: 
 * <pre>
 * UIManager.put("TextArea.font", new GuaranteedValue ("Label.font", new Font("Dialog", Font.PLAIN, 11)))
 *
 * @author  Tim Boudreau
 */
public class GuaranteedValue implements UIDefaults.LazyValue {
    private Object value;
    /** Creates a new instance of GuaranteedValue */
    public GuaranteedValue(String key, Object fallback) {
        //Be fail fast, so no random exceptions from random components later
        if (key == null || fallback == null) {
            throw new NullPointerException ("Null parameters: " + key + ',' + fallback);
        }
        
        value = UIManager.get(key);
        if (value == null) {
            value = fallback;
        }
    }
    
    public GuaranteedValue(String[] keys, Object fallback) {
        //Be fail fast, so no random exceptions from random components later
        if (keys == null || fallback == null) {
            throw new NullPointerException ("Null parameters: " + keys + ',' + fallback);
        }
        for (int i=0; i < keys.length; i++) {
            value = UIManager.get(keys[i]);
            if (value != null) {
                break;
            }
        }
        if (value == null) {
            value = fallback;
        }
    }
    
    public Object createValue(UIDefaults table) {
        return value;
    }
    
    /** Convenience getter of the value as a color - returns null if this
     * instance was used for some other type */
    public Color getColor() {
        Object o = createValue(null);
        if (o instanceof Color) {
            return (Color) o;
        } else {
            return null;
        }
    }

    public Font getFont() {
        Object o = createValue(null);
        if (o instanceof Font) {
            return (Font) o;
        } else {
            return null;
        }
    }
}
