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
 * Diff.java
 *
 * Created on February 2, 2001, 2:53 PM
 */

package org.netbeans.junit.diff;

/**
 * This interface must be implemented by any class used as file-diff facility in assertFile functions.
 * It declares two functions, which are called whenever the file comparision is required. Their meaning
 * is identical, they only differ by arguments types.
 *
 * Generally, they both take three parameters, the first two specify files being compared and the third
 * is the file, where comparision results are stored. Third paramtere can be null in case no additional
 * output except the return value is needed.
 *
 * @author Jan Becicka
 * @version 0.1
 * @see org.junit.Assert Assert class
 */
public interface Diff {
    
   /**
    * @param first first file to compare
    * @param second second file to compare
    * @param diff difference file, caller can pass null value, when results are not needed.
    * @return true iff files differ
    * @throws java.io.IOException if an I/O exception occurs
    */
    public boolean diff(final java.io.File first, final java.io.File second, java.io.File diff) throws java.io.IOException;
    
   /**
    * @param first first file to compare
    * @param second second file to compare
    * @param diff difference file, caller can pass null value, when results are not needed.
    * @return true iff files differ
    * @throws java.io.IOException if an I/O exception occurs
    */
    public boolean diff(final String first, final String second, String diff) throws java.io.IOException;
    
}
