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
package org.netbeans.libs.git.jgit.commands;

import java.net.URISyntaxException;
import java.util.Collection;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.FetchConnection;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
abstract class ListRemoteObjectsCommand extends TransportCommand {
    private Collection<Ref> refs;
    
    public ListRemoteObjectsCommand (Repository repository, GitClassFactory gitFactory, String remoteRepositoryUrl, ProgressMonitor monitor) {
        super(repository, gitFactory, remoteRepositoryUrl, monitor);
    }

    @Override
    protected final void runTransportCommand () throws GitException {
        Transport t = null;
        FetchConnection conn = null;
        try {
            t = openTransport(false);
            conn = t.openFetch();
            refs = conn.getRefs();
        } catch (URISyntaxException ex) {
            throw new GitException(ex.getMessage(), ex);
        } catch (NotSupportedException ex) {
            throw new GitException(ex.getMessage(), ex);
        } catch (TransportException e) {
            URIish uriish = null;
            try {
                uriish = getUriWithUsername(false);
            } catch (URISyntaxException ex) {
                throw new GitException(ex.getMessage(), ex);
            }
            handleException(e, uriish);
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (t != null) {
                t.close();
            }
        }
        processRefs();
    }

    protected abstract void processRefs ();
    
    protected final Collection<Ref> getRefs () {
        return refs;
    }
}
