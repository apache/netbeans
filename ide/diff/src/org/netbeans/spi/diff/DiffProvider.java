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

package org.netbeans.spi.diff;

import java.io.IOException;
import java.io.Reader;

//import org.openide.util.Lookup;

import org.netbeans.api.diff.Difference;

/**
 * This class represents a provider of diff algorithm. The implementing class
 * should calculate differences between two sources.
 * <p>The registered Diff Providers can be obtained via {@link org.openide.util.Lookup}
 * (e.g. you can get the default diff provider by
 *  <code>Lookup.getDefault().lookup(DiffProvider.class)</code>)
 *
 * @author  Martin Entlicher
 */
public abstract class DiffProvider extends Object {

    /*
    public static DiffProvider getDefault() {
        return (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
    }
     */
    
    /**
     * Create the differences of the content two streams.
     * @param r1 the first source
     * @param r2 the second source to be compared with the first one.
     * @return the list of differences found, instances of {@link Difference};
     *         or <code>null</code> when some error occured.
     * @throws IOException when the reading from input streams fails.
     */
    public abstract Difference[] computeDiff(Reader r1, Reader r2) throws IOException;
}
