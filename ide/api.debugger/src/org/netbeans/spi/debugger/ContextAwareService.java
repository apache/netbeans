/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.spi.debugger;

/**
 * Instance of registry entry, that delegates to a debugger service, that can be
 * context-aware.
 * This instances should be registered in layers and created by <code>ContextAwareSupport.createService</code> factory
 * method as follows:
 *
 *   <pre style="background-color: rgb(255, 255, 200);">
 *   &lt;folder name="Debugger"&gt;
 *       &lt;file name="MyDebuggerService.instance"&gt;
 *           &lt;attr name="instanceCreate" methodvalue="org.netbeans.spi.debugger.ContextAwareSupport.createService"/&gt;
 *           &lt;attr name="serviceName" stringvalue="org.netbeans.my_debugger.MyServiceImpl"/&gt;
 *           &lt;attr name="serviceClass" stringvalue="org.netbeans.debugger.Service"/&gt;
 *       &lt;/file&gt;
 *   &lt;/folder&gt;</pre>
 *
 * <br>
 *
 * @author Martin Entlicher
 * @since 1.16
 */
public interface ContextAwareService<T> {

    /**
     * Create a debugger service in a context.
     * 
     * @param context the context to create the service with
     * @return the debugger service of type <code>T</code>.
     */
    T forContext(ContextProvider context);

    /**
     * The ID of the service, usually the implementation class name.
     * Services can be made hidden by this ID.
     *
     * @return the session ID
     */
    // Lookup.Item.getId() is used instead.
    //String serviceID();
    
}
