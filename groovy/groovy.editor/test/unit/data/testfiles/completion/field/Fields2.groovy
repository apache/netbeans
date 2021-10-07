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
class Helper {
    private File builderPrivate;
    
    ProcessBuilder builderProperty;
    
}

class Fields2 {
    File    someFile
    private ProcessBuilder newBuilder() { return null; }
        
    def setup() {
        someFile = new File("Mr.Spock")
        someFile.parentFile.mkdirs()
        someFile.absoluteFile.parentFile.mkdirs()
        (someFile.canonicalFile.getParentFile).mkdirs()
        
        System.properties.get("foo")
        
        new Helper().builderProperty.inheritIO()
        
        System.err.println(Helper.class.classLoader)
    }
}

