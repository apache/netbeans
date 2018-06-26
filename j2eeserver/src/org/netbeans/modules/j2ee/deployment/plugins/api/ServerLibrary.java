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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.impl.ServerLibraryAccessor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryImplementation;

/**
 * The representation of the server library. This means the library server
 * manages not the jars deployed along with the application.
 * 
 * @since 1.68
 * @author Petr Hejl
 */
public final class ServerLibrary {

    static {
        ServerLibraryAccessor.setDefault(new ServerLibraryAccessor() {

            @Override
            public ServerLibrary createServerLibrary(ServerLibraryImplementation impl) {
                return new ServerLibrary(impl);
            }
        });
    }

    private final ServerLibraryImplementation impl;

    private ServerLibrary(@NonNull ServerLibraryImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the specification title of the library. May return
     * <code>null</code>.
     * <p>
     * <div class="nonnormative">
     * For example specification title for JSF would be JavaServer Faces.
     * </div>
     *
     * @return the specification title of the library; may return <code>null</code>
     */
    @CheckForNull
    public String getSpecificationTitle() {
        return impl.getSpecificationTitle();
    }

    /**
     * Returns the specification version of the library. May return
     * <code>null</code>.
     *
     * @return the specification version of the library; may return <code>null</code>
     */
    @CheckForNull
    public Version getSpecificationVersion() {
        return impl.getSpecificationVersion();
    }

    /**
     * Returns the implementation title of the library. May return
     * <code>null</code>.
     * <p>
     * <div class="nonnormative">
     * For example specification title for MyFaces implementation of JSF
     * this would be something like MyFaces.
     * </div>
     *
     * @return the implementation title of the library; may return <code>null</code>
     */
    @CheckForNull
    public String getImplementationTitle() {
        return impl.getImplementationTitle();
    }

    /**
     * Returns the implementation version of the library. May return
     * <code>null</code>.
     *
     * @return the implementation version of the library; may return <code>null</code>
     */
    @CheckForNull
    public Version getImplementationVersion() {
        return impl.getImplementationVersion();
    }

    /**
     * Returns the library name. May return <code>null</code>.
     *
     * @return the library name; may return <code>null</code>
     */
    @CheckForNull
    public String getName() {
        return impl.getName();
    }

    // TODO should we implement equals and hashCode ?
}
