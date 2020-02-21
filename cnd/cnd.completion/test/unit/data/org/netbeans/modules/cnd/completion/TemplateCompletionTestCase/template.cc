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

template <int i> class T1
{
public:
    int s;
    
    static int foo()
    {
        
    }
    
    int GetI()
    {
        return i;
    }
};

template <class C> class T2
{
public:
    int i;
    static int s;
};

template <int k, class C> class T3
{
public:
    int i;
    static int s;
};


int main() 
{  
    T1<1> t1;
    
     // select<Person>().one().
    
    return 0;
}

template <class T> class T4
{
public:
    T t;
};

class Person{
public:
    void method();
};

template <class T>
class DataSource {
public:
    T one() const {
        T t;
        return t;
    }
};


template <class T> DataSource<T> select() {
    DataSource<T> p;
    return p;
}
