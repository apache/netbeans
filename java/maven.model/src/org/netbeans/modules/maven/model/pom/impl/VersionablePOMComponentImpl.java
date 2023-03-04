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
package org.netbeans.modules.maven.model.pom.impl;

import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.POMQNames;
import org.netbeans.modules.maven.model.pom.VersionablePOMComponent;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public abstract class VersionablePOMComponentImpl extends POMComponentImpl implements VersionablePOMComponent {

    public VersionablePOMComponentImpl(POMModel model, Element element) {
        super(model, element);
    }

    // attributes


    @Override
    public String getGroupId() {
        POMModel model = getModel();
        assert model != null;
        POMQNames qnames = model.getPOMQNames();
        assert qnames != null;
        POMQName groupid = qnames.GROUPID;
        assert groupid != null;
        return getChildElementText(groupid.getQName());
    }

    @Override
    public void setGroupId(String groupId) {
        setChildElementText(getModel().getPOMQNames().GROUPID.getName(), groupId,
                getModel().getPOMQNames().GROUPID.getQName());
    }

    @Override
    public String getArtifactId() {
        return getChildElementText(getModel().getPOMQNames().ARTIFACTID.getQName());
    }

    @Override
    public void setArtifactId(String artifactId) {
        setChildElementText(getModel().getPOMQNames().ARTIFACTID.getName(), artifactId,
                getModel().getPOMQNames().ARTIFACTID.getQName());
    }

    @Override
    public String getVersion() {
        return getChildElementText(getModel().getPOMQNames().VERSION.getQName());
    }

    @Override
    public void setVersion(String version) {
        setChildElementText(getModel().getPOMQNames().VERSION.getName(), version,
                getModel().getPOMQNames().VERSION.getQName());
    }

}
