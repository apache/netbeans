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
package org.netbeans.spi.java.project.support.ui.templates;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Iterator;
import org.openide.filesystems.FileObject;

/**
 * Allows to extend the New Java File Wizard by project specific panels.
 * The instance of the {@link JavaFileWizardIteratorFactory} placed in the
 * project's Lookup is consulted by the New Java File Wizard for additional
 * panels.
 * @author Tomas Zezula
 * @since 1.69
 */
public interface JavaFileWizardIteratorFactory {
    /**
     * Creates an {@link Iterator} with project specific panels for given template.
     * When the created {@link Iterator} is an instance of the {@link WizardDescriptor.InstantiatingIterator}
     * the {@link WizardDescriptor.InstantiatingIterator} life cycle methods such as {@link WizardDescriptor.InstantiatingIterator#instantiate}
     * are called on proper places.
     * @param template the template to create additional panels for
     * @return the {@link Iterator} with additional panels or null when no additional
     * panels.
     */
    @CheckForNull
    Iterator<WizardDescriptor> createIterator(@NonNull FileObject template);
}
