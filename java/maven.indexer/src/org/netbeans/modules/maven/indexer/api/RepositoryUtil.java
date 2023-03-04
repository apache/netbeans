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

        byte[] bytes = new byte[(int) file.length()];
        try (InputStream is = new FileInputStream(file)) {

            int offset = 0;
            int numRead = 0;

            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) != -1) {
                offset += numRead;
            }
        }

        return bytes;
    }

}
