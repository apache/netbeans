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
