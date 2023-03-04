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
