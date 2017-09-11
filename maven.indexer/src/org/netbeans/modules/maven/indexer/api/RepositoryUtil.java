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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.indexer.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.codehaus.plexus.util.IOUtil;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Anuradha G
 */
public final class RepositoryUtil {

    private RepositoryUtil() {
    }

    public static @NonNull Artifact createArtifact(@NonNull NBVersionInfo info) {
        return createArtifact(info, null);
    }

    public static @NonNull Artifact createJavadocArtifact(@NonNull NBVersionInfo info) {
        return createArtifact(info, "javadoc"); //NOI18N
    }

    private static @NonNull Artifact createArtifact(@NonNull NBVersionInfo info, @NullAllowed String classifier) {
        Artifact art;
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        if (info.getClassifier() != null || classifier != null) {
            art = online.createArtifactWithClassifier(info.getGroupId(),
                    info.getArtifactId(),
                    info.getVersion(),
                    info.getType() != null ? info.getType() : "jar", //NOI18N
                    classifier == null ? info.getClassifier() : classifier);
        } else {
            art = online.createArtifact(info.getGroupId(),
                    info.getArtifactId(),
                    info.getVersion(),
                    null,
                    info.getType() != null ? info.getType() : "jar"); //NOI18N
        }
        ArtifactRepository repo = online.getLocalRepository();
        String localPath = repo.pathOf(art);
        art.setFile(FileUtil.normalizeFile(new File(online.getLocalRepositoryFile(), localPath)));

        return art;
    }
    
    /**
     * Tries to download an artifact.
     * @param info a version of an artifact
     * @return the file in the local repository (might not exist if download failed)
     * @throws AbstractArtifactResolutionException currently never?
     * @since 1.17
     */
    public static File downloadArtifact(NBVersionInfo info) throws Exception {
        Artifact a = createArtifact(info);
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        List<ArtifactRepository> remotes;
        RepositoryInfo repo = RepositoryPreferences.getInstance().getRepositoryInfoById(info.getRepoId());
        if (repo != null && repo.isRemoteDownloadable()) {
            remotes = Collections.singletonList(online.createRemoteRepository(repo.getRepositoryUrl(), repo.getId()));
        } else {
            remotes = Collections.singletonList(online.createRemoteRepository(RepositorySystem.DEFAULT_REMOTE_REPO_URL, RepositorySystem.DEFAULT_REMOTE_REPO_ID));
        }
        online.resolve(a, remotes, online.getLocalRepository());
        return a.getFile();
    }
    
    public static String calculateSHA1Checksum(File file) throws IOException {
        byte[] buffer = readFile(file);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException x) {
            throw new IOException(x);
        }
        digest.update(buffer);
        return String.format("%040x", new BigInteger(1, digest.digest()));
    }

    private static byte[] readFile(File file) throws IOException {

        InputStream is = null; 
        byte[] bytes = new byte[(int) file.length()];
        try {
            is = new FileInputStream(file);


        int offset = 0;
        int numRead = 0;

        while (offset < bytes.length &&
                (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {

            offset += numRead;
        }
        } finally {
            IOUtil.close(is);
        }

        return bytes;
    }

}
