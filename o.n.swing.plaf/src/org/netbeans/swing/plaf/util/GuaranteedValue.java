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
