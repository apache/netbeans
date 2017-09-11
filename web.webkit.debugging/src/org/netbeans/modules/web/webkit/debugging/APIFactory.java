/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.webkit.debugging;

import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.AbstractObject;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.PropertyDescriptor;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Script;

/**
 *
 */
public final class APIFactory {
    
    private APIFactory() {
    }
    
    /**
     * Creates the API representation of the provided SPI instance.
     * 
     * @param impl the SPI instance
     * @return the API server instance representation
     */
    public static WebKitDebugging createWebKitDebugging(TransportHelper transport) {
        return Accessor.DEFAULT.createWebKitDebugging(transport);
    }

    public static PropertyDescriptor createPropertyDescriptor(JSONObject property, WebKitDebugging webkit) {
        return Accessor2.DEFAULT.createPropertyDescriptor(property, webkit);
    }
    
    public static Script createScript(JSONObject property, WebKitDebugging webkit) {
        return Accessor2.DEFAULT.createScript(property, webkit);
    }
    
    public static Breakpoint createBreakpoint(JSONObject property, WebKitDebugging webkit) {
        return Accessor2.DEFAULT.createBreakpoint(property, webkit);
    }
    
    public static void breakpointResolved(Breakpoint bp, JSONObject location) {
        Accessor2.DEFAULT.breakpointResolved(bp, location);
    }
    
    public static CallFrame createCallFrame(JSONObject property, WebKitDebugging webkit, TransportHelper transport) {
        return Accessor2.DEFAULT.createCallFrame(property, webkit, transport);
    }
    
    /**
     * The accessor pattern class.
     */
    public abstract static class Accessor {

        /** The default accessor. */
        public static Accessor DEFAULT;

        static {
            // invokes static initializer of WebKitDebugging.class
            // that will assign value to the DEFAULT field above
            Class c = WebKitDebugging.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }
        }


        public abstract WebKitDebugging createWebKitDebugging(TransportHelper transport);

    }

    public abstract static class Accessor2 {

        /** The default accessor. */
        public static Accessor2 DEFAULT;

        static {
            // invokes static initializer of AbstractObject.class
            // that will assign value to the DEFAULT field above
            Class c = AbstractObject.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }
        }


        public abstract PropertyDescriptor createPropertyDescriptor(JSONObject property, WebKitDebugging webkit);
        public abstract Script createScript(JSONObject property, WebKitDebugging webkit);
        public abstract Breakpoint createBreakpoint(JSONObject property, WebKitDebugging webkit);
        public abstract void breakpointResolved(Breakpoint bp, JSONObject location);
        public abstract CallFrame createCallFrame(JSONObject property, WebKitDebugging webkit, TransportHelper transport);

    }
    
}
