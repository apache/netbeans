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

package org.netbeans.modules.j2ee.ejbcore.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class HomeMethodGenerator extends AbstractMethodGenerator {

    private HomeMethodGenerator(String ejbClass, FileObject ejbClassFileObject) {
        super(ejbClass, ejbClassFileObject);
    }

    public static HomeMethodGenerator create(String ejbClass, FileObject ejbClassFileObject) {
        return new HomeMethodGenerator(ejbClass, ejbClassFileObject);
    }

    /**
     * Generates home method.
     * <p>
     * <b>Should be called outside EDT.</b>
     */
    public void generate(MethodModel methodModel, boolean generateLocal, boolean generateRemote) throws IOException {

        Map<String, String> interfaces = getInterfaces();
        String local = interfaces.get(EntityAndSession.LOCAL);
        String localHome = interfaces.get(EntityAndSession.LOCAL_HOME);
        String remote = interfaces.get(EntityAndSession.REMOTE);
        String remoteHome = interfaces.get(EntityAndSession.HOME);

        // local interface
        if (generateLocal && localHome != null) {
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    methodModel.getReturnType(),
                    null,
                    methodModel.getParameters(),
                    methodModel.getExceptions(),
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, localHome);
            addMethod(methodModelCopy, fileObject, localHome);
        }

        // remote interface
        if (generateRemote && remoteHome != null) {
            List<String> exceptions = exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) {
                exceptions.add("java.rmi.RemoteException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    methodModel.getReturnType(),
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, remoteHome);
            addMethod(methodModelCopy, fileObject, remoteHome);
        }

        // ejb class
        MethodModel methodModelCopy = MethodModel.create(
                "ejbHome" + Character.toUpperCase(methodModel.getName().charAt(0)) + methodModel.getName().substring(1),
                methodModel.getReturnType(),
                methodModel.getBody(),
                methodModel.getParameters(),
                methodModel.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
        addMethod(methodModelCopy, ejbClassFileObject, ejbClass);

    }

}
