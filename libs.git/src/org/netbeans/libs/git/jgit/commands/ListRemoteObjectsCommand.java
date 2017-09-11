/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
