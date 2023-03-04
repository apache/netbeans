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
package org.netbeans.modules.project.spi.intern;

import java.io.IOException;
import javax.swing.Icon;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 */
public final class ProjectIDEServices {
    private static ProjectIDEServicesImplementation impl;

    private static synchronized ProjectIDEServicesImplementation getImpl() {
        if(impl == null) {
            impl = Lookup.getDefault().lookup(ProjectIDEServicesImplementation.class);
        }
        return impl;
    }
    
    public static boolean isUserQuestionException(IOException ioe) {
        ProjectIDEServicesImplementation i = getImpl();
        return i != null ? getImpl().isUserQuestionException(ioe) : false;
    }

    public static void handleUserQuestionException(IOException e, final ProjectIDEServicesImplementation.UserQuestionExceptionCallback callback) {
        ProjectIDEServicesImplementation i = getImpl();
        if(i != null) {
            i.handleUserQuestionException(e, callback);
        }
    }
    
    public static void notifyWarning(String message) {
        ProjectIDEServicesImplementation i = getImpl();
        if(i != null) {
            i.notifyWarning(message);
        }
    }
    
    public static ProjectIDEServicesImplementation.FileBuiltQuerySource createFileBuiltQuerySource(FileObject sourceFile) {
        ProjectIDEServicesImplementation i = getImpl();
        return i != null ? i.createFileBuiltQuerySource(sourceFile) : null;
    }
    
    public static Icon loadImageIcon( String resource, boolean localized ) {
        ProjectIDEServicesImplementation i = getImpl();
        return i != null ? i.loadIcon(resource, localized) : null;
    }
    
    public static boolean isEventDispatchThread() {
        ProjectIDEServicesImplementation i = getImpl();
        return i != null ? i.isEventDispatchThread() : false;
    }
}
