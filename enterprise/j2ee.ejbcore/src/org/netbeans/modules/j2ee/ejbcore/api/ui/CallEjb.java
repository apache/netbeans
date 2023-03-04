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

package org.netbeans.modules.j2ee.ejbcore.api.ui;

import java.io.IOException;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddFinderMethodStrategy;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddSelectMethodStrategy;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.CallEjbDialog;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Pavel Buzek
 */
public final class CallEjb {
    
    public static boolean showCallEjbDialog(FileObject fileObject, String className, String title) {
        try {
            return new CallEjbDialog().open(fileObject, className, title);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
    
    public static void addFinderMethod(FileObject fileObject, String beanClass) throws IOException {
        new AddFinderMethodStrategy().addMethod(fileObject, beanClass);
    }
    
    public static void addSelectMethod(FileObject fileObject, String beanClass) throws IOException {
        new AddSelectMethodStrategy().addMethod(fileObject, beanClass);
    }
}
