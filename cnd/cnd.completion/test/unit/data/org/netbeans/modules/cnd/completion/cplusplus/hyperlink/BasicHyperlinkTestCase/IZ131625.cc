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

class Container
{
public:
  struct ForwardStruct;

  bool do_cast(const Container* __dst_type,
                     ForwardStruct & __result) const;
};

struct Container::ForwardStruct
{
  int whole_details;
  int foo();
  ForwardStruct (int details_) : whole_details (details_) { }
};

bool Container::do_cast (const Container *dst_type,
                      ForwardStruct & result) const
{
  ForwardStruct result2 (result.whole_details);
  result2.foo();
  result.foo();
  return true;
}
