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

var testReturnType = {
    isGreat: true,
    isGood1 : function () {
        return true;
    },
    isGood2 : function () {
        if(isGreate) {
            return true;
        } else {
            return false;
        }
    },
    getSimpleDescription: function() {
        return "Simple Description";
    },
    getInteger: function () {
        return 22;
    },
    getDouble: function () {
        return 22.2;
    },
    getBigNumber: function () {
        return 32e20;
    },
    simpleMix: function() {
      if(isGood()) {
          return 10;
      }  
      return "default";
    },
    getRegExp: function () {
        return /regexp/;
    }
}

formatter.println("isGood1():" + testReturnType.isGood1());
formatter.println("getSimpleDescription: " + testReturnType.getSimpleDescription());
formatter.println("getInteger: " + testReturnType.getInteger());
formatter.println("getDouble: " + testReturnType.getDouble());
formatter.println("getBigNumber: " + testReturnType.getBigNumber());


