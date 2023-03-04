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
package org.netbeans.modules.web.javascript.debugger.breakpoints;

/**
 * XMLHttpRequest breakpoint.
 * 
 * @author Martin Entlicher
 */
public class XHRBreakpoint extends AbstractBreakpoint {
    
    public static final String PROP_URL_SUBSTRING = "urlSubstring";
    
    private String urlSubstring;
    
    public XHRBreakpoint(String urlSubstring) {
        this.urlSubstring = urlSubstring;
    }

    public String getUrlSubstring() {
        return urlSubstring;
    }

    public void setUrlSubstring(String urlSubstring) {
        String oldUrlSubstring;
        synchronized (this) {
            oldUrlSubstring = this.urlSubstring;
            this.urlSubstring = urlSubstring;
        }
        firePropertyChange(PROP_URL_SUBSTRING, oldUrlSubstring, urlSubstring);
    }
    
}
