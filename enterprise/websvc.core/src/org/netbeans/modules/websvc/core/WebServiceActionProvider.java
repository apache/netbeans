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

package org.netbeans.modules.websvc.core;

import org.netbeans.modules.websvc.spi.support.InvokeOperationActionProvider;
import org.netbeans.modules.websvc.api.support.InvokeOperationCookie;
import org.netbeans.modules.websvc.spi.support.AddOperationActionProvider;
import org.netbeans.modules.websvc.api.support.AddOperationCookie;
import java.util.Collection;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author mkuchtiak
 */
public class WebServiceActionProvider {

    private static final Lookup.Result<AddOperationActionProvider> addOperationActionProviders =
        Lookup.getDefault().lookup(new Lookup.Template<AddOperationActionProvider>(AddOperationActionProvider.class));
    
    /** Find AddOperationCookie for given FileObject (target source)
     */
    public static AddOperationCookie getAddOperationAction(FileObject targetSource) {
        Collection<? extends AddOperationActionProvider> instances = addOperationActionProviders.allInstances();
        for (AddOperationActionProvider impl: instances) {
            AddOperationCookie cookie = impl.getAddOperationCookie(targetSource);
            if (cookie != null) {
                return cookie;
            }
        }
        return null;
    }
    
    private static final Lookup.Result<InvokeOperationActionProvider> invokeOperationActionProviders =
        Lookup.getDefault().lookup(new Lookup.Template<InvokeOperationActionProvider>(InvokeOperationActionProvider.class));

    /** Find InvokeOperationCookie for given FileObject (target source)
     * and given web service operation
     */
    public static InvokeOperationCookie getInvokeOperationAction(FileObject targetSource) {
        Collection<? extends InvokeOperationActionProvider> instances = invokeOperationActionProviders.allInstances();
        for (InvokeOperationActionProvider impl: instances) {
            InvokeOperationCookie cookie = impl.getInvokeOperationCookie(targetSource);
            if (cookie != null) {
                return cookie;
            }
        }
        return null;
    }
}
