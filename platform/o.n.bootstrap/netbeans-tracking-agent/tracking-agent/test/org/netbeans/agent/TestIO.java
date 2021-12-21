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
package org.netbeans.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import org.netbeans.agent.hooks.TrackingHooks;

/**
 *
 * @author lahvac
 */
public class TestIO {
    public static void main(String... args) throws IOException {
        TrackingAgent.install();
        File temp = File.createTempFile("test", "test");
        String tempPath = temp.getPath();
        TrackingHooks.register(new TrackingHooks() {
            @Override
            protected void checkFileWrite(String path) {
                System.err.println("checkFileWrite: " + (path.equals(tempPath) ? "TEMP-FILE" : path));
            }
            @Override
            protected void checkFileRead(String path) {
                System.err.println("checkFileRead: " + (path.equals(tempPath) ? "TEMP-FILE" : path));
            }
            @Override
            protected void checkDelete(String path) {
                System.err.println("checkDelete: " + (path.equals(tempPath) ? "TEMP-FILE" : path));
            }
        }, 0, TrackingHooks.HOOK_IO);
        System.err.println("going to write using File:");
        new FileOutputStream(temp).close();
        System.err.println("going to delete using File:");
        temp.delete();
        System.err.println("going to write using Path:");
        Files.newOutputStream(temp.toPath()).close();
        System.err.println("delete using Path:");
        Files.delete(temp.toPath());
        System.err.println("going to write using String:");
        new FileOutputStream(temp.getAbsolutePath()).close();
        System.err.println("going to read using File:");
        new FileInputStream(temp).close();
        System.err.println("going to read using Path:");
        Files.newInputStream(temp.toPath()).close();
        System.err.println("going to read using String:");
        new FileInputStream(temp.getAbsolutePath()).close();
    }
}
