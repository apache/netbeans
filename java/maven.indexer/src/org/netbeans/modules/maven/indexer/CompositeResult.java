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
package org.netbeans.modules.maven.indexer;

import java.util.List;
import org.netbeans.modules.maven.indexer.spi.ResultImplementation;

/**
 * Merged view over multiple {@link ResultImplementation}s.
 * 
 * @author mbien
 */
record CompositeResult<T>(List<ResultImplementation<T>> results) implements ResultImplementation<T> {

    public CompositeResult(ResultImplementation<T> first, ResultImplementation<T> second) {
        this(List.of(first, second));
    }

    @Override
    public boolean isPartial() {
        for (ResultImplementation<T> result : results) {
            if (result.isPartial()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void waitForSkipped() {
        for (ResultImplementation<T> result : results) {
            result.waitForSkipped();
        }
    }

    @Override
    public List<T> getResults() {
        return results.stream()
                      .flatMap(r -> r.getResults().stream())
                      .sorted()
                      .distinct()
                      .toList();
    }

    @Override
    public int getTotalResultCount() {
        int ret = 0;
        for (ResultImplementation<T> result : results) {
            ret += result.getTotalResultCount();
        }
        return ret;
    }

    @Override
    public int getReturnedResultCount() {
        int ret = 0;
        for (ResultImplementation<T> result : results) {
            ret += result.getReturnedResultCount();
        }
        return ret;
    }

}
