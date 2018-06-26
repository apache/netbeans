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
package org.netbeans.modules.websvc.rest.codegen.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.netbeans.api.java.source.JavaSource;


/**
 * @author (changed by)ads
 *
 */
public class Resource {

        private String name;
        private String path;
        private String desc;
        private boolean hasDefaultGet;
        private Set<String> entities;

        private JavaSource src;

        private List<Method> methodList = Collections.emptyList();

        public Resource(String name, String path, String desc) {
            this.name = name;
            this.path = path;
            this.desc = desc;
            this.methodList = new ArrayList<Method>();
        }

        public Resource(String name, String path) {
            this(name, path , name );
        }  

        public String getName() {
            return name;
        }
        
        public String getPath() {
            return path;
        }

        public String getDescription() {
            return desc;
        }

        public void setDescription(String desc) {
            this.desc = desc;
        }        

        public List<Method> getMethods() {
            return methodList;
        }

        public void addMethod(Method m) {
            if ( m != null ){
                methodList.add(m);
            }
        }
        
        public void setDefaultGet(){
            hasDefaultGet = true;
        }
        
        public boolean hasDefaultGet(){
            return hasDefaultGet;
        }
        
        public Set<String> getEntities(){
            return entities;
        }
        
        public void setEntities(Set<String> set){
            entities = set;
        }

        protected JavaSource getSource() {
            return this.src;
        }

        protected void setSource(JavaSource src) {
            this.src = src;
        }     

        @Override
        public String toString() {
            return getName()+" : "+getPath();
        }
    }