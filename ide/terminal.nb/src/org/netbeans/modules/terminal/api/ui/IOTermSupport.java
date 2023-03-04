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
package org.netbeans.modules.terminal.api.ui;

import org.netbeans.lib.terminalemulator.Term;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 *
 * @author masha
 */
public final class IOTermSupport {
      /**
     * Return the underlying Term associatd with this IO.
     * @param io IO to operate on.
     * @return underlying Term associatd with io. null if no such Term.
     */
    public static Term term(InputOutput io) {
	IOTerm iot = find(io);
	if (iot != null)
	    return iot.term();
	else
	    return null;
    }  
    
    private static IOTerm find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOTerm.class);
        }
        return null;
    }

}
