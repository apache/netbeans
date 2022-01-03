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

namespace iz147312 {
/*
 * Example class.
 */
class MyClass {
public:
    void myMethod1() {}
};

/*
 * Simple pointer class.
 */
class MyClassPtr {
public:
    MyClassPtr(MyClass *p) : ptr(p) {}

    // Return contained pointer using method.
    MyClass *get() {
	return ptr;
    }

    // Return contained pointer using '->' operator.
    MyClass *operator->() {
	return ptr;
    }

    // Return contained pointer using '*' operator.
    MyClass &operator*() {
	return *ptr;
    }

private:
    MyClass *ptr;		// Contained pointer.
};

/*
 * Derived simple pointer class.
 */
class MyClass2Ptr : public MyClassPtr {
public:
    MyClass2Ptr(MyClass *p) : MyClassPtr(p) {}
};

/*
 * Templated pointer class.
 */
template <class T>
class MyTemplatePtr {
public:
    MyTemplatePtr(T *p) : ptr(p) {}

    // Return contained pointer using method.
    T *get() {
	return ptr;
    }

    // Return contained pointer using '->' operator.
    T *operator->() {
	return ptr;
    }

    // Return contained pointer using '*' operator.
    T &operator*() {
	return *ptr;
    }

private:
    T *ptr;		// Contained pointer.
};

/*
 * Derived templated pointer class.
 */
template <class T>
class MyTemplate2Ptr : public MyTemplatePtr<T> {
public:
    MyTemplate2Ptr(T *p) : MyTemplatePtr<T>(p) {}
};

/*
 * Main.
 */
int main(int argc, char** argv) {
    MyClass o;				// Create object.
    o.myMethod1();			// Call object method directly.

    MyClassPtr sp(&o);			// Create simple pointer to object.
    sp.get()->myMethod1();		// Call object method via get() method of simple pointer.
    sp->myMethod1();			// Call object method via '->' operator of simple pointer.
    (*sp).myMethod1();			// Call object method via '*' operator of simple pointer.

    MyTemplatePtr<MyClass> tp(&o);	// Create templated pointer to object.
    tp.get()->myMethod1();		// Call object method via get() method of templated pointer.
    tp->myMethod1();			// Call object method via '->' operator of templated pointer.
    (*tp).myMethod1();			// Call object method via '*' operator of templated pointer.

    MyClass2Ptr s2p(&o);		// Create derived simple pointer to object.
    s2p.get()->myMethod1();		// Call object method via get() method of derived simple pointer.
    s2p->myMethod1();			// FIXME: Call object method via '->' operator of derived simple pointer.
    (*s2p).myMethod1();			// FIXME: Call object method via '*' operator of derived simple pointer.

    MyTemplate2Ptr<MyClass> t2p(&o);	// Create derived templated pointer to object.
    t2p.get()->myMethod1();		// Call object method via get() method of derived templated pointer.
    t2p->myMethod1();			// FIXME: Call object method via '->' operator of derived templated pointer.
    (*t2p).myMethod1();			// FIXME: Call object method via '*' operator of derived templated pointer.

    return 0;
}
}