#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

def Callable

end

def typesTest

  a1 = []
  a2 = [1, 2, [3, 4]]
  b1 = true
  b2 = false
  null = nil
  i1 = 0
  i2 = 42
  i3 = 42.42
  i4 = -0.0;
  i5 = 1/i4;
  i6 = 1/0.0;
  i7 = -1/0.0;
  i8 = 0.0/0.0;
  nc = 2 + 3i
  nr = Rational(5.5)
  f = method(:Callable)
  d = Time.now
  str = "A String"
  symbol = :symbolic
  hash = {:a => 1, "b" => 2}
  i1 + i2

end

typesTest()

