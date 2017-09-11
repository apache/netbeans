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
package org.netbeans.modules.web.webkit.debugging.api.debugger;

import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.APIFactory;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;

/**
 *
 */
abstract public class AbstractObject {
    
    private JSONObject object;
    private WebKitDebugging webkit;

    static {
        APIFactory.Accessor2.DEFAULT = new APIFactory.Accessor2() {

            @Override
            public PropertyDescriptor createPropertyDescriptor(JSONObject o, WebKitDebugging webkit) {
                return  new PropertyDescriptor(o, webkit);
            }

            @Override
            public Script createScript(JSONObject o, WebKitDebugging webkit) {
                return new Script(o, webkit);
            }

            @Override
            public Breakpoint createBreakpoint(JSONObject o, WebKitDebugging webkit) {
                return new Breakpoint(o, webkit);
            }

            @Override
            public CallFrame createCallFrame(JSONObject o, WebKitDebugging webkit, TransportHelper transport) {
                return new CallFrame(o, webkit, transport);
            }

            @Override
            public void breakpointResolved(Breakpoint bp, JSONObject location) {
                bp.notifyResolved(location);
            }
        };
    }
    
    AbstractObject(JSONObject object, WebKitDebugging webkit) {
        this.object = object;
        this.webkit = webkit;
        assert object != null;
        assert webkit != null;
    }
    
    protected JSONObject getObject() {
        return object;
    }

    protected WebKitDebugging getWebkit() {
        return webkit;
    }
    
    @Override
    public String toString() {
        return object.toJSONString();
    }
    
}
