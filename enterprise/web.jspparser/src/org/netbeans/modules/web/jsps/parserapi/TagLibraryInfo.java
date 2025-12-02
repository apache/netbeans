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
package org.netbeans.modules.web.jsps.parserapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TagLibraryInfo {
    private String shortName;
    private String reliableURN;
    private String infoString;
    private String URI;
    private String prefixString;
    private String requiredVersion;
    private String tlibversion;
    private final List<TagInfo> tags = new ArrayList<>();
    private final List<TagFileInfo> tagFiles = new ArrayList<>();

    public TagLibraryInfo() {
    }

    public TagLibraryInfo(String shortName, String reliableURN, String infoString, String URI, String prefixString, String requiredVersion, List<TagInfo> tags, List<TagFileInfo> tagFiles) {
        this.shortName = shortName;
        this.reliableURN = reliableURN;
        this.infoString = infoString;
        this.URI = URI;
        this.prefixString = prefixString;
        this.requiredVersion = requiredVersion;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        if (tagFiles != null) {
            this.tagFiles.addAll(tagFiles);
        }
    }

    public TagLibraryInfo(String requiredVersion) {
        this.requiredVersion = requiredVersion;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getReliableURN() {
        return reliableURN;
    }

    public void setReliableURN(String reliableURN) {
        this.reliableURN = reliableURN;
    }

    public String getInfoString() {
        return infoString;
    }

    public void setInfoString(String infoString) {
        this.infoString = infoString;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getPrefixString() {
        return prefixString;
    }

    public void setPrefixString(String prefixString) {
        this.prefixString = prefixString;
    }

    public String getRequiredVersion() {
        return requiredVersion;
    }

    public void setRequiredVersion(String requiredVersion) {
        this.requiredVersion = requiredVersion;
    }

    public String getTlibversion() {
        return tlibversion;
    }

    public void setTlibversion(String tlibversion) {
        this.tlibversion = tlibversion;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<TagInfo> getTags() {
        return tags;
    }

    public TagInfo getTag(String tag) {
        return tags
                .stream()
                .filter(ti -> Objects.equals(tag, ti.getTagName()))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<TagFileInfo> getTagFiles() {
        return tagFiles;
    }

    public TagFileInfo getTagFile(String tag) {
        return tagFiles
                .stream()
                .filter(ti -> Objects.equals(tag, ti.getName()))
                .findFirst()
                .orElse(null);
    }
}
