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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ws.qaf.rest;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;

/**
 * Tests for New REST from Patterns wizard
 *
 * @author lukas
 */
public class JEE6PatternsTest extends PatternsTest {

    protected static Server server = Server.GLASSFISH;

    /**
     * Def constructor.
     *
     * @param testName name of particular test case
     */
    public JEE6PatternsTest(String name) {
        super(name, server);
    }

    @Override
    protected JavaEEVersion getJavaEEversion() {
        return JavaEEVersion.JAVAEE6;
    }

    /**
     * Creates suite from particular test cases. You can define order of testcases here.
     */
    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, JEE6PatternsTest.class,
                "testSingletonDef", //NOI18N
                "testContainerIDef", //NOI18N
                "testCcContainerIDef", //NOI18N
                "testSingleton1", //NOI18N
                "testCcContainerI1", //NOI18N
                "testSingleton2", //NOI18N
                "testContainerI1", //NOI18N
                "testContainerI2", //NOI18N
                "testSingleton3", //NOI18N
                "testContainerI3", //NOI18N
                "testCcContainerI2", //NOI18N
                "testCcContainerI3", //NOI18N
                "testDeploy", //NOI18N
                "testUndeploy"); //NOI18N
    }

    static class Pkg extends JComponentByLabelFinder {

        public Pkg() {
            //Resource Package:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_Package"));
        }
    }

    static class ClsName extends JComponentByLabelFinder {

        public ClsName() {
            //Class Name:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ClassName"));
        }
    }

    static class Mime extends JComponentByLabelFinder {

        public Mime() {
            //MIME Type:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_MimeType"));
        }
    }

    static class RCls extends JComponentByLabelFinder {

        public RCls() {
            //Representation Class:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_RepresentationClass"));
        }
    }

    static class Path extends JComponentByLabelFinder {

        public Path() {
            //Path:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_UriTemplate"));
        }
    }

    static class CClsName extends JComponentByLabelFinder {

        public CClsName() {
            //Container Class Name:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ContainerClass"));
        }
    }

    static class CPath extends JComponentByLabelFinder {

        public CPath() {
            //Container Path:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ContainerUriTemplate"));
        }
    }

    static class CRCls extends JComponentByLabelFinder {

        public CRCls() {
            //Container Representation Class:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ContainerRepresentationClass"));
        }
    }

    static class Loc extends JComponentByLabelFinder {

        public Loc() {
            //Location:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_SrcLocation"));
        }
    }
}
