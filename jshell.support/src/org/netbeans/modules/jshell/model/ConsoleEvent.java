/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.model;

import java.util.Collections;
import java.util.EventObject;
import java.util.List;

/**
 * Mutation event from console model. Informs that a section in console has been
 * created or updated.
 * <p/>
 * @author sdedic
 */
public final class ConsoleEvent extends EventObject {
    private final ConsoleSection theSection;
    private final List<ConsoleSection> affectedSections;
    private volatile Boolean input;
    private boolean start;
    
    public ConsoleEvent(ConsoleModel source, ConsoleSection section, boolean startStop) {
        this(source, section);
        this.start = startStop;
    }
    
    public ConsoleEvent(ConsoleModel source, ConsoleSection section) {
        super(source);
        this.theSection = section;
        this.affectedSections = null;
    }
    
    public ConsoleEvent(ConsoleModel source,List<ConsoleSection> sections) {
        super(source);
        this.affectedSections = sections;
        this.theSection = sections.isEmpty() ? null : sections.get(0);
    }
    
    public ConsoleSection getSection() {
        return theSection;
    }
    
    public List<ConsoleSection> getAffectedSections() {
        return affectedSections == null ?
                Collections.singletonList(theSection) : affectedSections;
    }
    
    public ConsoleModel getSource() {
        return (ConsoleModel)super.getSource();
    }
    
    public boolean containsInput() {
        if (input != null) {
            return input;
        }
        for (ConsoleSection s : getAffectedSections()) {
            if (s.getType().input) {
                return input = true;
            }
        }
        return input = false;
    }

    /**
     * Valid for executing event callback. True, if the execution has been started,
     * false indicates the execution finished.
     * @return 
     */
    public boolean isStart() {
        return start;
    }
}
