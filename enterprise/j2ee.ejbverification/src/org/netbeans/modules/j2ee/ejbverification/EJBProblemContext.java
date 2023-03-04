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
package org.netbeans.modules.j2ee.ejbverification;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota
 */
public class EJBProblemContext {

    private final FileObject fileObject;
    private final TypeElement clazz;
    private final Ejb ejb;
    private final SessionData sessionData;
    private final Project project;
    private final EjbJar ejbModule;

    public EJBProblemContext(
            Project project,
            EjbJar ejbModule,
            FileObject fileObject,
            TypeElement clazz,
            Ejb ejb,
            SessionData sessionData) {
        this.project = project;
        this.ejbModule = ejbModule;
        this.fileObject = fileObject;
        this.clazz = clazz;
        this.ejb = ejb;
        this.sessionData = sessionData;
    }
    
    public FileObject getFileObject() {
        return fileObject;
    }

    public TypeElement getClazz() {
        return clazz;
    }

    public Ejb getEjb() {
        return ejb;
    }

    public SessionData getEjbData() {
        return sessionData;
    }

    public EjbJar getEjbModule() {
        return ejbModule;
    }

    public Project getProject() {
        return project;
    }

    /**
     * Stores all data necessary for the hints computation, to precompute most of information.
     */
    public static class SessionData {

        private final String[] businessLocal;
        private final String[] businessRemote;
        private final String sessionType;

        public SessionData(String[] businessLocal, String[] businessRemote, String sessionType) {
            this.businessLocal = businessLocal;
            this.businessRemote = businessRemote;
            this.sessionType = sessionType;
        }

        public String getSessionType() {
            return sessionType;
        }

        public String[] getBusinessLocal() {
            return businessLocal;
        }

        public String[] getBusinessRemote() {
            return businessRemote;
        }
    }
}
