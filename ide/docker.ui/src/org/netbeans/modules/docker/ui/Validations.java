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
package org.netbeans.modules.docker.ui;

import java.util.regex.Pattern;
import org.netbeans.modules.docker.api.DockerName;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hejl
 */
public final class Validations {

    private static final Pattern CONTAINER_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_.-]+$");

    private static final Pattern REPOSITORY_PATTERN = Pattern.compile("^[a-z0-9_\\.-]+$");

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z0-9_-]+$");

    private static final Pattern FORBIDDEN_REPOSITORY_PATTERN = Pattern.compile("^[a-f0-9]{64}$");

    private static final Pattern TAG_PATTERN = Pattern.compile("^[A-Za-z0-9_\\.-]+$");

    private Validations() {
        super();
    }

    @NbBundle.Messages({
        "MSG_ErrorContainerShort=Name length must be at least 2 characters.",
        "# {0} - error token",
        "MSG_ErrorContainerCharacter=Only [a-zA-Z0-9][a-zA-Z0-9_.-] allowed in name ({0})."
    })
    public static String validateContainer(String container) {
        Parameters.notNull("container", container);

        if (container.isEmpty()) {
            return null;
        }
        if (container.length() < 2) {
            return Bundle.MSG_ErrorContainerShort();
        }
        if (!CONTAINER_PATTERN.matcher(container).matches()) {
            return Bundle.MSG_ErrorContainerCharacter(container);
        }

        return null;
    }

    // this is based on 1.6.2/remote 1.18
    // FIXME should we version it
    @NbBundle.Messages({
        "MSG_ErrorSchema=Repository must not contain schema definition.",
        "MSG_ErrorRepositoryEmpty=Repository name can't be empty.",
        "# {0} - error token",
        "MSG_ErrorRepositoryCharacter=Only [a-z0-9_.-] allowed in repository ({0}).",
        "MSG_ErrorRepository64HexBytes=Repository name can't be 64 hex characters.",
        "# {0} - error token",
        "MSG_ErrorRegistryStartEndHyphen=Registry can''t start or end with hyphen ({0}).",
        "# {0} - error token",
        "MSG_ErrorNamespaceCharacter=Only [a-z0-9_-] allowed in namespace ({0}).",
        "MSG_ErrorNamespaceShort=Namespace length must at least 2 characters.",
        "MSG_ErrorNamespaceLong=Namespace length must be at most 255 characters.",
        "# {0} - error token",
        "MSG_ErrorNamespaceStartEndHyphen=Namespace can''t start or end with hyphen ({0}).",
        "MSG_ErrorNamespaceConsecutiveHyphens=Namespace can''t contain consecutive hyphens."
    })
    public static String validateRepository(String repository) {
        Parameters.notNull("repository", repository);

        if (repository.isEmpty()) {
            return null;
        }

        // must not contain schema
        if (repository.contains("://")) { // NOI18N
            return Bundle.MSG_ErrorSchema();
        }

        DockerName parsed = DockerName.parse(repository);
        String registry = parsed.getRegistry();
        String ns = parsed.getNamespace();
        String repo = parsed.getRepository();

        if (registry != null && (registry.startsWith("-") || registry.endsWith("-"))) { // NOI18N
            return Bundle.MSG_ErrorRegistryStartEndHyphen(registry);
        }
        if (ns != null) {
            if (ns.length() < 2) {
                return Bundle.MSG_ErrorNamespaceShort();
            }
            if (ns.length() > 255) {
                return Bundle.MSG_ErrorNamespaceLong();
            }
            if (ns.startsWith("-") || ns.endsWith("-")) { // NOI18N
                return Bundle.MSG_ErrorNamespaceStartEndHyphen(ns);
            }
            if (ns.contains("--")) { // NOI18N
                return Bundle.MSG_ErrorNamespaceConsecutiveHyphens();
            }
            if (!NAMESPACE_PATTERN.matcher(ns).matches()) {
                return Bundle.MSG_ErrorNamespaceCharacter(ns);
            }
        }

        if (repo.isEmpty()) {
            return Bundle.MSG_ErrorRepositoryEmpty();
        }
        if (ns == null && FORBIDDEN_REPOSITORY_PATTERN.matcher(repo).matches()) {
            return Bundle.MSG_ErrorRepository64HexBytes();
        }
        if (!REPOSITORY_PATTERN.matcher(repo).matches()) {
            return Bundle.MSG_ErrorRepositoryCharacter(repo);
        }
        return null;
    }

    @NbBundle.Messages({
        "# {0} - error token",
        "MSG_ErrorTagCharacter=Only [A-Za-z0-9_.-] allowed in tag ({0}).",
        "MSG_ErrorTagLong=Tag length must be at most 128 characters."
    })
    public static String validateTag(String tag) {
        Parameters.notNull("tag", tag);

        if (tag.isEmpty()) {
            return null;
        }
        if (!TAG_PATTERN.matcher(tag).matches()) {
            return Bundle.MSG_ErrorTagCharacter(tag);
        }
        if (tag.length() > 128) {
            return Bundle.MSG_ErrorTagLong();
        }
        return null;
    }
}
