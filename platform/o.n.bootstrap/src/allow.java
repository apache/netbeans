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

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class allow extends SecurityManager {

    @Override
    public void checkAccept(String host, int port) {
        uninstall();
    }

    @Override
    public void checkAccess(Thread t) {
        uninstall();
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        uninstall();
    }

    @Override
    public void checkAwtEventQueueAccess() {
        uninstall();
    }

    @Override
    public void checkConnect(String host, int port) {
        uninstall();
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        uninstall();
    }

    @Override
    public void checkCreateClassLoader() {
        uninstall();
    }

    @Override
    public void checkDelete(String file) {
        uninstall();
    }

    @Override
    public void checkExec(String cmd) {
        uninstall();
    }

    @Override
    public void checkExit(int status) {
        uninstall();
    }

    @Override
    public void checkLink(String lib) {
        uninstall();
    }

    @Override
    public void checkListen(int port) {
        uninstall();
    }

    @Override
    public void checkMemberAccess(Class<?> clazz, int which) {
        uninstall();
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        uninstall();
    }

    @Override
    public void checkMulticast(InetAddress maddr, byte ttl) {
        uninstall();
    }

    @Override
    public void checkPackageAccess(String pkg) {
        uninstall();
    }

    @Override
    public void checkPackageDefinition(String pkg) {
        uninstall();
    }

    @Override
    public void checkPermission(Permission perm) {
        uninstall();
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        uninstall();
    }

    @Override
    public void checkPrintJobAccess() {
        uninstall();
    }

    @Override
    public void checkPropertiesAccess() {
        uninstall();
    }

    @Override
    public void checkPropertyAccess(String key) {
        uninstall();
    }

    @Override
    public void checkRead(FileDescriptor fd) {
        uninstall();
    }

    @Override
    public void checkRead(String file) {
        uninstall();
    }

    @Override
    public void checkRead(String file, Object context) {
        uninstall();
    }

    @Override
    public void checkSecurityAccess(String target) {
        uninstall();
    }

    @Override
    public void checkSetFactory() {
        uninstall();
    }

    @Override
    public void checkSystemClipboardAccess() {
        uninstall();
    }

    @Override
    public boolean checkTopLevelWindow(Object window) {
        uninstall();
        return true;
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        uninstall();
    }

    @Override
    public void checkWrite(String file) {
        uninstall();
    }

    private final AtomicBoolean uninstalling = new AtomicBoolean();

    private void uninstall() {
        if (uninstalling.compareAndSet(false, true)) {
            System.setSecurityManager(null);
        }
    }
}
