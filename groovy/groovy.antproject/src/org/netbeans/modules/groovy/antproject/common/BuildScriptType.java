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
package org.netbeans.modules.groovy.antproject.common;

import java.net.URL;

/**
 *
 * @author Martin Janicek
 */
public enum BuildScriptType {

    EJB {
        @Override
        protected String getGroovyBuildXslPath() {
            return "org/netbeans/modules/groovy/antproject/resources/groovy-build-ejb.xsl"; // NOI18N
        }
    },

    WEB {
        @Override
        protected String getGroovyBuildXslPath() {
            return "org/netbeans/modules/groovy/antproject/resources/groovy-build-web.xsl"; // NOI18N
        }
    },

    J2SE {
        @Override
        protected String getGroovyBuildXslPath() {
            return "org/netbeans/modules/groovy/antproject/resources/groovy-build-j2se.xsl"; // NOI18N
        }
    };

    public URL getStylesheet() {
        return this.getClass().getClassLoader().getResource(getGroovyBuildXslPath());
    }

    protected abstract String getGroovyBuildXslPath();

}
