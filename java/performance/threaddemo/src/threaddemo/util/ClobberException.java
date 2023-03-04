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

package threaddemo.util;

/**
 * Exception thrown when changes to an original model are about to
 * be clobbered by modifications to a derived model, in lieu of
 * clobbering them.
 * @author Jesse Glick
 * @see TwoWaySupport
 */
public final class ClobberException extends RuntimeException {

    private final TwoWaySupport s;

    private final Object oldValue, derivedDelta;

    ClobberException(TwoWaySupport s, Object oldValue, Object derivedDelta) {
        assert s != null;
        this.s = s;
        this.oldValue = oldValue;
        assert derivedDelta != null;
        this.derivedDelta = derivedDelta;
    }
    
    /**
     * Get the associated two-way support.
     * @return the support
     */
    public TwoWaySupport getTwoWaySupport() {
        return s;
    }
    
    /**
     * Get the old value of the derived model before the attempted clobber.
     * @return the old value, or null if it was never calculated
     */
    public Object getOldValue() {
        return oldValue;
    }
    
    /**
     * Get the attempted change to the derived model.
     * @return the derived delta
     */
    public Object getDerivedDelta() {
        return derivedDelta;
    }
    
}
