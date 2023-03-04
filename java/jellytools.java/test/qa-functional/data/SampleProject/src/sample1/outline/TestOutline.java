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
package sample1.outline;

/**
 * This is a sample class used in Jellytools tests
 */
public class TestOutline {

    TestOutline test;
    /** Creates a new instance of SampleClass1 */
    public TestOutline() {
    }

    public TestOutline(int depth)
    {
        if (depth > 0)
            test = new TestOutline(depth - 1);

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        TestOutline test = new TestOutline(5);

        System.out.println("Place breakpoint here");
    }

}
