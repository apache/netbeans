/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.jaxb.model;

import java.math.BigDecimal;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.netbeans.modules.xml.jaxb.spi.JAXBWizModuleConstants;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Mutex;

/**
 *
 * @author gpatil
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType={
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-j2ee-ejbjarproject"
})
public class JAXBWizProjectOpenedHookImpl extends ProjectOpenedHook{
    private Project prj;
    
    public JAXBWizProjectOpenedHookImpl(Project project){
        this.prj = project;
    }
    
    @Override
    protected void projectClosed() {
    }

    @Override
    protected void projectOpened() {
        Schemas scs = ProjectHelper.getXMLBindingSchemas(prj);
        if (scs != null && scs.sizeSchema() > 0){
            BigDecimal v = scs.getVersion();
            if ((v == null) || (JAXBWizModuleConstants.LATEST_CFG_VERSION.compareTo(v) > 0)){
                ProjectHelper.migrateProjectFromPreDot5Version(prj);
            }
            // Set endorsed classpath
            ProjectManager.mutex().writeAccess(
                    new Mutex.Action() {
                        public Object run() {
                            try {
                                ProjectHelper.addJaxbApiEndorsed(prj);
                            } catch (java.io.IOException ex) {
                                ex.printStackTrace();
                            }
                            return null;
                        }
                    });

        }
    }
}
