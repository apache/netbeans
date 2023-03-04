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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openide.util.Exceptions;

public abstract class AbstractSamplerBase {

    protected abstract SamplerTest.Handle createManualSampler(String name);
    protected abstract SamplerTest.Handle createSampler(String name);
    protected abstract boolean logsMessage();

    protected static abstract class Handle {
        protected abstract void start();
        protected abstract void stop();
        protected abstract void stopAndWriteTo(DataOutputStream dos) throws IOException;
        protected abstract void cancel();
    }

    final void longRunningMethod() {
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Test of cancel method, of class Sampler.
     */
    @Test
    public void testCancel() {
        SamplerTest.Handle instance = createManualSampler("cancel");
        instance.start();
        instance.cancel();
    }

    /**
     * Test of createManualSampler method, of class Sampler.
     */
    @Test
    public void testCreateManualSampler() {
        String name = "gentest";
        SamplerTest.Handle result = createManualSampler(name);
        assertNotNull(result);
    }

    /**
     * Test of createSampler method, of class Sampler.
     */
    @Test
    public void testCreateSampler() {
        String name = "test";
        SamplerTest.Handle result = createSampler(name);
        assertNotNull(result);
    }

    /**
     * Test of stop method, of class Sampler.
     */
    @Test
    public void testStop() {
        SamplerTest.Handle instance = createManualSampler("stop");
        assertNotNull(instance);
        try (final SamplerTest.DD d = new SamplerTest.DD()) {
            instance.start();
            longRunningMethod();
            instance.stop();
            if (logsMessage()) {
                assertNotNull("Cancel message has been logged", d.logged);
            } else {
                assertNull("CLI handler doesn't use logging", d.logged);
            }
        }
    }

    /**
     * Test of stopAndWriteTo method, of class Sampler.
     */
    @Test
    public void testStopAndWriteTo() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(out)) {
            SamplerTest.Handle instance = createSampler("cancel");
            instance.start();
            instance.stopAndWriteTo(dos);
        }
        // there should no data in out, since stopAndWriteTo is
        // invoked immediately after start
        assertTrue(out.size() == 0);
    }

    /**
     * Test of stopAndWriteTo method, of class Sampler.
     */
    @Test
    public void testStopAndWriteTo1() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(out)) {
            SamplerTest.Handle instance = createSampler("cancel");
            instance.start();
            longRunningMethod();
            instance.stopAndWriteTo(dos);
        }
        // make sure we have some sampling data
        assertTrue(out.size() > 0);
    }

    public static final class DD extends Handler implements Closeable {

        private final Logger LOG = Logger.getLogger("org.openide.util.Exceptions");
        LogRecord logged;

        public DD() {
            super();
            LOG.addHandler(this);
            LOG.setLevel(Level.WARNING);
            LOG.setUseParentHandlers(false);
            setLevel(Level.WARNING);
        }

        @Override
        public void publish(LogRecord record) {
            logged = record;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
            LOG.removeHandler(this);
        }
    } // end of DD

}
