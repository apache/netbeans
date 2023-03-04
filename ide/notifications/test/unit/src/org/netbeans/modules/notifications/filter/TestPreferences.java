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
package org.netbeans.modules.notifications.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Hector Espert
 */
public class TestPreferences extends Preferences {

    @Override
    public void put(String string, String string1) {
        if ("test_name".equalsIgnoreCase(string)) {
            assertEquals("testfilter", string1);
        } else if ("test_types_enabled".equalsIgnoreCase(string)) {
            assertEquals("default_category_warning\ndefault_category_error\ndefault_category_info", string1);
        } else {
            fail();
        }
    }

    @Override
    public String get(String string, String string1) {
        if ("test_types_enabled".equals(string)) {
            return "test_category_error";
        }
        return String.format("%s %s", string, string1);
    }

    @Override
    public void remove(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putInt(String string, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getInt(String string, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putLong(String string, long l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getLong(String string, long l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putBoolean(String string, boolean bln) {
        assertEquals("test_types", string);
        assertTrue(bln);
    }

    @Override
    public boolean getBoolean(String string, boolean bln) {
        if ("test_types".equalsIgnoreCase(string)) {
            return true;
        }
        return bln;
    }

    @Override
    public void putFloat(String string, float f) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getFloat(String string, float f) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putDouble(String string, double d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getDouble(String string, double d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putByteArray(String string, byte[] bytes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getByteArray(String string, byte[] bytes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] keys() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] childrenNames() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Preferences parent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Preferences node(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean nodeExists(String string) throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeNode() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String name() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String absolutePath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isUserNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void flush() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sync() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addNodeChangeListener(NodeChangeListener nl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener nl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exportNode(OutputStream out) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exportSubtree(OutputStream out) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
