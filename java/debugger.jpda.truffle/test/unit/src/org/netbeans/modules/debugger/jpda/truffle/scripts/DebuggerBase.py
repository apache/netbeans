#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

class TestObject:
  def addAO(self): self.ao = "AO"
def fnc1():
  a = 20
  o = TestObject()
  o.addAO()
  arr = []
  arr = [5, 4, 3, 2, 1]

  return 30


def fnc2(n):

  n1 = n + 1
  f2 = 0
  if n1 <= 10:
    f2 = fnc2(n1) + 1

  return f2


ga = 6
fnc1()

for i in range(1, 10):

  fnc2(i)


