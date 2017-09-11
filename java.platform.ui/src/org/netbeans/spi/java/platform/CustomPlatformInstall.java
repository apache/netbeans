/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.spi.java.platform;

import org.openide.WizardDescriptor;


/**
 * Defines an API for registering custom Java platform installer. The installer
 * is responsible for instantiation of {@link JavaPlatform} through the provided
 * TemplateWizard.Iterator. If your installer selects the platform on the local disk you
 * probably don't want to use this class, the {@link PlatformInstall} class
 * creates an platform chooser for you. You want to use this class if the
 * platform is not on the local disk, eg. you want to download it from the web.
 * 
 * Consult the {@link GeneralPlatformInstall} javadoc about the {@link CustomPlatformInstall} registration.
 * 
 * @author Tomas Zezula
 * @since 1.5
 */
public abstract class CustomPlatformInstall extends GeneralPlatformInstall {
    
    /**
     * Returns the {@link WizardDescriptor#InstantiatingIterator} used to install
     * the platform. The platform definition file returned by the instantiate method
     * should be created in the Services/Platforms/org-netbeans-api-java-Platform
     * folder on the system filesystem.
     * @return TemplateWizard.Iterator instance responsible for instantiating
     * the platform. The instantiate method of the returned iterator should
     * return the Set containing the platform.
     */
    public abstract WizardDescriptor.InstantiatingIterator<WizardDescriptor> createIterator();
    
}
