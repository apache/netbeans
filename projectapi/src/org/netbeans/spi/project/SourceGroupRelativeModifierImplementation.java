/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.spi.project;

import org.netbeans.api.project.SourceGroup;

/**
 * Intermediate level for more structured projects, where the simple
 * type-based information are not sufficient to create an appropriate folder
 * structure. 
 * <p/>
 * Prototypically used in J2SE Modular projects, where tests or sources belong
 * to different modules, and it is critical to create the folder in the "correct"
 * one.
 * <p/>
 * The project can be partitioned to several (hiearchical) parts. SourceGroups for
 * certain types/hints can be created in some of those parts (see {@link SourceGroupModifierImplementation#canCreateSourceGroup}.
 * For example, java modular projects contains modules, a module may contain several places where sources are expected - these
 * form the part hierarchy. When the original SourceGroup is specific enough, the hierarchy argument may be
 * missing or can be even ignored by the modifier implementation - provided that the newly created folders have the correct
 * relationship to the original source group.
 * <p/>
 * Similar structure may be used in other types of projects. {@code projectParts} are abstract uninterpreted identifiers, so 
 * the implementation / project may choose any semantics suitable for the project type.
 * @author sdedic
 * @since 1.68
 */
public interface SourceGroupRelativeModifierImplementation {
    /**
     * Returns Modifier, which is bound to a specific location or conceptual part of the project.
     * @param existingGroup existing location or concept within the project
     * @param projectPart identifies part of the project. The meaning depends on the "existingGroup"
     * @return modifier able to create folders, or {@code null}, if the specified project part does not exist
     */
    public SourceGroupModifierImplementation    relativeTo(SourceGroup existingGroup, String... projectPart);
}
