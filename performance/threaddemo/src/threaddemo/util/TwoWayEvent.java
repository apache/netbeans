/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
