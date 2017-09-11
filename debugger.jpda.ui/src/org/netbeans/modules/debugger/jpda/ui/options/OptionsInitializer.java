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

package org.netbeans.modules.debugger.jpda.ui.options;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.netbeans.api.debugger.Properties.Initializer;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 * Initializer of Java debugger options.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(types={org.netbeans.api.debugger.Properties.Initializer.class})
public class OptionsInitializer implements Initializer {

    private static final String CLASS_FILTERS_ALL       = "debugger.sources.class_filters.all"; // NOI18N
    private static final String CLASS_FILTERS_ENABLED   = "debugger.sources.class_filters.enabled"; // NOI18N


    public String[] getSupportedPropertyNames() {
        return new String[] {
            CLASS_FILTERS_ALL,
            CLASS_FILTERS_ENABLED,
        };
    }

    public Object getDefaultPropertyValue(String propertyName) {
        if (CLASS_FILTERS_ALL.equals(propertyName)) {
            Set allFilters = new LinkedHashSet<String>();
            fillClassFilters(allFilters, false);
            return allFilters;
        }
        if (CLASS_FILTERS_ENABLED.equals(propertyName)) {
            Set<String> enabled = new HashSet();
            fillClassFilters(enabled, true);
            return enabled;
        }
        return null;
    }

    private static void fillClassFilters(Set filters, boolean enabled) {
        filters.add(ClassLoader.class.getName());
        filters.add(StringBuffer.class.getName());
        filters.add(StringBuilder.class.getName());
        filters.add("java.lang.AbstractStringBuilder");
        filters.add("java.lang.String");
        filters.add("java.lang.invoke.ConstantCallSite");   // JDK 8 Lambda
        filters.add("java.lang.invoke.Invokers");           // JDK 8 Lambda
        filters.add("java.lang.invoke.LambdaForm*");        // JDK 8 Lambda
        filters.add("REPL.*");                              // JDK 9 JShell generated classes
        if (!enabled) {
            filters.add("sun.*");
            filters.add("sunw.*");
        }
    }

}
