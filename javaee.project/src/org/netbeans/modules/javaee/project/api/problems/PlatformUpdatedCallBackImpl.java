/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
