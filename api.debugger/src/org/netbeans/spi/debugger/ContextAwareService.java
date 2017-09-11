/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * <br/>
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
