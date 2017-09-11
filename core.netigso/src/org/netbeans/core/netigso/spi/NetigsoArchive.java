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

package org.netbeans.core.netigso.spi;

import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.netbeans.ArchiveResources;
import org.netbeans.core.netigso.Netigso;
import org.netbeans.core.netigso.NetigsoArchiveFactory;

/** Netigso's accessor to resource cache. Can be obtained from framework
 * configuration properties under the key "netigso.archive". For each
 * bundle then use {@link #forBundle(long, org.netbeans.core.netigso.spi.BundleContent)}
 * method to obtain own copy of the archive. Your bundle needs to have
 * an associated {@link BundleContent} implementation. Then you can read
 * cached content of your bundles via {@link #fromArchive(java.lang.String)}.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 1.3
 */
public final class NetigsoArchive {
    private final Netigso netigso;
    private final long bundleId;
    private final ArchiveResources content;

    NetigsoArchive(Netigso n, long id, final BundleContent content) {
        this.netigso = n;
        this.bundleId = id;
        this.content = new ArchiveResources() {
            @Override
            public byte[] resource(String name) throws IOException {
                return content == null ? null : content.resource(name);
            }

            @Override
            public String getIdentifier() {
                return "netigso://" + bundleId + "!/"; // NOI18N
            }
        };
    }

    /** Creates a clone of the archive for given bundle.
     *
     * @param bundleId identification of the bundle
     * @param content implementation that can read from the bundle
     * @return archive instance
     */
    public NetigsoArchive forBundle(long bundleId, BundleContent content) {
        return new NetigsoArchive(netigso, bundleId, content);
    }

    /** Checks whether the given resource is in the archive cache. If so,
     * returns it. If not, the asks the {@link BundleContent} associated with
     * this archive to deliver it. Later, during system execution this resource
     * is stored into the global archive for use during subsequent restart.
     *
     * @param resource name of the resource
     * @return the content of the resource (if it exists) or null
     * @throws IOException signals I/O error
     */
    public byte[] fromArchive(String resource) throws IOException {
        return netigso.fromArchive(bundleId, resource, content);
    }
    
    /** Checks whether the archive should be used or not. During first
     * start the Netigso system iterates through all bundle entries to 
     * record so-called <em>covered packages</em>. During this iteration
     * one should not try to use the archive - this method returns <code>false</code>.
     * 
     * @return true, if it is safe to use the {@link #fromArchive(java.lang.String)} method
     * @since 1.14
     */
    public boolean isActive() {
        return netigso.isArchiveActive();
    }
    
    /** Gives OSGi containers that support bytecode patching a chance to
     * call into NetBeans internal patching system based on 
     * {@link Instrumentation}. 
     * 
     * @param l class loader loading the class
     * @param className the name of the class to define
     * @param pd its protection domain
     * @param arr bytecode (must not be modified)
     * @return the same or alternative bytecode to use for the defined class
     * @since 1.25
     */
    public final byte[] patchByteCode(ClassLoader l, 
        String className, 
        ProtectionDomain pd, 
        byte[] arr
    ) {
        return netigso.patchBC(l, className, pd, arr);
    }

    static {
        NetigsoArchiveFactory f = new NetigsoArchiveFactory() {
            @Override
            protected NetigsoArchive create(Netigso n) {
                return new NetigsoArchive(n, 0, null);
            }
        };
    }
}
