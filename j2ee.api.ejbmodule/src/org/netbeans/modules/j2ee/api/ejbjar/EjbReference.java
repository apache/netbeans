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

package org.netbeans.modules.j2ee.api.ejbjar;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public final class EjbReference {
    public static enum EjbRefIType{
        NO_INTERFACE("Bean"),
        LOCAL("Local"),
        REMOTE("Remote");

        private String name;
        EjbRefIType(String name){
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    private final String ejbClass;
    private final String ejbRefType;
    private final String local;
    private final String localHome;
    private final String remote;
    private final String remoteHome;
    private final EjbJar ejbModule;
    private final ClasspathInfo cpInfo;
    
    private EjbReference(String ejbClass, String ejbRefType, String local, String localHome, String remote, String remoteHome, EjbJar ejbModule) {
        this.ejbClass = ejbClass;
        this.ejbRefType = ejbRefType;
        this.local = local;
        this.localHome = localHome;
        this.remote = remote;
        this.remoteHome = remoteHome;
        this.ejbModule = ejbModule;
        
        FileObject[] javaSources = getEjbModule().getJavaSources();
        cpInfo = javaSources.length > 0 ? ClasspathInfo.create(
                    ClassPath.getClassPath(javaSources[0], ClassPath.BOOT),
                    ClassPath.getClassPath(javaSources[0], ClassPath.COMPILE),
                    ClassPath.getClassPath(javaSources[0], ClassPath.SOURCE)
                ) : null;
    }

    public static EjbReference create(String ejbClass, String ejbRefType, String local, String localHome, String remote, String remoteHome, EjbJar ejbModule) {
        return new EjbReference(ejbClass, ejbRefType, local, localHome, remote, remoteHome, ejbModule);
    }
    
    public String getEjbClass() {
        return ejbClass;
    }
    
    public String getEjbRefType() {
        return ejbRefType;
    }
    
    public String getLocal() {
        return local;
    }

    public String getLocalHome() {
        return localHome;
    }

    public String getRemote() {
        return remote;
    }

    public String getRemoteHome() {
        return remoteHome;
    }

    public EjbJar getEjbModule() {
        return ejbModule;
    }

    public String getHomeName(EjbRefIType iType){
        switch(iType){
            case LOCAL: return getLocalHome();
            case REMOTE: return getRemoteHome();
            case NO_INTERFACE: return getEjbClass();
            default: return null;
        }
    }

    public String getComponentName(EjbRefIType iType){
        switch(iType){
            case LOCAL: return getLocal();
            case REMOTE: return getRemote();
            case NO_INTERFACE: return getEjbClass();
            default: return null;
        }
    }

    public ClasspathInfo getClasspathInfo(){
        return cpInfo;
    }

    public FileObject getComponentFO(EjbRefIType iType){
        switch(iType){
            case LOCAL: return findFileObject(getLocal());
            case REMOTE: return findFileObject(getRemote());
            case NO_INTERFACE: return findFileObject(getEjbClass());
            default: return null;
        }
    }

    private FileObject findFileObject(final String className){
        if (cpInfo == null){
            return null;
        }
        final FileObject[] result = new FileObject[]{null};
        try{
            JavaSource.create(cpInfo).runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = controller.getElements().getTypeElement(className);
                    if (typeElement != null) {
                        result[0] = SourceUtils.getFile(ElementHandle.create(typeElement), controller.getClasspathInfo());
                    }
                }
            }, true);
        }catch(IOException ex){}
        
        return result[0];
    }
}
