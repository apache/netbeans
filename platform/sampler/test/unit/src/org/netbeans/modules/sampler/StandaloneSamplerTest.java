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
package org.netbeans.modules.sampler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

public class StandaloneSamplerTest extends AbstractSamplerBase {
    private static Class<?> samplerClass;

    @BeforeClass
    public static void loadInIsolatedLoader() throws ClassNotFoundException {
        URLClassLoader l = new URLClassLoader(new URL[] {
            Sampler.class.getProtectionDomain().getCodeSource().getLocation()
        }, Sampler.class.getClassLoader().getParent());

        samplerClass = l.loadClass("org.netbeans.modules.sampler.Sampler");

        try {
            Class<?> lookupClass = l.loadClass("org.openide.util.Lookup");
            fail("It shouldn't be possible to load Lookup class with this classloader: " + lookupClass);
        } catch (ClassNotFoundException ok) {
            assertNotNull(ok);
        }
    }

    @Override
    protected Handle createManualSampler(String name) {
        try {
            Object sampler = samplerClass.getMethod("createManualSampler", String.class).invoke(null, name);
            return new ReflectionHandle(sampler);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected Handle createSampler(String name) {
        try {
            Object sampler = samplerClass.getMethod("createSampler", String.class).invoke(null, name);
            return new ReflectionHandle(sampler);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected boolean logsMessage() {
        return false;
    }

    private static final class ReflectionHandle extends Handle {
        private final Object sampler;

        private ReflectionHandle(Object sampler) {
            this.sampler = sampler;
        }

        @Override
        protected void start() {
            try {
                samplerClass.getMethod("start").invoke(sampler);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        protected void stop() {
            try {
                samplerClass.getMethod("stop").invoke(sampler);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        protected void stopAndWriteTo(DataOutputStream dos) throws IOException {
            try {
                samplerClass.getMethod("stopAndWriteTo", DataOutputStream.class).invoke(sampler, dos);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        protected void cancel() {
            try {
                samplerClass.getMethod("cancel").invoke(sampler);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
