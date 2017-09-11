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

        public void exit(int status) {
            System.exit(status);
        }

        public void saveAll() {
        }
    }
}
