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

package org.netbeans.modules.web.debug.breakpoints;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;

/**
 *
 * @author Martin Grebac
 */
public class JspBreakpointsReader implements Properties.Reader {

    public String [] getSupportedClassNames () {
        return new String[] { JspLineBreakpoint.class.getName () };
    }

    public Object read (String typeID, Properties properties) {

        JspLineBreakpoint b = null;
        if (typeID.equals (JspLineBreakpoint.class.getName ())) {
            String url = properties.getString(JspLineBreakpoint.PROP_URL, null);
            // #110349 - ignore loading of breakpoints which do not have URL
            if (url == null || url.trim().length() == 0) {
                return null;
            }
            b = JspLineBreakpoint.create (
                url,
                properties.getInt(JspLineBreakpoint.PROP_LINE_NUMBER, 1)
            );
            b.setCondition(properties.getString (JspLineBreakpoint.PROP_CONDITION, ""));
            b.setPrintText(properties.getString (JspLineBreakpoint.PROP_PRINT_TEXT, ""));
            b.setGroupName(properties.getString (Breakpoint.PROP_GROUP_NAME, ""));
            b.setSuspend(properties.getInt (JspLineBreakpoint.PROP_SUSPEND, JspLineBreakpoint.SUSPEND_ALL));
            if (properties.getBoolean (JspLineBreakpoint.PROP_ENABLED, true)) {
                b.enable ();
            } else {
                b.disable ();
            }
        }

        return b;
    }
    
    public void write (Object object, Properties properties) {

        if (object instanceof JspLineBreakpoint) {
            JspLineBreakpoint b = (JspLineBreakpoint) object;
            properties.setString (JspLineBreakpoint.PROP_PRINT_TEXT, b.getPrintText ());
            properties.setString (JspLineBreakpoint.PROP_GROUP_NAME, b.getGroupName ());
            properties.setInt (JspLineBreakpoint.PROP_SUSPEND, b.getSuspend ());
            properties.setBoolean (JspLineBreakpoint.PROP_ENABLED, b.isEnabled ());        
            properties.setString (JspLineBreakpoint.PROP_URL, b.getURL ());
            properties.setInt (JspLineBreakpoint.PROP_LINE_NUMBER, b.getLineNumber ());
            properties.setString (JspLineBreakpoint.PROP_CONDITION, b.getCondition ());
        }
        return;
    }
}
