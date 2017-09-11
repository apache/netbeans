/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.ui.registry;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFilter;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.netbeans.spi.debugger.ui.DebuggingView.Deadlock;

/**
 *
 * @author Martin Entlicher
 */
public class DVSupportContextAware extends DVSupport implements ContextAwareService<DVSupport>  {

    private final String serviceName;
    private ContextProvider context;
    private DVSupport delegate;
    
    private DVSupportContextAware(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public DVSupport forContext(ContextProvider context) {
        if (context == this.context) {
            return this;
        } else {
            return (DVSupport) ContextAwareSupport.createInstance(serviceName, context);
        }
    }
    
    // A dummy impl. follows:
    
    @Override
    public STATE getState() {
        return STATE.DISCONNECTED;
    }

    @Override
    public List<DVThread> getAllThreads() {
        return Collections.emptyList();
    }

    @Override
    public DVThread getCurrentThread() {
        return null;
    }

    @Override
    public String getDisplayName(DVThread thread) {
        return "";
    }

    @Override
    public Image getIcon(DVThread thread) {
        return null;
    }

    @Override
    public Session getSession() {
        return null;
    }

    @Override
    public void resume() {
    }

    @Override
    public Set<Deadlock> getDeadlocks() {
        return Collections.emptySet();
    }

    @Override
    protected List<DVFilter> getFilters() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return super.toString() + " with service = "+serviceName+" and delegate = "+delegate;
    }

    /**
     * Creates instance of <code>ContextAwareService</code> based on layer.xml
     * attribute values
     *
     * @param attrs attributes loaded from layer.xml
     * @return new <code>ContextAwareService</code> instance
     */
    static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
        String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
        return new DVSupportContextAware(serviceName);
    }

}
