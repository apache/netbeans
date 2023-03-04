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
public class ScmImpl extends POMComponentImpl implements Scm {

    public ScmImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ScmImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().SCM));
    }

    // attributes

    // child elements
    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getConnection() {
        return getChildElementText(getModel().getPOMQNames().CONNECTION.getQName());
    }

    @Override
    public void setConnection(String connection) {
        setChildElementText(getModel().getPOMQNames().CONNECTION.getName(), connection,
                getModel().getPOMQNames().CONNECTION.getQName());
    }

    @Override
    public String getDeveloperConnection() {
        return getChildElementText(getModel().getPOMQNames().DEVELOPERCONNECTION.getQName());
    }

    @Override
    public void setDeveloperConnection(String connection) {
        setChildElementText(getModel().getPOMQNames().DEVELOPERCONNECTION.getName(), connection,
                getModel().getPOMQNames().DEVELOPERCONNECTION.getQName());
    }

    @Override
    public String getUrl() {
        return getChildElementText(getModel().getPOMQNames().URL.getQName());
    }

    @Override
    public void setUrl(String url) {
        setChildElementText(getModel().getPOMQNames().URL.getName(), url,
                getModel().getPOMQNames().URL.getQName());
    }

    @Override
    public String getTag() {
        return getChildElementText(getModel().getPOMQNames().TAG.getQName());
    }

    @Override
    public void setTag(String tag) {
        setChildElementText(getModel().getPOMQNames().TAG.getName(), tag,
                getModel().getPOMQNames().TAG.getQName());
    }

}
