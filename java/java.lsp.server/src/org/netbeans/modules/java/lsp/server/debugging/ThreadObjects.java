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
package org.netbeans.modules.java.lsp.server.debugging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Object associated with a thread. The objects can be associated with a suspended
 * thread and they are pruned automatically on resume.
 *
 * @author martin
 */
public final class ThreadObjects {

    private int lastId = 1;

    /**
     * A map of thread ID to a list of object IDs associated with that thread.
     * The array of object IDs has an effective size stored in its first element
     * to allow resize by larger blocks.
     */
    private final Map<Integer, int[]> threadObjectIds = new HashMap<>();

    /**
     * Map of object IDs.
     */
    private final Map<Integer, Object> objects = new HashMap<>();

    ThreadObjects() {
    }

    public synchronized int addObject(int threadId, Object object) {
        int id = lastId++;
        objects.put(id, object);
        int[] objectIds = threadObjectIds.get(threadId);
        if (objectIds == null) {
            objectIds = new int[10];
        }
        int numIds = objectIds[0];
        numIds++;
        if (numIds >= objectIds.length) {
            int newSize = numIds + (numIds >> 1);
            objectIds = Arrays.copyOf(objectIds, newSize);
        }
        objectIds[numIds] = id;
        objectIds[0] = numIds;
        return id;
    }

    public synchronized Object getObject(int objectId) {
        return objects.get(objectId);
    }

    public synchronized void cleanObjects(int threadId) {
        int[] objectIds = threadObjectIds.remove(threadId);
        if (!threadObjectIds.isEmpty()) {
            for (int i = objectIds[0]; i > 0; i--) {
                objects.remove(objectIds[i]);
            }
        } else {
            // clean all
            objects.clear();
            lastId = 1;
        }
    }

    public synchronized void cleanAll() {
        threadObjectIds.clear();
        objects.clear();
        lastId = 1;
    }

    public synchronized int findObjectThread(int objectId) {
        for (Map.Entry<Integer, int[]> threadEntry : threadObjectIds.entrySet()) {
            int[] ids = threadEntry.getValue();
            if (Arrays.binarySearch(ids, 1, ids[0], objectId) >= 0) {
                return threadEntry.getKey();
            }
        }
        return -1;
    }
}
