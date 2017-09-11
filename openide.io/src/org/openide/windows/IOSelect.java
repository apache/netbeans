/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.openide.windows;

import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Capability of an InputOutput of finer grained selection of a component.
 * <p>
 * InputOutput.select() does too much.
 * @author ivan
 * @since 1.23
 */
public abstract class IOSelect {

    /**
     * Additional operations to perform when issuing {@link IOSelect#select}.
     * @author ivan
     */
    public static enum AdditionalOperation {
	/**
	 * Additionally issue open() on the TopComponent containing the InputOutput.
	 */
	OPEN,

	/**
	 * Additionally issue requestVisible() on the TopComponent containing the InputOutput.
	 */
	REQUEST_VISIBLE,

	/**
	 * Additionally issue requestActive() on the TopComponent containing the InputOutput.
	 */
	REQUEST_ACTIVE
    }

    private static IOSelect find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOSelect.class);
        }
        return null;
    }

    /**
     * With an empty 'extraOps' simply selects this io
     * without involving it's containing TopComponent.
     * <p>
     * For example:
     * <pre>
     * if (IOSelect.isSupported(io) {
     *     IOSelect.select(io, EnumSet.noneOf(IOSelect.AdditionalOperation.class));
     * }
     * </pre>
     * <p>
     * If this capability is not supported then regular InputOutput.select()
     * will be called.
     * @param io InputOutput to operate on.
     * @param extraOps Additional operations to apply to the containing
     * TopComponent.
     */
    public static void select(InputOutput io, Set<AdditionalOperation> extraOps) {
	Parameters.notNull("extraOps", extraOps);	// NOI18N
	IOSelect ios = find(io);
	if (ios != null)
	    ios.select(extraOps);
	else
	    io.select();	// fallback
    }

    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
        return find(io) != null;
    }

    /**
     * With an empty 'extraOps' simply selects this io
     * without involving it's containing TopComponent.
     * @param extraOps Additional operations to apply to the containing
     * TopComponent.
     */
    abstract protected void select(Set<AdditionalOperation> extraOps);
}
