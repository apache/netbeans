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

namespace bug251181 {
    namespace parsing_test251181 {
        struct AAA251181 {
            int boo();
        };

        int zoo251181() { 
            if (true) {
                auto x = []()->AAA251181{ 
                    return AAA251181(); 
                };
                return x().boo();
            } else {
                return []() mutable noexcept(1+1) ->AAA251181{ 
                    return AAA251181(); 
                }().boo();        
            }
        }
    }
    
    namespace overloading_test251181 {
        struct true_type251181
        {
          static constexpr bool value = true;
        };

        struct false_type251181
        {
          static constexpr bool value = false;
        };

        struct Runnable251181 {};

        template <typename T>
        class my_function251181 {};

        template<typename _Res, typename... _ArgTypes>
        class my_function251181<_Res(_ArgTypes...)> {
        public:
            template <typename T>
            my_function251181(T functor) {};
        };

        namespace Variant1_251181 {
          typedef int a_time251181;

          struct Scheduler251181 { 
              void schedule(Runnable251181 *r, bool deleteOnComplete = false);
              void schedule(my_function251181<void ()> func);
              void schedule(a_time251181 period, a_time251181 duration, Runnable251181 *r);
              void schedule(a_time251181 period, a_time251181 duration, my_function251181<void ()> f);
          };

          void foo251181() {
            Scheduler251181 scheduler;
            Runnable251181 runnable;
            scheduler.schedule(&runnable);
            scheduler.schedule([](){ return; });
            scheduler.schedule(1000, 1000, &runnable);
            scheduler.schedule(1000, 1000, [](){ return; });
          }
        }

        namespace Variant2_251181 {
          struct a_time251181 {
            a_time251181(int time) {}
          };

          struct Scheduler251181 {
              void schedule(Runnable251181 *r, bool deleteOnComplete = false);
              void schedule(my_function251181<void ()> func);
              void schedule(a_time251181 period, a_time251181 duration, Runnable251181 *r);
              void schedule(a_time251181 period, a_time251181 duration, my_function251181<void ()> f);
          };

          void foo251181() {
            Scheduler251181 scheduler;
            Runnable251181 runnable;
            scheduler.schedule(&runnable);
            scheduler.schedule([](){ return; });
            scheduler.schedule(1000, 1000, &runnable);
            scheduler.schedule(1000, 1000, [](){ return; });
          }
        }
    }
}