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
package org.netbeans.modules.javaee.project.api.problems;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport.PlatformUpdatedCallBack;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Parameters;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Implementation of {@link PlatformUpdatedCallBack} for all web projects.
 * @since 1.18
 * @author Tomas Zezula
 */
public final class PlatformUpdatedCallBackImpl implements PlatformUpdatedCallBack {

    private static final String[] DEFAULT_PRE_ELEMENTS = {"name", "minimum-ant-version"};   //NOI18N

    private final String nameSpace;
    private final UpdateHelper helper;
    private final boolean setExplicitSourceSupported;
    private final Set<String> precedingElements;

    private PlatformUpdatedCallBackImpl(
            @NonNull final String nameSpace,
            @NonNull final UpdateHelper helper,
            final boolean setExplicitSourceSupported,
            @NonNull final String[] precedingElements) {
        Parameters.notNull("nameSpace", nameSpace); //NOI18N
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("precedingElements", precedingElements); //NOI18N
        this.nameSpace = nameSpace;
        this.helper = helper;
        this.setExplicitSourceSupported = setExplicitSourceSupported;
        this.precedingElements = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(precedingElements)));
    }

    private boolean updateProjectXml(@NonNull final JavaPlatform platform) {
        assert ProjectManager.mutex().isWriteAccess();
        final boolean remove = platform.equals(JavaPlatformManager.getDefault().getDefaultPlatform());
        final Element root = helper.getPrimaryConfigurationData(true);
        boolean changed = false;
        if (remove) {
            final Element platformElement = XMLUtil.findElement(
                root,
                "explicit-platform",    //NOI18N
                nameSpace);
            if (platformElement != null) {
                root.removeChild(platformElement);
                changed = true;
            }
        } else {
            Element insertBefore = null;
            for (Element e : XMLUtil.findSubElements(root)) {
                final String name = e.getNodeName();
                if (!precedingElements.contains(name)) {   //NOI18N
                    insertBefore = e;
                    break;
                }
            }
            final Element platformNode = insertBefore.getOwnerDocument().createElementNS(
                    nameSpace,
                    "explicit-platform"); //NOI18N
            if (setExplicitSourceSupported) {
                platformNode.setAttribute(
                        "explicit-source-supported",    //NOI18N
                        platform.getSpecification().getVersion().compareTo(new SpecificationVersion("1.3"))>0?   //NOI18N
                            "true":                                                                              //NOI18N
                            "false");                                                                            //NOI18N
            }
            root.insertBefore(platformNode, insertBefore);
            changed = true;
        }
        if (changed) {
            helper.putPrimaryConfigurationData(root, true);
        }
        return changed;
    }

    @Override
    public void platformPropertyUpdated(JavaPlatform platform) {
        updateProjectXml(platform);
    }

    @NonNull
    public static PlatformUpdatedCallBackImpl create (
            @NonNull final String nameSpace,
            @NonNull final UpdateHelper helper) {
        return new PlatformUpdatedCallBackImpl(nameSpace, helper, true, DEFAULT_PRE_ELEMENTS);
    }

    public static PlatformUpdatedCallBackImpl create(
            @NonNull final String nameSpace,
            @NonNull final UpdateHelper helper,
            final boolean setExplicitSourceSupported,
            @NonNull final String[] precedingElements) {
        return new PlatformUpdatedCallBackImpl(nameSpace, helper, setExplicitSourceSupported, precedingElements);
    }
}
