/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.libs.junit4;

import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;


public final class NbJUnitProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        TreeSet<String> all = new TreeSet<>();

        // org.junit annotations
        all.add(org.junit.After.class.getCanonicalName());
        all.add(org.junit.AfterClass.class.getCanonicalName());
        all.add(org.junit.Before.class.getCanonicalName());
        all.add(org.junit.BeforeClass.class.getCanonicalName());
        all.add(org.junit.ClassRule.class.getCanonicalName());
        all.add(org.junit.FixMethodOrder.class.getCanonicalName());
        all.add(org.junit.Ignore.class.getCanonicalName());
        all.add(org.junit.Rule.class.getCanonicalName());
        all.add(org.junit.Test.class.getCanonicalName());

        return all;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
