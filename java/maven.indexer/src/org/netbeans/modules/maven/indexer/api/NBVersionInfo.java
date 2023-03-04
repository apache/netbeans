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
 * Contributor(s): thenauradha@netbeans.org
 */
package org.netbeans.modules.maven.indexer.api;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 *
 * @author Anuradha G
 */
public final class NBVersionInfo implements Comparable<NBVersionInfo> {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final ComparableVersion comparableVersion;
    private final String type;
    private final String packaging;
    private final String projectName;
    private final String classifier;
    private final String projectDescription;
    private final String repoId;
//    private String sha;
    private long lastModified;
    private long size;
    private float luceneScore = 0f;

    //-----
    private boolean sourcesExists;
    private boolean javadocExists;
    private boolean signatureExists;

    public NBVersionInfo(String repoId,String groupId, String artifactId, String version,
            String type, String packaging, String projectName,String desc,String classifier) {
        this.repoId = repoId;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.packaging = packaging;
        this.projectName = projectName;
        this.projectDescription = desc;
        this.classifier = classifier;
        if (version != null) {
            if (version.matches("RELEASE\\d+(-.+)?")) { // NOI18N
                // Maven considers RELEASE671 to be newer than RELEASE69. Hack up the version here.
                comparableVersion = new ComparableVersion(version.replaceAll("(\\d)", ".$1")); // NOI18N
            } else {
                comparableVersion = new ComparableVersion(version);
            }
        } else {
            comparableVersion = null;
        }
    }

    public String getRepoId() {
        return repoId;
    }

    public boolean isJavadocExists() {
        return javadocExists;
    }

    public void setJavadocExists(boolean javadocExists) {
        this.javadocExists = javadocExists;
    }

    public boolean isSignatureExists() {
        return signatureExists;
    }

    public void setSignatureExists(boolean signatureExists) {
        this.signatureExists = signatureExists;
    }

    public boolean isSourcesExists() {
        return sourcesExists;
    }

    public void setSourcesExists(boolean sourcesExists) {
        this.sourcesExists = sourcesExists;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getPackaging() {
        return packaging;
    }

    public @CheckForNull String getProjectName() {
        return projectName;
    }

    public @CheckForNull String getProjectDescription() {
        return projectDescription;
    }

    public String getClassifier() {
        return classifier;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

//    public String getSha() {
//        return sha;
//    }
//
//    public void setSha(String sha) {
//        this.sha = sha;
//    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version + ":" + repoId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.groupId != null ? this.groupId.hashCode() : 0);
        hash = 97 * hash + (this.artifactId != null ? this.artifactId.hashCode() : 0);
        hash = 97 * hash + (this.comparableVersion != null ? this.comparableVersion.hashCode() : 0);
        hash = 97 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 97 * hash + (this.classifier != null ? this.classifier.hashCode() : 0);
        hash = 97 * hash + (this.repoId != null ? this.repoId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NBVersionInfo other = (NBVersionInfo) obj;
        if ((this.groupId == null) ? (other.groupId != null) : !this.groupId.equals(other.groupId)) {
            return false;
        }
        if ((this.artifactId == null) ? (other.artifactId != null) : !this.artifactId.equals(other.artifactId)) {
            return false;
        }
        if ((this.comparableVersion == null) ? (other.comparableVersion != null) : !this.comparableVersion.equals(other.comparableVersion)) {
            return false;
        }
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        if ((this.classifier == null) ? (other.classifier != null) : !this.classifier.equals(other.classifier)) {
            return false;
        }
        if ((this.repoId == null) ? (other.repoId != null) : !this.repoId.equals(other.repoId)) {
            return false;
        }
        return true;
    }

     public @Override int compareTo(NBVersionInfo o) {
         return compareTo(o, true);
     }
     /**
      * attempt to compare 2 instance without taking the repository in into account (so 2 instances are equals if the only difference is what repository they came from)
      * @param o
      * @return 
      * @since 2.30
      */
     public int compareToWithoutRepoId(NBVersionInfo o) {
         return compareTo(o, false);
     }
    
    private int compareTo(NBVersionInfo o, boolean includeRepo) {
//        int c = Float.compare(luceneScore, o.luceneScore);
//        if (c != 0) {
//            return c;
//        }
        int c = groupId.compareTo(o.groupId);
        if (c != 0) {
            return c;
        }
        c = artifactId.compareTo(o.artifactId);
        if (c != 0) {
            return c;
        }
        c = comparableVersion.compareTo(o.comparableVersion);
        if (c != 0) {
            return -c;
        }
        return extrakey(includeRepo).compareTo(o.extrakey(includeRepo));// show e.g. jar vs. nbm artifacts in some predictable order
    }
    
    private String extrakey(boolean repo) {
        return classifier + type + (repo ? repoId : "");
    }
    
    public float getLuceneScore() {
        return luceneScore;
    }

    public void setLuceneScore(float luceneScore) {
        this.luceneScore = luceneScore;
    }
}
