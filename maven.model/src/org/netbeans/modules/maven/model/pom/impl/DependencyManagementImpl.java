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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.pom.impl;

import java.util.*;
import org.w3c.dom.Element;
import org.netbeans.modules.maven.model.pom.*;	
import org.netbeans.modules.maven.model.pom.POMComponentVisitor;	

/**
 *
 * @author mkleint
 */
public class DependencyManagementImpl extends POMComponentImpl implements DependencyManagement {

    public DependencyManagementImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public DependencyManagementImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().DEPENDENCYMANAGEMENT));
    }

    // attributes

    @Override
    public List<Dependency> getDependencies() {
        ModelList<Dependency> childs = getChild(DependencyImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addDependency(Dependency dep) {
        ModelList<Dependency> childs = getChild(DependencyImpl.List.class);
        if (childs == null) {
            setChild(DependencyImpl.List.class,
                    getModel().getPOMQNames().DEPENDENCIES.getQName().getLocalPart(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().DEPENDENCIES.getQName()),
                    Collections.<Class<? extends POMComponent>>emptyList());
            childs = getChild(DependencyImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(dep);
    }

    @Override
    public void removeDependency(Dependency dep) {
        remove(dep, getModel().getPOMQNames().DEPENDENCIES.getName(), DependencyImpl.List.class);
    }

    @Override
    public Dependency findDependencyById(String groupId, String artifactId, String classifier) {
        assert groupId != null;
        assert artifactId != null;
        List<Dependency> deps = getDependencies();
        if (deps != null) {
            for (Dependency d : deps) {
                if (groupId.equals(d.getGroupId()) && artifactId.equals(d.getArtifactId()) &&
                        (classifier == null || classifier.equals(d.getClassifier()))) {
                    return d;
                }
            }
        }
        return null;
    }


    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    public static class DMList extends DependencyImpl.List {

        public DMList(POMModel model, Element element) {
            super(model, element);
        }

        public DMList(POMModel model) {
            super(model);
        }

        @Override
        protected DependencyContainer getDependencyContainer() {
            return getModel().getProject().getDependencyManagement();
        }

    }
}
