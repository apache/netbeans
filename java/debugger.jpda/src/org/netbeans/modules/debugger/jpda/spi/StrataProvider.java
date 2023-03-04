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

package org.netbeans.modules.debugger.jpda.spi;

import java.util.List;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;

/**
 * A provider of strata.
 * Use when a default strata detection is not sufficient.
 * Register the implementation into the debugger session lookup.
 * 
 * @author Martin
 */
public interface StrataProvider {
    
    /**
     * Provide the default stratum of this call stack frame.
     * Do not call {@link CallStackFrameImpl#getDefaultStratum()} or
     * {@link CallStackFrameImpl#getAvailableStrata()} in this method.
     * The {@link CallStackFrameImpl} is populated with what you return here.
     * @param csf the stack frame to find the default strata for.
     * @return the desired default strata, or <code>null</code> to use the default impl.
     */
    String getDefaultStratum(CallStackFrameImpl csf);
    
    /**
     * Provide the list of strata of this call stack frame.
     * Do not call {@link CallStackFrameImpl#getDefaultStratum()} or
     * {@link CallStackFrameImpl#getAvailableStrata()} in this method.
     * The {@link CallStackFrameImpl} is populated with what you return here.
     * @param csf the stack frame to find the available strata for.
     * @return the desired available strata, or <code>null</code> to use the default impl.
     */
    List<String> getAvailableStrata(CallStackFrameImpl csf);
    
    int getStrataLineNumber(CallStackFrameImpl csf, String stratum);
}
