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
    public abstract static class Service {
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
