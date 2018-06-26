/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

/**
 *
 * @author Petr Hejl
 */
public class WLMessageDestination implements MessageDestination, WLApplicationModule {

    private final String jndiName;

    private final String resourceName;

    private final Type type;

    private final File origin;

    private final boolean system;

    public WLMessageDestination(String resourceName, String jndiName, Type type,
            File origin, boolean system) {
        this.resourceName = resourceName;
        this.jndiName = jndiName;
        this.type = type;
        this.origin = origin;
        this.system = system;
    }

    // this is the JNDI name
    @Override
    public String getName() {
        return jndiName;
    }

    // the object name
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public File getOrigin() {
        return origin;
    }

    public boolean isSystem() {
        return system;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WLMessageDestination other = (WLMessageDestination) obj;
        if ((this.jndiName == null) ? (other.jndiName != null) : !this.jndiName.equals(other.jndiName)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.jndiName != null ? this.jndiName.hashCode() : 0);
        hash = 43 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
}
