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

import org.w3c.dom.Element;
import org.netbeans.modules.maven.model.pom.*;	
import org.netbeans.modules.maven.model.pom.POMComponentVisitor;	

/**
 *
 * @author mkleint
 */
public class MailingListImpl extends POMComponentImpl implements MailingList {

    public MailingListImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public MailingListImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().MAILINGLIST));
    }

    // attributes

    // child elements
    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getName() {
        return getChildElementText(getModel().getPOMQNames().NAME.getQName());
    }

    @Override
    public void setName(String name) {
        setChildElementText(getModel().getPOMQNames().NAME.getName(), name,
                getModel().getPOMQNames().NAME.getQName());
    }

    @Override
    public String getSubscribe() {
        return getChildElementText(getModel().getPOMQNames().SUBSCRIBE.getQName());
    }

    @Override
    public void setSubscribe(String subscribe) {
        setChildElementText(getModel().getPOMQNames().SUBSCRIBE.getName(), subscribe,
                getModel().getPOMQNames().SUBSCRIBE.getQName());
    }

    @Override
    public String getUnsubscribe() {
        return getChildElementText(getModel().getPOMQNames().UNSUBSCRIBE.getQName());
    }

    @Override
    public void setUnsubscribe(String unsubscribe) {
        setChildElementText(getModel().getPOMQNames().UNSUBSCRIBE.getName(), unsubscribe,
                getModel().getPOMQNames().UNSUBSCRIBE.getQName());
    }

    @Override
    public String getPost() {
        return getChildElementText(getModel().getPOMQNames().POST.getQName());
    }

    @Override
    public void setPost(String post) {
        setChildElementText(getModel().getPOMQNames().POST.getName(), post,
                getModel().getPOMQNames().POST.getQName());
    }

    @Override
    public String getArchive() {
        return getChildElementText(getModel().getPOMQNames().ARCHIVE.getQName());
    }

    @Override
    public void setArchive(String archive) {
        setChildElementText(getModel().getPOMQNames().ARCHIVE.getName(), archive,
                getModel().getPOMQNames().ARCHIVE.getQName());
    }

    public static class List extends ListImpl<MailingList> {
        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().MAILINGLIST, MailingList.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().MAILINGLISTS));
        }
    }

}
