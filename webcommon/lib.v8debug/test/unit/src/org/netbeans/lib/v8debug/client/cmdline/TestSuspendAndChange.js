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

var changing = require('./TestTheChangingScript');

numLoops = 10;

/*
function RunLoop() {
    var toChange = "to change";
    var changed;
    changed = changing.changingFunction(toChange);
    console.log(changed);
    if (--numLoops > 0) {
        setTimeout(RunLoop, 500);
    }
}
*/
function RunLoop() {
    var n = 10000000*numLoops;
    var i = 0;
    var s = 0;
    for (i = 0; i < n; i++) {
        s = changing.sum(i);
    }
    console.log("s = "+s);
}
RunLoop();
