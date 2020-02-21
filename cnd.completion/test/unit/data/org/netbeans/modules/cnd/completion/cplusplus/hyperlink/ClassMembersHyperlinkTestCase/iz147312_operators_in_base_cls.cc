/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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