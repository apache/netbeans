/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.api;

import org.openide.util.Parameters;

/**
 *
 * @author Petr Hejl
 */
public class DockerName {

    private final String registry;

    private final String namespace;

    private final String repository;

    private final String tag;

    private DockerName(String registry, String namespace, String repository, String tag) {
        this.registry = registry;
        this.namespace = namespace;
        this.repository = repository;
        this.tag = tag;
    }

    public static DockerName parse(String name) {
        Parameters.notNull("repository", name);

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name can't be empty");
        }

        String[] parts = name.split("/", 3); // NOI18N

        String registry = null;
        String ns = null;
        String repo = null;
        String tag = null;
        if (parts.length == 1) {
            repo = parts[0];
        } else if (parts.length == 2) {
            String namespace = parts[0];
            // registry host
            if (namespace.contains(".") || namespace.contains(":") || "localhost".equals(namespace)) { // NOI18N
                registry = namespace;
            } else {
                ns = namespace;
            }
            repo = parts[1];
        } else if (parts.length == 3) {
            registry = parts[0];
            ns = parts[1];
            repo = parts[2];
        }

        int index = repo.indexOf(':');
        if (index > 0) {
            tag = repo.substring(index + 1);
            repo = repo.substring(0, index);
        }

        return new DockerName(registry, ns, repo, tag);
    }

    public String getRegistry() {
        return registry;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getRepository() {
        return repository;
    }

    public String getTag() {
        return tag;
    }
}
