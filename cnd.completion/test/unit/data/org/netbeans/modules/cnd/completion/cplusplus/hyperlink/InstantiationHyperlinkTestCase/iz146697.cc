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

template<typename _Tp>
class iz_146697_allocator {
public:
    typedef _Tp& reference;

    template<typename _Tp1>
    struct rebind {
        typedef iz_146697_allocator<_Tp1> other;
    };
};

template<typename _Tp, typename _Alloc>
struct iz_146697__Vector_base {
    typedef typename _Alloc::template rebind<_Tp>::other _Tp_alloc_type; // !OK
    //typedef typename _Alloc _Tp_alloc_type; // OK
};

template<typename _Tp, typename _Alloc = iz_146697_allocator<_Tp> >
        class iz_146697_vector : protected iz_146697__Vector_base<_Tp, _Alloc> {
public:
    typedef iz_146697__Vector_base<_Tp, _Alloc> _Base;
    typedef typename _Base::_Tp_alloc_type _Tp_alloc_type;
    typedef typename _Tp_alloc_type::reference reference;

    reference
    operator[](int __n) {
        return 0;
    }
};

class iz_146697_string {
public:
    char c;
};

typedef iz_146697_vector<iz_146697_string> iz_146697_StringList;

int iz_146697_foo() {
    iz_146697_StringList sl;
    char s = sl[0].c;
}
