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
package org.netbeans.modules.spring.beans.jumpto;

import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.openide.filesystems.FileUtil;

/**
 * 
 * @author Rohan Ranade
 */
public class BeanTypeDescriptor extends AbstractBeanTypeDescriptor {

    private final String className;
    private final int offset;

    public BeanTypeDescriptor(String displayName, SpringBean springBean) {
        super(displayName, springBean.getLocation().getFile());
        this.offset = springBean.getLocation().getOffset();
        this.className = springBean.getClassName();
    }

    @Override
    public String getContextName() {
        if(className == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append( " (").append(className).append(")");
        return sb.toString();
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void open() {
        SpringXMLConfigEditorUtils.openFile(getFileObject(), getOffset());
    }
}
