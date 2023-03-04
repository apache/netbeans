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

package org.netbeans.modules.java.hints.spiimpl.pm;

/**
 *
 * @author lahvac
 */
public class CopyFinderBasedBulkSearchTest extends BulkSearchTestPerformer {

    public CopyFinderBasedBulkSearchTest(String name) {
        super(name);
    }

    @Override
    protected BulkSearch createSearch() {
        return new CopyFinderBasedBulkSearch();
    }

    @Override
    protected boolean verifyIndexingData() {
        return false;
    }

    @Override
    public void testSerialization() throws Exception {
        //XXX
    }

    @Override
    public void testFrequencies() throws Exception {
        //XXX: serialization is a prerequisite
    }

    @Override
    public void testPatternEncodingAndIdentifiers() throws Exception {
        //XXX
    }

    @Override
    public void testNoExponentialTimeComplexity() throws Exception {
        //XXX
    }

    @Override
    public void testCheckIdentifiers2() throws Exception {
        //not critical, only improves performance on vast amounts of sources,
        //and NFA based search is used in such case anyway.
        //XXX
    }

    @Override
    public void testCheckIdentifiers3() throws Exception {
        //not critical, only improves performance on vast amounts of sources,
        //and NFA based search is used in such case anyway.
        //XXX
    }

}