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
package test;

public class Test {
    
    /**
     * {@snippet :
     * class HelloWorld {// @highlight substring=""
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight substring=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight substring
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight substring=
     * }
     * }
     * 
     * 
     * {@snippet :
     * class HelloWorld {// @highlight regex=""
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight regex=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight regex
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight type=""
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight type=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight type
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight type="xyz"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight type="highlighted" substring=" "
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight substring= type="highlighted" 
     * }
     * }
     */
    
    public void errorsInHighlightTag(){}
    
    /**
     * {@snippet :
     * class HelloWorld {// @replace substring="" replacement="interface"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace replacement="interface"  substring=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace substring replacement="interface" 
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace regex="" replacement="interface"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace replacement="interface" regex=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace regex replacement="interface"
     * }
     * }
     * 
     * {@snippet :
     * class Helloclass {// @replace substring="class"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace regex="/bclass/b" 
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace substring
     * }
     * }
     * 
     * {@snippet :
     * class Helloclass {// @replace regex="\bclass\b"  replacement="interface"
     * }
     * }
     * 
     * {@snippet :
     * class HelloClass {// @replace substring="Class"  replacement=""
     * }
     * }
     */
    public void errorsInReplaceTag(){}
    
    /**
     * {@snippet :
     * class HelloWorld {// @link substring="" target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link target="System#out"  substring=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link substring target="System#out" 
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link regex="" target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link target="System#out" regex=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link regex target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class Helloclass {// @link substring="class"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link regex="/bclass/b" 
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link substring
     * }
     * }
     * 
     * {@snippet :
     * class Helloclass {// @link regex="\bclass\b"  target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class HelloClass {// @link substring="Class"  target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class Helloclass {// @link substring="Class"  target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class HelloClass {// @link substring=" "  target="System#out"
     * }
     * }
     */
    public void errorsInLinkTag(){}
    
    /**
     * {@snippet :
     * class HelloClass {// @highlight substring="Hello" region
     * }
     * }
     * 
     * {@snippet :
     * class HelloClass {// @highlight substring="Hello" region=rg1
     * }
     * }
     * 
     * {@snippet :
     * class HelloClass {// @highlight substring="Hello" region=rg1
     *      final int i = 10;// @highlight substring="int" region=rg2
     * }//@end region=rg1
     * }
     * 
     * {@snippet :
     * class HelloClass {// @highlight substring="Hello" region=rg1
     *      final int i = 10;// @highlight substring="int" region=rg2
     * }//@end region=rg2
     * }
     */
    public void errorsInUnpairedRegion(){}
    
     /**
     * {@snippet :
     * class HelloClass {// @highlight substring="Hello" 
     * }//@end
     * }
     */
    public void errorsInNoRegionToEnd(){}
    
    /**
    *
    * {@snippet file=""}
    * {@snippet class=""}
    * {@snippet file="" class=""}
    * {@snippet file="" file=""}
    */
    public void errorFileEmpty() {}

    /**
    *
    * {@snippet file="HiWorld.java"}
    * {@snippet class="HiWorld"}
    * {@snippet file="HiWorld.java" class="HiWorld"}
    * {@snippet file="HiWorld.java" file="HiWorld.java"}
    */
    public void errorFileInvalid() {}

    /**
    *
    * {@snippet file="HelloWorld.java"}
    * {@snippet class="HelloWorld"}
    * {@snippet file="HiWorld.java" class="HelloWorld"}
    * {@snippet file="HelloWorld.java" file="HiWorld.java"}
    * {@snippet file="" file="HelloWorld.java"}
    */
    public void externalSnippetFile() {}

    /**
    *
    * {@snippet file="HelloWorld.java" region="test"}
    * {@snippet class="HelloWorld" region="test"}
    */
    public void errorRegionInvalid() {}

    /**
    *
    * {@snippet file="HelloWorld.java" region="example"}
    * {@snippet class="HelloWorld" region=""}
    * {@snippet class="HelloWorld" region="example"}
    * {@snippet class="HelloWorld" region="region1"}
    * 
    */
    public void testRegionValid() {}
}
