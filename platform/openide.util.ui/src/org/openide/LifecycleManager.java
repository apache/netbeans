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

package org.openide;

import org.openide.util.Lookup;

/** Manages major aspects of the NetBeans lifecycle - currently saving all objects and exiting.
 * @author Jesse Glick
 * @since 3.14
 */
public abstract class LifecycleManager {
    /** Subclass constructor. */
    protected LifecycleManager() {
    }

    /**
     * Get the default lifecycle manager.
     * Normally this is found in {@link Lookup#getDefault} but if no instance is
     * found there, a fallback instance is returned which behaves as follows:
     * <ol>
     * <li>{@link #saveAll} does nothing
     * <li>{@link #exit} calls {@link System#exit} with an exit code of 0
     * </ol>
     * This is useful for unit tests and perhaps standalone library usage.
     * @return the default instance (never null)
     */
    public static LifecycleManager getDefault() {
        LifecycleManager lm = Lookup.getDefault().lookup(LifecycleManager.class);

        if (lm == null) {
            lm = new Trivial();
        }

        return lm;
    }

    /** Save all opened objects.
     */
    public abstract void saveAll();

    /** Exit NetBeans.
     * This method will return only if {@link java.lang.System#exit} fails, or if at least one component of the
     * system refuses to exit (because it cannot be properly shut down).
     */
    public abstract void exit();

    /** Exit NetBeans with the given exit code.
     * This method will return only if {@link java.lang.System#exit} fails, or if at least one component of the
     * system refuses to exit (because it cannot be properly shut down). The
     * default implementation calls {@link #exit()} for compatibility reasons,
     * but subclasses are encouraged to provide better implementation.
     * 
     * @param status the exit code of the application
     * @since 8.23
     */
    public void exit(int status) {
        exit();
    }

    /**
     * Request that the application restart immediately after next being shut down.
     * You may want to then call {@link #exit} to go ahead and restart now.
     * @throws UnsupportedOperationException if this request cannot be honored
     * @since org.openide.util 7.25
     */
    public void markForRestart() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /** Fallback instance. */
    private static final class Trivial extends LifecycleManager {
        public Trivial() {
        }

        public void exit() {
            System.exit(0);
        }

        @Override
        public void exit(int status) {
            System.exit(status);
        }

        public void saveAll() {
        }
    }
}
