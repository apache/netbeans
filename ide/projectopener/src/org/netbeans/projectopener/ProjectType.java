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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.projectopener;

/**
 *
 * @author Milan Kubec
 */
public class ProjectType {

    public static final int UNKNOWN_TYPE = 1;
    public static final int J2SE_TYPE = 2;
    public static final int FREEFORM_TYPE = 3;
    public static final int J2ME_TYPE = 4;
    public static final int WEB_TYPE = 5;
    public static final int EJB_TYPE = 6;
    public static final int EAR_TYPE = 7;
    public static final int MAVEN_TYPE = 7;
    
    public static final String J2SE_NAME = "org.netbeans.modules.java.j2seproject";
    public static final String FREEFORM_NAME = "org.netbeans.modules.ant.freeform";
    public static final String J2ME_NAME = "org.netbeans.modules.kjava.j2meproject";
    public static final String WEB_NAME = "org.netbeans.modules.web.project";
    public static final String EJB_NAME = "org.netbeans.modules.j2ee.ejbjarproject";
    public static final String EAR_NAME = "org.netbeans.modules.j2ee.earproject";
    public static final String MAVEN_NAME = "maven";
    
    public static final ProjectType J2SE = new J2SEProjectType();
    public static final ProjectType FREEFORM = new FreeformProjectType();
    public static final ProjectType J2ME = new J2MEProjectType();
    public static final ProjectType WEB = new WebProjectType();
    public static final ProjectType EJB = new EJBProjectType();
    public static final ProjectType EAR = new EARProjectType();
    public static final ProjectType MAVEN = new MavenProjectType();
    
    private String typeString;
    private String[] importantFiles;
    
    public ProjectType(String type, String[] impFiles) {
        typeString = type;
        importantFiles = impFiles;
    }
    
    public String getTypeString() {
        return typeString;
    }
    
    public String[] getImportantFiles() {
        return importantFiles;
    }
    
    public String toString() {
        return getTypeString();
    }
    
    // ---
     
    public static final class J2SEProjectType extends ProjectType {
        public J2SEProjectType() {
            super(J2SE_NAME, new String[] { "modules/org-netbeans-modules-java-j2seproject.jar" });
        }
    }
    
    public static final class FreeformProjectType extends ProjectType {
        public FreeformProjectType() {
            super(FREEFORM_NAME, new String[] { "modules/org-netbeans-modules-ant-freeform.jar" });
        }
    }
    
    public static final class J2MEProjectType extends ProjectType {
        public J2MEProjectType() {
            super(J2ME_NAME, new String[] { "modules/org-netbeans-modules-kjava-j2meproject.jar" });
        }
    }
    
    public static final class WebProjectType extends  ProjectType {
        public WebProjectType() {
            super(WEB_NAME, new String[] { "modules/org-netbeans-modules-web-project.jar" });
        }
    }
    
    public static final class EJBProjectType extends ProjectType {
        public EJBProjectType() {
            super(EJB_NAME, new String[] { "modules/org-netbeans-modules-j2ee-ejbjarproject.jar" });
        }
    }
    
    public static final class EARProjectType extends ProjectType {
        public EARProjectType() {
            super(EAR_NAME, new String[] { "modules/org-netbeans-modules-j2ee-earproject.jar" });
        }
    }
    
    public static final class MavenProjectType extends ProjectType {
        public MavenProjectType() {
            super(MAVEN_NAME, new String[] { "modules/org-codehaus-mevenide-netbeans.jar" });
        }
    }
    
}
