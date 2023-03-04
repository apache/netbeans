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

import org.netbeans.modules.maven.model.pom.*;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ExclusionImpl extends POMComponentImpl implements Exclusion {

    public ExclusionImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ExclusionImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().EXCLUSION));
    }

    @Override
    public String getGroupId() {
        return getChildElementText(getModel().getPOMQNames().GROUPID.getQName());
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
    // attributes

    // child elements
    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    public static class List extends ListImpl<Exclusion> {
        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().EXCLUSION, Exclusion.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().EXCLUSIONS));
        }
    }

}
