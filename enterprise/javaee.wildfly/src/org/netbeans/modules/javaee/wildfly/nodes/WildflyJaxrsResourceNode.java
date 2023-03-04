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
package org.netbeans.modules.javaee.wildfly.nodes;

import java.awt.Image;
import org.netbeans.modules.javaee.wildfly.config.WildflyJaxrsResource;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author <a href="mailto:ehugonne@redhat.com">Emmanuel Hugonnet</a> (c) 2015 Red Hat, inc.
 */
public class WildflyJaxrsResourceNode extends AbstractNode {

    public WildflyJaxrsResourceNode(WildflyJaxrsResource resource, Lookup lookup) {
        super(new WildflyJaxrsResourceMethodsChildren(resource.getServerUrl(), resource.getMethods()));
        String resourceName;
        if(resource.getClassName().indexOf('.') > 0) {
            resourceName = resource.getClassName().substring(resource.getClassName().lastIndexOf('.') + 1);
        } else {
            resourceName = resource.getClassName();
        }
        setDisplayName(String.format("%s [%s]", resourceName, resource.getPath()));
        setDisplayName(String.format("%s [%s]", resourceName, resource.getPath()));
        setDisplayName(String.format("%s [%s]", resourceName, resource.getPath()));
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(Util.JAXRS_ICON);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage(Util.JAXRS_ICON);
    }
}
