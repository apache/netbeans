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
package org.netbeans.spi.debugger.jpda;

import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;

/**
 * Implement this interface to adjust breakpoint properties of newly created
 * breakpoints according to the strata.
 * <p>
 * By default, breakpoints created via static methods like
 * {@link LineBreakpoint#create(String, int)}, etc. are targeted to the default
 * Java stratum. When the breakpoint's location is in a different language,
 * adjustments of it's properties might be necessary.
 * <p>
 * When an implementation of this interface is
 * {@link org.openide.util.lookup.ServiceProvider registered into the lookup},
 * it is called with newly created JPDA breakpoint. Depending on its current
 * properties, the implementation can adapt the breakpoint to the language
 * stratum as necessary.
 * <p>
 * Currently applies to creation of {@link LineBreakpoint} with non-empty URL only.
 * It can be extended to other breakpoint types in the future.
 *
 * @author Martin Entlicher
 * @since 3.27
 */
public interface BreakpointStratifier {

    /**
     * Adjust the breakpoint properties according to its specific language stratum.
     * @param breakpoint the breakpoint to adjust.
     */
    void stratify(JPDABreakpoint breakpoint);
}
