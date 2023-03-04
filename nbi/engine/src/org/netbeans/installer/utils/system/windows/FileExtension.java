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

package org.netbeans.installer.utils.system.windows;

import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Dmitry Lipin
 */
public class FileExtension {
    private String name;
    private String description;
    private PerceivedType perceivedType;
    private String mimeType;
    private String icon;
    public FileExtension(String extName) {
        setName(extName);
    }
    
    protected FileExtension(FileExtension fe) {
        name = fe.name;
        description = fe.description;
        perceivedType = fe.perceivedType;
        mimeType = fe.mimeType;
        icon = fe.icon;
    }
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        //remove starting dots
        if(name!=null) {
            while(name.substring(0,1).equals(StringUtils.DOT)) {
                name = name.substring(1);
            }
        }
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public PerceivedType getPerceivedType() {
        return perceivedType;
    }
    
    public void setPerceivedType(PerceivedType perceivedType) {
        this.perceivedType = perceivedType;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getDotName() {
        return StringUtils.DOT + getName();
    }
}
