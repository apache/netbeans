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

template<typename _Key, typename _Val> class pair {
public:
       _Key getKey();
       _Val getValue();
};

template<typename _Key, typename _Val> class _Rb_tree {
public:
       _Key key_OK();
       _Val val_OK();
};

template <typename _Key, typename _Tp> class MYmap {
public:
    typedef _Key                                          key_type;
    typedef pair<const _Key, _Tp>                         value_type;
    typedef _Rb_tree<key_type, value_type> _Rep_type;
    typedef typename _Rep_type::iterator               iterator;
    _Key key_BAD();
    _Tp tp_BAD();
    key_type td_key_BAD();
    value_type td_pair_BAD();
};

class A {
public:
    void foo();
};

class B {
public:
    void boo();
};

template<typename _Iterator> class My__normal_iterator {
protected:
    _Iterator _M_current;

public:
    typedef typename _Iterator::pointer pointer;

    pointer
    operator->() const {
        return _M_current;
    }
};

template<typename _Tp> class MyAllocator {
public:
    typedef _Tp* pointer;
};

template<typename _Tp, typename _Alloc = MyAllocator<_Tp> > class MyVector {
public:
    typedef _Tp value_type;
    typedef typename _Alloc::pointer pointer;
    typedef My__normal_iterator<pointer> iterator;

    void push_back(const value_type& __x);
};
