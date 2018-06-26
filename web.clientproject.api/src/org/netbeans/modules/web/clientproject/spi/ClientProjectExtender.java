/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.spi;

import org.netbeans.api.annotations.common.NonNull;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;

/**
 * Extender interface to extend behavior of new HTML5 Project Wizard.
 * Instances to be registered via @ServiceProvider.
 * @author Jan Becicka
 */
public interface ClientProjectExtender {

    /**
     * Initialize new HTML5 Project.
     * @param wizardDescriptor corresponding WizardDescriptor
     */
    public void initialize(WizardDescriptor wizardDescriptor);

    /**
     * Creates additional Wizard Descriptor Panels, which will be added before default HTML5 Project Wizard Panels.
     * @return
     */
    @NonNull
    public Panel<WizardDescriptor>[] createInitPanels();
    /**
     * Creates additional Wizard Descriptor Panels, which will be added after default HTML5 Projezt Wizard panels.
     * Wizard.
     * @return 
     */
    @NonNull
    public Panel<WizardDescriptor>[] createWizardPanels();

    /**
     * Creates additional changes in Client Side Project.
     * @param projectRoot
     * @param siteRoot
     * @param libsPath 
     */
    public void apply(FileObject projectRoot, FileObject siteRoot, String libsPath);
    
}
