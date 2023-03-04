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
