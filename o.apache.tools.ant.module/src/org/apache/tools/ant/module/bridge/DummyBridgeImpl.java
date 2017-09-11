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

package org.apache.tools.ant.module.bridge;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.module.AntModule;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Enumerations;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Used when the real Ant class loader cannot be initialized for some reason.
 * @author Jesse Glick
 */
final class DummyBridgeImpl implements BridgeInterface, IntrospectionHelperProxy {
    
    private final Throwable problem;
    
    public DummyBridgeImpl(Throwable problem) {
        this.problem = problem;
        AntModule.err.notify(ErrorManager.INFORMATIONAL, problem);
    }
    
    public String getAntVersion() {
        return NbBundle.getMessage(DummyBridgeImpl.class, "ERR_ant_not_loadable", problem);
    }
    
    public boolean isAnt16() {
        return false;
    }
    
    public IntrospectionHelperProxy getIntrospectionHelper(Class<?> clazz) {
        return this;
    }
    
    public Class getAttributeType(String name) {
        throw new IllegalStateException();
    }
    
    public Enumeration<String> getAttributes() {
        return Enumerations.empty();
    }
    
    public Class getElementType(String name) {
        throw new IllegalStateException();
    }
    
    public Enumeration<String> getNestedElements() {
        return Enumerations.empty();
    }
    
    public boolean supportsCharacters() {
        return false;
    }
    
    public boolean toBoolean(String val) {
        return Boolean.valueOf(val).booleanValue();
    }
    
    public String[] getEnumeratedValues(Class<?> c) {
        return null;
    }

    @Override
    public boolean run(File buildFile, List<String> targets, InputStream in, OutputWriter out, OutputWriter err, Map<String,String> properties,
            Set<? extends String> concealedProperties, int verbosity, String displayName, Runnable interestingOutputCallback, ProgressHandle handle, InputOutput io) {
        err.println(NbBundle.getMessage(DummyBridgeImpl.class, "ERR_cannot_run_target"));
        problem.printStackTrace(err);
        return false;
    }
    
    public void stop(Thread process) {
        // do nothing
    }

}
