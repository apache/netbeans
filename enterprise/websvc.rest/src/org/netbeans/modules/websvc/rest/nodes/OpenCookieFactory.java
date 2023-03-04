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
package org.netbeans.modules.websvc.rest.nodes;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Liu
 */
public class OpenCookieFactory {
    
    public static OpenCookie create(Project project, String className) {
        return create(project, className, null);
    }
    
    public static OpenCookie create(Project project, String className, String methodName) {
        return new OpenCookieImpl(project, className, methodName);
    }
    
    private static class OpenCookieImpl implements OpenCookie {
        private String className;
        private String methodName;
        private Project project;
        
        public OpenCookieImpl(Project project, String className, String methodName) {
            this.project = project;
            this.className = className;
            this.methodName = methodName;
        } 
        
        @Override
        public void open() {
            if ( SwingUtilities.isEventDispatchThread()){
                final AtomicBoolean cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        doOpen();
                    }
                },
                NbBundle.getMessage(OpenCookieFactory.class, "TXT_OpenResource"),    // NOI18N
                cancel,
                false);
            }
            else {
                doOpen();
            }
        }

        private void doOpen() {
            try {
                FileObject source = SourceGroupSupport.getFileObjectFromClassName(
                        className, project);
                if (source != null) {
                    final DataObject dataObj = DataObject.find(source);
                    if (dataObj != null) {
                        JavaSource javaSource = JavaSource.forFileObject(source);
                        final long[] position = JavaSourceHelper.getPosition(
                                javaSource, methodName);
                        
                        SwingUtilities.invokeLater( new Runnable() {
                            
                            @Override
                            public void run() {
                                OpenCookie oc = dataObj.getCookie(OpenCookie.class);
                                if (oc != null) {
                                    oc.open();
                                }
                                LineCookie lc = dataObj.getCookie(LineCookie.class);
                                if (lc != null) {
                                    Line line = lc.getLineSet().getOriginal(
                                            (int) position[0]);
                                    line.show(ShowOpenType.OPEN, 
                                            ShowVisibilityType.NONE, (int) position[1]);
                                }
                                
                            }
                        });
                        
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
