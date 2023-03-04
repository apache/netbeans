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
package org.netbeans.modules.web.webkit.debugging.api.debugger;

import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.APIFactory;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;

/**
 *
 */
public abstract class AbstractObject {
    
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
