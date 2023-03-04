/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.model.pom.impl;

import java.util.*;
import org.netbeans.modules.maven.model.pom.*;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class DependencyImpl extends VersionablePOMComponentImpl implements Dependency {

    public DependencyImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public DependencyImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().DEPENDENCY));
    }

    // attributes

    // child elements
    @Override
    public java.util.List<Exclusion> getExclusions() {
        ModelList<Exclusion> childs = getChild(ExclusionImpl.List.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    @Override
    public void addExclusion(Exclusion exclusion) {
        ModelList<Exclusion> childs = getChild(ExclusionImpl.List.class);
        if (childs == null) {
            setChild(ExclusionImpl.List.class,
                    getModel().getPOMQNames().EXCLUSIONS.getName(),
                    getModel().getFactory().create(this, getModel().getPOMQNames().EXCLUSIONS.getQName()),
                    Collections.<Class<? extends POMComponent>>emptyList());
            childs = getChild(ExclusionImpl.List.class);
            assert childs != null;
        }
        childs.addListChild(exclusion);
    }

    @Override
    public void removeExclusion(Exclusion exclusion) {
        remove(exclusion, getModel().getPOMQNames().EXCLUSIONS.getName(), ExclusionImpl.List.class);
    }

    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getType() {
        return getChildElementText(getModel().getPOMQNames().TYPE.getQName());
    }

    @Override
    public void setType(String type) {
        setChildElementText(getModel().getPOMQNames().TYPE.getName(), type,
                getModel().getPOMQNames().TYPE.getQName());
    }

    @Override
    public String getClassifier() {
        return getChildElementText(getModel().getPOMQNames().CLASSIFIER.getQName());
    }

    @Override
    public void setClassifier(String classifier) {
        setChildElementText(getModel().getPOMQNames().CLASSIFIER.getName(), classifier,
                getModel().getPOMQNames().CLASSIFIER.getQName());
    }

    @Override
    public String getScope() {
        return getChildElementText(getModel().getPOMQNames().SCOPE.getQName());
    }

    @Override
    public void setScope(String scope) {
        setChildElementText(getModel().getPOMQNames().SCOPE.getName(), scope,
                getModel().getPOMQNames().SCOPE.getQName());
    }

    @Override
    public String getSystemPath() {
        return getChildElementText(getModel().getPOMQNames().SYSTEMPATH.getQName());
    }

    @Override
    public void setSystemPath(String systemPath) {
        setChildElementText(getModel().getPOMQNames().SYSTEMPATH.getName(), systemPath,
                getModel().getPOMQNames().SYSTEMPATH.getQName());
    }

    @Override
    public Boolean isOptional() {
        String str = getChildElementText(getModel().getPOMQNames().OPTIONAL.getQName());
        if (str != null) {
            return Boolean.valueOf(str);
        }
        return null;
    }

    @Override
    public void setOptional(Boolean optional) {
        setChildElementText(getModel().getPOMQNames().OPTIONAL.getName(),
                optional == null ? null : optional.toString(),
                getModel().getPOMQNames().OPTIONAL.getQName());
    }

    @Override
    public Exclusion findExclusionById(String groupId, String artifactId) {
        assert groupId != null;
        assert artifactId != null;
        java.util.List<Exclusion> excs = getExclusions();
        if (excs != null) {
            for (Exclusion e : excs) {
                if (groupId.equals(e.getGroupId()) && artifactId.equals(e.getArtifactId())) {
                    return e;
                }
            }
        }
        return null;
    }

    /** This class takes care of inserting new dependencies in the dependency list
     *  in a particular order, to override the Java EE API stub jars, that should be
     *  at the end of the dependency list. Cf. bugs 180767 and 181861.
     */
    public static class List extends ListImpl<Dependency> {

        // List of "dangerous" dependencies that should be kept at the end of the
        // dependency list, such as the Java EE API stub jars, to avoid order-related
        // problems. described in bugs 180767 and 181861. New dependencies should
        // be inserted before these dangerous deps.
        private static final java.util.List<String[]> DANGEROUS_DEPS = new ArrayList<String[]>();
        static {
            DANGEROUS_DEPS.add(new String[] {"javax", "javaee-web-api"});
            DANGEROUS_DEPS.add(new String[] {"javax", "javaee-api"});
            DANGEROUS_DEPS.add(new String[] {"javaee", "javaee-api"});
        }

        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().DEPENDENCY, Dependency.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().DEPENDENCIES));
        }

        @Override
        public void addListChild(Dependency child) {
            int index = findIndexForInsertion(child);
            if (index == -1) {
                super.addListChild(child);
            } else {
                insertAtIndex(childname.getQName().getLocalPart(), child, index);
            }
        }

        /** Returns a dependency container relevant to a particullar occurrence of
         * dependencies in the POM file. Subclasses should override this to provide
         * a concrete dependency container. This is for the purposes of determining
         * an index for inserting a dependency.
         * @return dependency container that we are operating on, or null if the
         * index for inserting a dependency is not important
         */
        protected DependencyContainer getDependencyContainer() {
            return null;
        }

        /**
         * Find the index in the dependencies list where the new element should be inserted.
         * This is needed in situations such as those described in issues 180767
         * and 181861, when new dependencies should be inserted before the Java EE
         * stub API classes.
         *
         * @param child what to insert
         * @return position where the new dependency should be inserted, or -1 to append to the end
         */
        private int findIndexForInsertion(Dependency child) {
            DependencyContainer dc = getDependencyContainer();
            if (dc != null) {
                java.util.List<Dependency> dangerousDeps = new ArrayList<Dependency>();
                for (String[] depSpec : DANGEROUS_DEPS) {
                    Dependency dep = dc.findDependencyById(depSpec[0], depSpec[1] , null);
                    if (dep != null) {
                        dangerousDeps.add(dep);
                    }
                }
                java.util.List<Dependency> myDeps = dc.getDependencies();
                for (int i = 0; i < myDeps.size(); i++) {
                    Dependency dep = myDeps.get(i);
                    if (dangerousDeps.contains(dep)) {
                        return i;
                    }
                }
            }
            return -1;
        }

    }


}
