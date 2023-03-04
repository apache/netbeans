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
package org.netbeans.nbbuild.extlibs.licenseinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Fileset {

    private final List<File> files = new ArrayList<>();
    private String licenseRef;
    private String licenseInfo;
    private CommentType commentType;
    private String comment;
    private String notice;
    private boolean sourceOnly;

    public List<File> getFiles() {
        return files;
    }

    public String getLicenseRef() {
        return licenseRef;
    }

    public void setLicenseRef(String licenseRef) {
        if (licenseRef != null && licenseRef.trim().isEmpty()) {
            licenseRef = null;
        }
        this.licenseRef = licenseRef;
    }

    public String getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(String licenseInfo) {
        if (licenseInfo != null && licenseInfo.trim().isEmpty()) {
            licenseInfo = null;
        }
        this.licenseInfo = licenseInfo;
    }

    public CommentType getCommentType() {
        return commentType;
    }

    public void setCommentType(CommentType commentType) {
        this.commentType = commentType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (comment != null && comment.trim().isEmpty()) {
            comment = null;
        }
        this.comment = comment;
    }

    public boolean isSourceOnly() {
        return sourceOnly;
    }

    public void setSourceOnly(boolean sourceOnly) {
        this.sourceOnly = sourceOnly;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        if (notice != null && notice.trim().isEmpty()) {
            notice = null;
        }
        this.notice = notice;
    }

    void parse(File licenseinfo, Element fileset) {
        NodeList childNodes = fileset.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element) {
                Element e = (Element) childNodes.item(i);
                if ("file".equals(e.getTagName())) {
                    getFiles().add(new File(licenseinfo.getParentFile(), e.getTextContent()));
                } else if ("license".equals(e.getTagName())) {
                    String text = e.getTextContent();
                    if (text != null && (!text.trim().isEmpty())) {
                        setLicenseInfo(text);
                    }
                    if (!e.getAttribute("ref").isEmpty()) {
                        setLicenseRef(e.getAttribute("ref"));
                    }
                } else if ("comment".equals(e.getTagName())) {
                    String text = e.getTextContent();
                    if (text != null && (!text.trim().isEmpty())) {
                        setComment(text);
                    }
                    if (!e.getAttribute("type").isEmpty()) {
                        setCommentType(CommentType.valueOf(e.getAttribute("type")));
                    }
                } else if ("notice".equals(e.getTagName())) {
                    String text = e.getTextContent();
                    if (text != null && (!text.trim().isEmpty())) {
                        setNotice(text);
                    }
                } else if ("sourceOnly".equals(e.getTagName())) {
                    setSourceOnly(true);
                }
            }
        }
    }
}
