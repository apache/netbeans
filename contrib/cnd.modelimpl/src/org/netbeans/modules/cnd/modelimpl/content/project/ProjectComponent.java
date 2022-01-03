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
package org.netbeans.modules.cnd.modelimpl.content.project;

import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * A common ancestor for project components that
 * 1) has key (most likely a project-based one);
 * 2) are able to put themselves into repository.
 *
 * It similar to Identifiable, but doesn't involve UIDs:
 * UIDs are unnecessary for such internal components as different project parts.
 *
 */
public abstract class ProjectComponent implements Persistent, SelfPersistent {

    private final Key key;

    public ProjectComponent(Key key) {
        CndUtils.assertTrueInConsole(key == null || key.getBehavior() == Key.Behavior.LargeAndMutable, "should be LargeAndMutable ", key);
        this.key = key;
    }

    public ProjectComponent(RepositoryDataInput in) throws IOException {
        key = KeyFactory.getDefaultFactory().readKey(in);
        if (TraceFlags.TRACE_PROJECT_COMPONENT_RW) {
            System.err.printf("< ProjectComponent: Reading %s key %s%n", this, key);
        }
    }

    public Key getKey() {
        return key;
    }

    /** conveniency shortcut */
    protected final int getUnitId() {
        return getKey().getUnitId();
    }

    public void put() {
        if (TraceFlags.TRACE_PROJECT_COMPONENT_RW) {
            System.err.printf("> ProjectComponent: store %s by key %s%n", this, key);
        }
        RepositoryUtils.put(key, this);
    }

//    private void putImpl() {
//	if( TraceFlags.TRACE_PROJECT_COMPONENT_RW ) System.err.printf("> ProjectComponent: Putting %s by key %s%n", this, key);
//	RepositoryUtils.put(key, this);
//    }
    @Override
    public void write(RepositoryDataOutput out) throws IOException {
        if (TraceFlags.TRACE_PROJECT_COMPONENT_RW) {
            System.err.printf("> ProjectComponent: Writing %s by key %s%n", this, key);
        }
        writeKey(key, out);
    }

    public static Key readKey(RepositoryDataInput in) throws IOException {
        return KeyFactory.getDefaultFactory().readKey(in);
    }

    public static void writeKey(Key key, RepositoryDataOutput out) throws IOException {
        KeyFactory.getDefaultFactory().writeKey(key, out);
    }

//    public static void setStable(Key key) {
//        Persistent p = RepositoryUtils.tryGet(key);
//        if (p != null) {
//            assert p instanceof ProjectComponent;
//            //ProjectComponent pc = (ProjectComponent) p;
//            // A workaround for #131701
//            //pc.putImpl();
//        }
//    }
}

