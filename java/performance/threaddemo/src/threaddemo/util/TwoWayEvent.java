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

import java.util.EventObject;

/**
 * Event indicating something happened to a two-way support.
 * Always indicates a state change, not just a return to the same state.
 * @author Jesse Glick
 * @see TwoWaySupport
 */
public abstract class TwoWayEvent<DM, UMD, DMD> extends EventObject {

    private TwoWayEvent(TwoWaySupport<DM, UMD, DMD> s) {
        super(s);
        assert s != null;
    }

    /**
     * Get the associated two-way support.
     * @return the support
     */
    public TwoWaySupport<DM, UMD, DMD> getTwoWaySupport() {
        @SuppressWarnings("unchecked")
        TwoWaySupport<DM, UMD, DMD> source = (TwoWaySupport<DM, UMD, DMD>) getSource();
        return source;
    }
    
    /**
     * Event indicating a derived value has been produced.
     */
    public static final class Derived<DM, UMD, DMD> extends TwoWayEvent<DM, UMD, DMD> {
        
        private final DM oldValue, newValue;
        private final DMD derivedDelta;
        private final UMD underlyingDelta;
        
        Derived(TwoWaySupport<DM, UMD, DMD> s, DM oldValue, DM newValue, DMD derivedDelta, UMD underlyingDelta) {
            super(s);
            this.oldValue = oldValue;
            assert newValue != null;
            this.newValue = newValue;
            assert oldValue != null ^ derivedDelta == null;
            this.derivedDelta = derivedDelta;
            this.underlyingDelta = underlyingDelta;
        }

        /**
         * Get the old value of the derived model.
         * @return the old value, or null if it was never calculated
         */
        public DM getOldValue() {
            return oldValue;
        }
        
        /**
         * Get the new value of the derived model.
         * @return the new value
         */
        public DM getNewValue() {
            return newValue;
        }
        
        /**
         * Get the change to the derived model.
         * @return the delta, or null if the old value was null
         */
        public DMD getDerivedDelta() {
            return derivedDelta;
        }
        
        /**
         * Get the change to the underlying model that triggered this derivation.
         * Only applicable in case the derived model had been invalidated and
         * was stale before this derivation.
         * @return the invalidating change to the underlying model, or null if
         *         the derived model is simply being computed for the first time
         */
        public UMD getUnderlyingDelta() {
            return underlyingDelta;
        }
        
        public String toString() {
            return "TwoWayEvent.Derived[" + getTwoWaySupport() + ",oldValue=" + oldValue + ",newValue=" + newValue + ",derivedDelta=" + derivedDelta + ",underlyingDelta=" + underlyingDelta + "]";
        }
        
    }
    
    /**
     * Event indicating a derived model has been invalidated.
     */
    public static final class Invalidated<DM, UMD, DMD> extends TwoWayEvent<DM, UMD, DMD> {
        
        private final DM oldValue;
        private final UMD underlyingDelta;
        
        Invalidated(TwoWaySupport<DM, UMD, DMD> s, DM oldValue, UMD underlyingDelta) {
            super(s);
            assert oldValue != null;
            this.oldValue = oldValue;
            assert underlyingDelta != null;
            this.underlyingDelta = underlyingDelta;
        }
        
        /**
         * Get the old value of the derived model that is now invalid.
         * @return the old value
         */
        public DM getOldValue() {
            return oldValue;
        }
        
        /**
         * Get the change to the underlying model that triggered this invalidation.
         * @return the invalidating change to the underlying model
         */
        public UMD getUnderlyingDelta() {
            return underlyingDelta;
        }
        
        public String toString() {
            return "TwoWayEvent.Invalidated[" + getTwoWaySupport() + ",oldValue=" + oldValue + ",underlyingDelta=" + underlyingDelta + "]";
        }
        
    }
    
    /**
     * Event indicating the derived model was changed and the underlying model recreated.
     */
    public static final class Recreated<DM, UMD, DMD> extends TwoWayEvent<DM, UMD, DMD> {
        
        private final DM oldValue, newValue;
        private final DMD derivedDelta;
        
        Recreated(TwoWaySupport<DM, UMD, DMD> s, DM oldValue, DM newValue, DMD derivedDelta) {
            super(s);
            assert oldValue != null;
            this.oldValue = oldValue;
            assert newValue != null;
            this.newValue = newValue;
            assert derivedDelta != null;
            this.derivedDelta = derivedDelta;
        }
        
        /**
         * Get the old value of the derived model that is now invalid.
         * @return the old value
         */
        public DM getOldValue() {
            return oldValue;
        }
        
        /**
         * Get the new value of the derived model.
         * @return the new value
         */
        public DM getNewValue() {
            return newValue;
        }
        
        /**
         * Get the change to the derived model that should be applied to the underlying
         * model as well.
         * @return the delta to the derived model
         */
        public DMD getDerivedDelta() {
            return derivedDelta;
        }
        
        public String toString() {
            return "TwoWayEvent.Recreated[" + getTwoWaySupport() + ",oldValue=" + oldValue + ",newValue=" + newValue + ",derivedDelta=" + derivedDelta + "]";
        }
        
    }
    
    /**
     * Event indicating changes in the underlying model were clobbered by changes to
     * the derived model.
     */
    public static final class Clobbered<DM, UMD, DMD> extends TwoWayEvent<DM, UMD, DMD> {
        
        private final DM oldValue, newValue;
        private final DMD derivedDelta;
        
        Clobbered(TwoWaySupport<DM, UMD, DMD> s, DM oldValue, DM newValue, DMD derivedDelta) {
            super(s);
            this.oldValue = oldValue;
            assert newValue != null;
            this.newValue = newValue;
            assert derivedDelta != null;
            this.derivedDelta = derivedDelta;
        }
        
        /**
         * Get the old value of the derived model that is now invalid.
         * @return the old value, or null if it was never calculated
         */
        public DM getOldValue() {
            return oldValue;
        }
        
        /**
         * Get the new value of the derived model.
         * @return the new value
         */
        public DM getNewValue() {
            return newValue;
        }
        
        /**
         * Get the change to the derived model that should be applied to the underlying
         * model as well whether it is applicable or not.
         * @return the delta to the derived model
         */
        public DMD getDerivedDelta() {
            return derivedDelta;
        }
        
        public String toString() {
            return "TwoWayEvent.Clobbered[" + getTwoWaySupport() + ",oldValue=" + oldValue + ",newValue=" + newValue + ",derivedDelta=" + derivedDelta + "]";
        }
        
    }

    /**
     * Event indicating the reference to the derived model was garbage collected.
     * This does not apply if the support itself was already collected.
     * Also, only supports overriding {@link TwoWaySupport#createReference} will
     * ever fire this event, since the default implementation creates a strong
     * reference that cannot be collected.
     */
    public static final class Forgotten<DM, UMD, DMD> extends TwoWayEvent<DM, UMD, DMD> {
        
        Forgotten(TwoWaySupport<DM, UMD, DMD> s) {
            super(s);
        }
        
        public String toString() {
            return "TwoWayEvent.Forgotten[" + getTwoWaySupport() + "]";
        }
        
    }
    
    /**
     * Event indicating an attempted derivation failed with an exception.
     * The underlying model is thus considered to be in an inconsistent state.
     */
    public static final class Broken<DM, UMD, DMD> extends TwoWayEvent<DM, UMD, DMD> {
        
        private final DM oldValue;
        private final UMD underlyingDelta;
        
        private final Exception exception;
        
        Broken(TwoWaySupport<DM, UMD, DMD> s, DM oldValue, UMD underlyingDelta, Exception exception) {
            super(s);
            this.oldValue = oldValue;
            this.underlyingDelta = underlyingDelta;
            assert exception != null;
            this.exception = exception;
        }
        
        /**
         * Get the old value of the derived model that is now invalid.
         * @return the old value, or null if it was never calculated
         */
        public DM getOldValue() {
            return oldValue;
        }
        
        /**
         * Get the change to the underlying model that triggered this derivation.
         * Only applicable in case the derived model had been invalidated and
         * was stale before this derivation.
         * @return the invalidating change to the underlying model, or null if
         *         the derived model is simply being computed for the first time
         */
        public UMD getUnderlyingDelta() {
            return underlyingDelta;
        }
        
        /**
         * Get the exception encountered when trying to derive a new model.
         * @return the exception that prevented a new derived model from being created
         */
        public Exception getException() {
            return exception;
        }
        
        public String toString() {
            return "TwoWayEvent.Broken[" + getTwoWaySupport() + ",oldValue=" + oldValue + ",underlyingDelta=" + underlyingDelta + ",exception=" + exception + "]";
        }
        
    }
    
}
