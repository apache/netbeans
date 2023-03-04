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

package org.netbeans.modules.debugger.jpda;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Jancura
 */
public class JSR45DebuggerEngineProvider extends DebuggerEngineProvider {

    private final String language;
    private final RequestProcessor rp;
    private DebuggerEngine.Destructor desctuctor;

    JSR45DebuggerEngineProvider (String language, RequestProcessor rp) {
        this.language = language;
        this.rp = rp;
    }

    public String[] getLanguages () {
        return new String[] {language};
    }

    public String getEngineTypeID () {
        return JPDADebugger.SESSION_ID + "/" + language;
    }

    public Object[] getServices () {
        return new Object[] { rp };
    }

    public void setDestructor (DebuggerEngine.Destructor desctuctor) {
        this.desctuctor = desctuctor;
    }

    public DebuggerEngine.Destructor getDesctuctor() {
        return desctuctor;
    }
}

