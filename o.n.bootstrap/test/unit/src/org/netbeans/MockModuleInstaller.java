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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2009 Sun
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

package org.netbeans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockModuleInstaller extends ModuleInstaller {

    // For examining results of what happened:
    public final List<String> actions = new ArrayList<String>();
    public final List<Object> args = new ArrayList<Object>();

    public void clear() {
        actions.clear();
        args.clear();
    }
    // For adding invalid modules:
    public final Set<Module> delinquents = new HashSet<Module>();
    // For adding modules that don't want to close:
    public final Set<Module> wontclose = new HashSet<Module>();

    public void prepare(Module m) throws InvalidException {
        if (delinquents.contains(m)) {
            throw new InvalidException(m, "not supposed to be installed");
        }
        actions.add("prepare");
        args.add(m);
    }

    public void dispose(Module m) {
        actions.add("dispose");
        args.add(m);
    }

    public void load(List<Module> modules) {
        actions.add("load");
        args.add(new ArrayList<Module>(modules));
    }

    public void unload(List<Module> modules) {
        actions.add("unload");
        args.add(new ArrayList<Module>(modules));
    }

    public boolean closing(List<Module> modules) {
        actions.add("closing");
        args.add(new ArrayList<Module>(modules));
        for (Module m : modules) {
            if (wontclose.contains(m)) {
                return false;
            }
        }
        return true;
    }

    public void close(List<Module> modules) {
        actions.add("close");
        args.add(new ArrayList<Module>(modules));
    }

}
