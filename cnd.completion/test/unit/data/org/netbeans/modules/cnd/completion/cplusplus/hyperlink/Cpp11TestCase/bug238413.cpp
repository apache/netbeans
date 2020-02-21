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

namespace bug238413 {
  
  namespace AAA {
      inline namespace BBB {
          inline namespace CCC {
              int foo();
          }
      }
  }

  int boo() {
      AAA::BBB::CCC::foo();
      AAA::BBB::foo();
      AAA::foo();
      return 0;
  }

  namespace a { 
      inline namespace b { 
          struct foo { 
              static int bar() { 
                  return 0; 
              } 
          }; 
      } 
  } 

  int main() { 
      return a::foo::bar(); 
  }  
 
  namespace FFF {
      inline namespace GGG {
          namespace EEE {
              struct RRR {
                  int foo();
              };
          }

          inline namespace EEE {
              void roo();
          }
      }

      int loo() {
          EEE::RRR c;
          c.foo();
          RRR c1;
          c1.foo();
          roo();
      }
  }

  int main1() {
      FFF::loo();
      return 0;
  }
}