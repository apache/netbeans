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
package org.netbeans.modules.maven.indexer;

import org.apache.lucene.document.Document;
import org.apache.maven.index.ArtifactAvailability;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;

/**
 * Creates compact remote repository indices by discarding less important fields,
 * or fields which can be easily substituted by online search services.
 * 
 * @author mbien
 */
final class MinimalArtifactInfoRemoteIndexCreator extends MinimalArtifactInfoIndexCreator {
    
    private static final char FS = ArtifactInfo.FS.charAt(0);
    
    static {
        if (ArtifactInfo.FS.length() != 1) {
            throw new IllegalStateException("field format changed");
        }
    }

    @Override
    public void updateDocument(ArtifactInfo ai, Document doc) {
        String info = ArtifactInfo.nvl(ai.getPackaging())
                + ArtifactInfo.FS
                + ai.getLastModified()
                + ArtifactInfo.FS
                + ai.getSize()
                + ArtifactInfo.FS
                + ai.getSourcesExists().toString()
                + ArtifactInfo.FS
                + ai.getJavadocExists().toString()
                + ArtifactInfo.FS
                + ai.getSignatureExists().toString()
                + ArtifactInfo.FS
                + ai.getFileExtension();

        doc.add(FLD_INFO.toField(info));

        doc.add(FLD_GROUP_ID_KW.toField(ai.getGroupId()));
        doc.add(FLD_ARTIFACT_ID_KW.toField(ai.getArtifactId()));
        doc.add(FLD_VERSION_KW.toField(ai.getVersion()));

        // V3
        doc.add(FLD_GROUP_ID.toField(ai.getGroupId()));
        doc.add(FLD_ARTIFACT_ID.toField(ai.getArtifactId()));
        doc.add(FLD_VERSION.toField(ai.getVersion()));
        doc.add(FLD_EXTENSION.toField(ai.getFileExtension()));

        if (ai.getName() != null) {
            doc.add(FLD_NAME.toField(ai.getName()));
        }

//        if (ai.getDescription() != null) {
//            doc.add(FLD_DESCRIPTION.toField(ai.getDescription()));
//        }

        if (ai.getPackaging() != null) {
            doc.add(FLD_PACKAGING.toField(ai.getPackaging()));
        }

        if (ai.getClassifier() != null) {
            doc.add(FLD_CLASSIFIER.toField(ai.getClassifier()));
        }

//        if (ai.getSha1() != null) {
//            doc.add(FLD_SHA1.toField(ai.getSha1()));
//        }
    }

    @Override
    public boolean updateArtifactInfo(Document doc, ArtifactInfo ai) {
        boolean res = false;

        String uinfo = doc.get(ArtifactInfo.UINFO);

        if (uinfo != null) {

            int start = 0;
            int end = uinfo.indexOf(FS);
            ai.setGroupId(uinfo.substring(start, end));

            start = end + 1;
            end = uinfo.indexOf(FS, start);
            ai.setArtifactId(uinfo.substring(start, end));

            start = end + 1;
            end = uinfo.indexOf(FS, start);
            ai.setVersion(uinfo.substring(start, end));

            start = end + 1;
            end = uinfo.indexOf(FS, start);
            if (end == -1) {
                end = uinfo.length();
            }
            ai.setClassifier(ArtifactInfo.renvl(uinfo.substring(start, end)));

            if (end < uinfo.length()) {
                start = end + 1;
                end = uinfo.length();
                ai.setFileExtension(uinfo.substring(start, end));
            }

            res = true;
        }

        String info = doc.get(ArtifactInfo.INFO);

        if (info != null) {

            int start = 0;
            int end = info.indexOf(FS);
            ai.setPackaging(ArtifactInfo.renvl(info.substring(start, end)));

            start = end + 1;
            end = info.indexOf(FS, start);
            ai.setLastModified(Long.parseLong(info.substring(start, end)));

            start = end + 1;
            end = info.indexOf(FS, start);
            ai.setSize(Long.parseLong(info.substring(start, end)));

            start = end + 1;
            end = info.indexOf(FS, start);
            ai.setSourcesExists(ArtifactAvailability.fromString(info.substring(start, end)));

            start = end + 1;
            end = info.indexOf(FS, start);
            ai.setJavadocExists(ArtifactAvailability.fromString(info.substring(start, end)));

            start = end + 1;
            end = info.indexOf(FS, start);
            if (end == -1) {
                end = info.length();
            }
            ai.setSignatureExists(ArtifactAvailability.fromString(info.substring(start, end)));

            if (end < info.length()) {
                start = end + 1;
                end = info.length();
                ai.setFileExtension(info.substring(start, end));
            } else {
                if (ai.getClassifier() != null //
                        || "pom".equals(ai.getPackaging()) //
                        || "war".equals(ai.getPackaging()) //
                        || "ear".equals(ai.getPackaging())) {
                    ai.setFileExtension(ai.getPackaging());
                } else {
                    ai.setFileExtension("jar"); // best guess
                }
            }

            res = true;
        }

        String name = doc.get(ArtifactInfo.NAME);

        if (name != null) {
            ai.setName(name);
            res = true;
        }

//        String description = doc.get(ArtifactInfo.DESCRIPTION);
//
//        if (description != null) {
//            ai.setDescription(description);
//            res = true;
//        }

        // sometimes there's a pom without packaging(default to jar), but no artifact, then the value will be a "null"
        // String
        if ("null".equals(ai.getPackaging())) {
            ai.setPackaging(null);
        }

//        String sha1 = doc.get(ArtifactInfo.SHA1);
//
//        if (sha1 != null) {
//            ai.setSha1(sha1);
//        }

        return res;
    }
}