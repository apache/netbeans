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

template <class X, class ACE_LOCK>
class bug187272_ACE_Refcounted_Auto_Ptr
{
public:
  /// Check rep easily.
  bool operator !() const;

  /// Check rep easily.
  operator bool () const;
  
  int *rep_;
};

template<class X, class ACE_LOCK> inline
bug187272_ACE_Refcounted_Auto_Ptr<X, ACE_LOCK>::operator bool() const
{
  return this->rep_++;
}

template<class X, class ACE_LOCK> inline bool
bug187272_ACE_Refcounted_Auto_Ptr<X, ACE_LOCK>::operator !() const
{
  return this->rep_++;
}