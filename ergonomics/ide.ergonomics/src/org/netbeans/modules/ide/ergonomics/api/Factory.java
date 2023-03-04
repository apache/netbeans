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

package org.netbeans.modules.ide.ergonomics.api;

import java.io.IOException;
import org.netbeans.modules.ide.ergonomics.newproject.FeatureOnDemandWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/** Factories for iterators, actions and other useful elements for feature
 * on demand UI.
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>, Jirka Rechtacek <jrechtacek@netbeans.org>
 */
public final class Factory {
    private Factory() {}

    /** Creates new iterator for data provided by given file object.
     * 
     * @param fo file object describing the iterator
     * @return the Feature On Demand-ready iterator
     * @throws java.io.IOException 
     */
    public static WizardDescriptor.InstantiatingIterator newProject (FileObject fo) throws IOException {
        return FeatureOnDemandWizardIterator.newProject(fo);
    }
}
