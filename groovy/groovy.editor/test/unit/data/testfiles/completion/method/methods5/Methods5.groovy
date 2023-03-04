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
class Helper {
    private File ovPrivate() { return null; }
    
    File overload(String a) {
        return null;
    }
    
    public String overload(List<String> b) {
        return null;
    }
}

class Methods5 {
    File    someFile
    private ProcessBuilder newBuilder() { return null; }
        
    def setup() {
        someFile = new File("Mr.Spock")
        someFile.getParentFile().mkdirs()
        someFile.getAbsoluteFile().getParentFile().mkdirs()
        (someFile.getCanonicalFile().getParentFile()).mkdirs()
        
        
        ProcessBuilder b = new ProcessBuilder()
        b.command("good").inheritIO().command()
        Object o = ProcessBuilder.class;
        
        b.command(Arrays.asList("good", "bad")).inheritIO().command()
        o = ProcessBuilder.class;
        
        ProcessBuilder aa = new ProcessBuilder()
        aa.command "bye".substring(1)
        
        new Helper().privateNewFile()
        new Helper().overload("hello").getAbsoluteFile();
        new Helper().overload(Arrays.asList("hi")).substring(1)
    }
}

