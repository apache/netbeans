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

package org.apache.tools.ant.module.spi;

import java.util.Set;
import org.apache.tools.ant.module.run.LoggerTrampoline;

/**
 * Describes the structure of a task.
 * Each instance corresponds to one task or nested element in a build script.
 * @author Jesse Glick
 * @since org.apache.tools.ant.module/3 3.12
 */
public final class TaskStructure {

    static {
        LoggerTrampoline.TASK_STRUCTURE_CREATOR = new LoggerTrampoline.Creator() {
            public AntSession makeAntSession(LoggerTrampoline.AntSessionImpl impl) {
                throw new AssertionError();
            }
            public AntEvent makeAntEvent(LoggerTrampoline.AntEventImpl impl) {
                throw new AssertionError();
            }
            public TaskStructure makeTaskStructure(LoggerTrampoline.TaskStructureImpl impl) {
                return new TaskStructure(impl);
            }
        };
    }
    
    private final LoggerTrampoline.TaskStructureImpl impl;
    private TaskStructure(LoggerTrampoline.TaskStructureImpl impl) {
        this.impl = impl;
    }
    
    /**
     * Get the element name.
     * XXX precise behavior w.r.t. namespaces etc.
     * @return a name, never null
     */
    public String getName() {
        return impl.getName();
    }
    
    /**
     * Get a single attribute.
     * It will be unevaluated as configured in the script.
     * If you wish to find the actual runtime value, you may
     * use {@link AntEvent#evaluate}.
     * @param name the attribute name
     * @return the raw value of that attribute, or null
     */
    public String getAttribute(String name) {
        return impl.getAttribute(name);
    }
    
    /**
     * Get a set of all defined attribute names.
     * @return a set of names suitable for {@link #getAttribute}; may be empty but not null
     */
    public Set<String> getAttributeNames() {
        return impl.getAttributeNames();
    }
    
    /**
     * Get configured nested text.
     * It will be unevaluated as configured in the script.
     * If you wish to find the actual runtime value, you may
     * use {@link AntEvent#evaluate}.
     * @return the raw text contained in the element, or null
     */
    public String getText() {
        return impl.getText();
    }

    /**
     * Get any configured child elements.
     * @return a list of child structure elements; may be empty but not null
     */
    public TaskStructure[] getChildren() {
        return impl.getChildren();
    }
    
    @Override
    public String toString() {
        return impl.toString();
    }
    
}
