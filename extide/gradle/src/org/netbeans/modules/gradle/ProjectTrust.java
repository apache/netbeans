/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.gradle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.netbeans.api.project.Project;
import org.openide.util.NbPreferences;

/**
 * This class allows Projects to store and check the trust property of a project.
 * The trust is a unique identifier which is calculated on the project trusted
 * directory and the current NetBeans user home.
 * 
 * @author lkishalmi
 */
public class ProjectTrust {
    private static final Logger LOG = Logger.getLogger(ProjectTrust.class.getName());
    
    private static final String KEY_SALT     = "secret";     //NOI18N
    private static final String NODE_PROJECT = "projects"; //NOI18N
    private static final String NODE_TRUST   = "trust";    //NOI18N

    private static final String HMAC_SHA256  = "HmacSHA256"; //NOI18N

    private static ProjectTrust instance;

    private final Key key;
    final Preferences projectTrust;
    final byte[] salt;
    
    /**
     * Projects that are trusted just "transiently", for a single IDE session.
     */
    private final Set<String> temporaryTrustedIds = new HashSet<>();

    ProjectTrust(Preferences prefs) {
        byte[] buf = prefs.getByteArray(KEY_SALT, null);
        if (buf == null) {
            buf = new byte[16];
            new SecureRandom().nextBytes(buf);
            prefs.putByteArray(KEY_SALT, buf);
        }
        salt = buf;
        projectTrust = prefs.node(NODE_PROJECT);
        key = new SecretKeySpec(salt, HMAC_SHA256);
    }
    
    /**
     * Returns true if the specified project is trusted.
     *
     * @param project of the trust check.
     * @return true if the given project is trusted.
     */
    public boolean isTrusted(Project project) {
        synchronized (this) {
            if (temporaryTrustedIds.contains(getPathId(project))) {
                LOG.log(Level.FINER, "Project {0} temporarily trusted.", project);
                return true;
            }
        }
        return isTrustedPermanently(project);
    }

    /**
     * Returns true if the specified project is trusted <b>permanently</b>.
     * You should be probably using {@link #isTrusted} to avoid duplicate questions
     * during one IDE run.
     *
     * @param project of the trust check.
     * @return true if the given project is trusted.
     */
    public boolean isTrustedPermanently(Project project) {
        String pathId = getPathId(project);
        String projectId = projectTrust.get(pathId, null);
        if (projectId == null) {
            return false;
        }
        boolean ret = false;
        Path trustFile = getProjectTrustFile(project);
        try {
            List<String> trust = Files.readAllLines(trustFile);
            String hash = hmacSha256(fromHex(projectId));
            ret = trust.size() == 1 && trust.iterator().next().equals(hash);
            LOG.log(Level.FINER, "Trust for project {0} is: {1}", new Object[] { project, ret });
        } catch (IOException ex) {
            LOG.log(Level.FINER, "Could not load trust file {0} for projec {1}.", new Object[] { trustFile, project});
        }
        return ret;        
    }
    
    /**
     * Marks the given project trusted, if it was not trusted before. If {@code permanently}
     * is true, the decision will be recorded for further IDE runs.
     * 
     * @param project the project to trust.
     */
    public void trustProject(Project project, boolean permanently) {
        String pathId = getPathId(project);
        synchronized (this) {
            temporaryTrustedIds.add(pathId);
        }
        if (permanently && !isTrustedPermanently(project)) {
            Path trustFile = getProjectTrustFile(project);
            byte[] rnd = new byte[16];
            new SecureRandom().nextBytes(rnd);
            String projectId = toHex(rnd);
            projectTrust.put(pathId, projectId);
            try {
                Files.createDirectories(trustFile.getParent());
                Files.write(trustFile, Collections.singletonList(hmacSha256(rnd)));
            } catch (IOException ex) {}
        }
    }

    /**
     * Marks the given project trusted, if it was not trusted before. The decision
     * will be recorded persistently.
     * 
     * @param project the project to trust.
     */
    public void trustProject(Project project) {
        trustProject(project, true);
    }

    /**
     * Marks the given project not trusted. The decision will be deleted also
     * from the persistent storage and from the temporarily trusted projects.
     * 
     * @param project the project to remove trust from.
     */
    public void distrustProject(Project project) {
        String pathId = getPathId(project);
        projectTrust.remove(pathId);
        synchronized (this) {
            temporaryTrustedIds.remove(pathId);
        }
        Path trustFile = getProjectTrustFile(project);
        if (trustFile != null) {
            try {
                Files.delete(trustFile);
            } catch (IOException ex) {
            }
        }

    }

    public static ProjectTrust getDefault() {
        if (instance == null) {
            Preferences p = NbPreferences.forModule(ProjectTrust.class).node(NODE_TRUST);
            instance = new ProjectTrust(p);
        }
        return instance;
    }

    /**
     * The path which shall be considered as a source of trust for the given
     * project. For Gradle projects it is the root project directory.
     *
     * @param project the project to calculate the source of trust from.
     * @return the Path to the trusted directory of the project
     */
    protected Path getProjectTrustPath(Project project) {
        if (project instanceof NbGradleProjectImpl) {
            return ((NbGradleProjectImpl) project).getGradleFiles().getRootDir().toPath();
        }
        throw new IllegalArgumentException("Project shall be an NbGradleProjectImpl instance."); //NOI18N
    }

    /**
     * The directory where to store the project trust files. It is preferred to
     * return a directory which is most likely end up on the ignore list of the
     * used version control system.
     *
     * @param project the project to return the trust file path for.
     * @return the Path of the directory to place the trust files in.
     */
    protected Path getProjectTrustFilePath(Project project) {
        if (project instanceof NbGradleProjectImpl) {
            Path root = getProjectTrustPath(project);
            return root == null ? null : root.resolve(".gradle/nb-cache/trust"); //NOI18N
        }
        throw new IllegalArgumentException("Project shall be an NbGradleProjectImpl instance."); //NOI18N
    }

    /**
     * Returns the name of the file where the project trust shall be stored. It
     * is the {@code <trust file path>/<unique path id>}. It ensures that different
     * NetBeans installations won't clash on a same file.
     *
     * @param project the project to calculate the trust file for.
     * @return the Path to the trust file.
     */
    Path getProjectTrustFile(Project project) {
        String pathId = getPathId(project);
        Path trustFilePath = getProjectTrustFilePath(project);
        return trustFilePath.resolve(pathId);
    }

    /**
     * Generate a unique id of the Project trusted path. The returned id is
     * unique as of the given project and the NetBeans user home, so the same
     * project with different NetBeans installation would result a different id.
     *
     * @param project the project to get the trusted path from.
     * @return the unique ID of the project trust path.
     */
    String getPathId(Project project) {
        Path path = getProjectTrustPath(project);
        path = path.normalize().toAbsolutePath();
        return hmacSha256(path.toString().getBytes(StandardCharsets.UTF_8));
    }

    String hmacSha256(byte[] buf) {
        byte[] out;
        try {
            Mac hmac = Mac.getInstance(HMAC_SHA256);
            hmac.init(key);
            out = hmac.doFinal(buf);
            return toHex(out);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            // Shall not happen on JVM-s fulfilling the specs.
            // This throw line is not expected to be called, but let hmac be final
            throw new IllegalArgumentException("JDK has issues with HMAC_SHA256: " + ex.getMessage());
        }
    }

    static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] ret = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            ret[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));

        }
        return ret;
    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8); //NOI18N
    static String toHex(byte[] b) {
        byte[] hexChars = new byte[b.length * 2];
        for (int j = 0; j < b.length; j++) {
            int v = b[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

}
