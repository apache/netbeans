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
/*
 * OneOneFinders.java
 *
 * Created on November 18, 2004, 9:53 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface OneOneFinders extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String FINDER = "Finder";	// NOI18N

    public Finder[] getFinder();
    public Finder getFinder(int index);
    public void setFinder(Finder[] value);
    public void setFinder(int index, Finder value);
    public int addFinder(Finder value);
    public int removeFinder(Finder value);
    public int sizeFinder();
    public Finder newFinder();
}
