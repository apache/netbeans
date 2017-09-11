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

package org.netbeans.spi.project;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.util.Lookup;

/**
 * Like {@link LookupProvider} but registers a single object into a project's lookup.
 * An annotated class must have one public constructor, which may take {@link Project} and/or {@link Lookup} parameters.
 * An annotated factory method must have similar parameters.
 * <pre class="nonnormative">
public final class TestAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        System.err.println("===> running action");
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            Service s = p.getLookup().lookup(Service.class);
            if (s != null) {
                System.err.println("===> got a service: " + s.m());
            } else {
                System.err.println("===> nothing for " + p);
            }
        }
    }
    public static abstract class Service {
        static {
            System.err.println("===> loading Service");
        }
        public abstract String m();
    }
    &#64;ProjectServiceProvider(service=Service.class,
                            projectType="org-netbeans-modules-java-j2seproject")
    public static class ServiceImpl extends Service {
        static {
            System.err.println("===> loading ServiceImpl");
        }
        private final Project p;
        public ServiceImpl(Project p) {
            this.p = p;
            System.err.println("===> new ServiceImpl on " + p);
        }
        public String m() {
            return ProjectUtils.getInformation(p).getDisplayName();
        }
    }
}
 * </pre>
 * <p>
 * To avoid deadlocks, stack overflows, and the like, an implementation
 * accepting a {@link Project} in its constructor (or factory method) may not
 * examine that project's lookup inside the constructor. It is fine to use the
 * project lookup from other service methods called later, typically to find
 * "sister" services (such as {@link ProjectInformation} in the example above).
 * It is also safe to accept a {@link Lookup} in the constructor
 * and examine its contents, since this is the "base lookup" supplied to
 * {@link LookupProviderSupport#createCompositeLookup}, which will not have
 * other declaratively registered services anyway.
 * </p>
 * @since org.netbeans.modules.projectapi/1 1.23
 * @see LookupProviderSupport#createCompositeLookup
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ProjectServiceProvider {

    /**
     * Service class(es) to be registered.
     * The annotated class must be assignable to the service class(es).
     */
    Class<?>[] service();

    /**
     * Token(s) denoting one or more project types, e.g. {@code "org-netbeans-modules-java-j2seproject"}
     * {@link LookupProviderSupport#createCompositeLookup} may be used with the path {@code Projects/TYPE/Lookup}.
     */
    String[] projectType() default {};

    /**
     * Alternate registration of project types with positions.
     * You must specify either this or {@link #projectType} (or both).
     */
    ProjectType[] projectTypes() default {};

}
