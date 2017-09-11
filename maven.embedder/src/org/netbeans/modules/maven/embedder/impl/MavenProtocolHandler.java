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

package org.netbeans.modules.maven.embedder.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.openide.util.URLStreamHandlerRegistration;
import org.openide.util.BaseUtilities;

/**
 * Resolves URLs from a Maven repository using the online embedder.
 * <dl>
 * <dt>{@code m2:/org.grp:art:1.0:jar}
 * <dd>simple JAR artifact from Central
 * <dt>{@code m2:/org.grp:art:1.0:jar:javadoc}
 * <dd>Javadoc secondary artifact
 * </dl>
 * Currently only resolves artifacts from Central.
 * Could put repo ID in host field, but would require this to be moved to maven.indexer to use RepositoryPreferences.
 */
@URLStreamHandlerRegistration(protocol="m2")
public class MavenProtocolHandler extends URLStreamHandler {

    protected @Override URLConnection openConnection(URL u) throws IOException {
        String path = u.getPath();
        if (!path.startsWith("/")) {
            throw new IOException(path);
        }
        String stuff = path.substring(1);
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        Artifact a;
        String[] pieces = stuff.split(":");
        if (pieces.length == 4) {
            a = online.createArtifact(pieces[0], pieces[1], pieces[2], pieces[3]);
        } else if (pieces.length == 5) {
            a = online.createArtifactWithClassifier(pieces[0], pieces[1], pieces[2], pieces[3], pieces[4]);
        } else {
            throw new IOException(stuff);
        }
        try {
            online.resolve(a, Collections.<ArtifactRepository>singletonList(online.createRemoteRepository(RepositorySystem.DEFAULT_REMOTE_REPO_URL, RepositorySystem.DEFAULT_REMOTE_REPO_ID)), online.getLocalRepository());
        } catch (Exception x) {
            throw new IOException(stuff + ": " + x, x);
        }
        File f = a.getFile();
        if (!f.isFile()) {
            throw new IOException("failed to download " + stuff);
        }
        Logger.getLogger(MavenProtocolHandler.class.getName()).log(Level.FINE, "resolved {0} -> {1}", new Object[] {stuff, f});
        return BaseUtilities.toURI(f).toURL().openConnection();
    }

}
