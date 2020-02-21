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
package org.netbeans.modules.cnd.toolchain.compilers;

import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class MacroNameTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testValidMacroNames() {
        Assert.assertTrue(CCCCompiler.isValidMacroName("_A_"));
        Assert.assertTrue(CCCCompiler.isValidMacroName("A()"));
        Assert.assertTrue(CCCCompiler.isValidMacroName("A(y, i)"));
        Assert.assertTrue(CCCCompiler.isValidMacroName("A1(...)"));
    }

    @Test
    public void testInvalidMacroNames() {
        Assert.assertFalse(CCCCompiler.isValidMacroName("0_A_"));
        Assert.assertFalse(CCCCompiler.isValidMacroName("A()A"));
        Assert.assertFalse(CCCCompiler.isValidMacroName("'"));
    }

    @Test
    public void testInvalidMacroDefines() {
        String[] macro = CCCCompiler.getMacro("sun");
        Assert.assertEquals(macro[0], "sun");
        Assert.assertEquals(macro[1], null);
        
        macro = CCCCompiler.getMacro("sun 1");
        Assert.assertEquals(macro[0], "sun");
        Assert.assertEquals(macro[1], "1");
        
        macro = CCCCompiler.getMacro("A(i)");
        Assert.assertEquals(macro[0], "A(i)");
        Assert.assertEquals(macro[1], null);
        
        macro = CCCCompiler.getMacro("A(i)    i#i");
        Assert.assertEquals(macro[0], "A(i)");
        Assert.assertEquals(macro[1], "i#i");
        
        macro = CCCCompiler.getMacro("A(...) qq");
        Assert.assertEquals(macro[0], "A(...)");
        Assert.assertEquals(macro[1], "qq");
    }

    @Test
    public void testCommandLine() {
        List<String> res = new ArrayList<String>();
        CCCCompiler.parseUserMacros("-DA -D -D'' -D!= -DB(...)=3", res);
        Assert.assertEquals(res.get(0), "A");
        Assert.assertEquals(res.get(1), "B(...)=3");
    }

    @Test
    public void testCommandLineARM() {
        List<String> res = new ArrayList<String>();
        CCCCompiler.parseUserMacros("Configured with: /scratch/jbrown/arm-eabi/src/gcc-4.7-2012.09/configure --build=i686-pc-linux-gnu --host=i686-mingw32 --target=arm-none-eabi --enable-threads --disable-libmudflap --disable-libssp --disable-libstdcxx-pch --enable-extra-sgxxlite-multilibs --with-gnu-as --with-gnu-ld --with-specs='%{save-temps: -fverbose-asm} -D__CS_SOURCERYGXX_MAJ__=2012 -D__CS_SOURCERYGXX_MIN__=9 -D__CS_SOURCERYGXX_REV__=63 %{O2:%{!fno-remove-local-statics: -fremove-local-statics}} %{O*:%{O|O0|O1|O2|Os:;:%{!fno-remove-local-statics: -fremove-local-statics}}}' --enable-languages=c,c++ --disable-shared --enable-lto --with-newlib --with-pkgversion='Sourcery CodeBench Lite 2012.09-63' --with-bugurl=https://support.codesourcery.com/GNUToolchain/ --disable-nls --prefix=/opt/codesourcery --with-headers=yes --with-sysroot=/opt/codesourcery/arm-none-eabi --with-build-sysroot=/scratch/jbrown/arm-eabi/install/host-i686-mingw32/arm-none-eabi --with-libiconv-prefix=/scratch/jbrown/arm-eabi/obj/pkg-2012.09-63-arm-none-eabi/arm-2012.09-63-arm-none-eabi.extras/host-libs-i686-mingw32/usr --with-gmp=/scratch/jbrown/arm-eabi/obj/pkg-2012.09-63-arm-none-eabi/arm-2012.09-63-arm-none-eabi.extras/host-libs-i686-mingw32/usr --with-mpfr=/scratch/jbrown/arm-eabi/obj/pkg-2012.09-63-arm-none-eabi/arm-2012.09-63-arm-none-eabi.extras/host-libs-i686-mingw32/usr --with-mpc=/scratch/jbrown/arm-eabi/obj/pkg-2012.09-63-arm-none-eabi/arm-2012.09-63-arm-none-eabi.extras/host-libs-i686-mingw32/usr --with-ppl=/scratch/jbrown/arm-eabi/obj/pkg-2012.09-63-arm-none-eabi/arm-2012.09-63-arm-none-eabi.extras/host-libs-i686-mingw32/usr --with-host-libstdcxx='-static-libgcc -Wl,-Bstatic,-lstdc++,-Bdynamic -lm' --with-cloog=/scratch/jbrown/arm-eabi/obj/pkg-2012.09-63-arm-none-eabi/arm-2012.09-63-arm-none-eabi.extras/host-libs-i686-mingw32/usr --with-libelf=/scratch/jbrown/arm-eabi/obj/pkg-2012.09-63-arm-none-eabi/arm-2012.09-63-arm-none-eabi.extras/host-libs-i686-mingw32/usr --disable-libgomp --disable-libitm --enable-poison-system-directories --with-build-time-tools=/scratch/jbrown/arm-eabi/obj/tools-i686-pc-linux-gnu-2012.09-63-arm-none-eabi-i686-mingw32/arm-none-eabi/bin --with-build-time-tools=/scratch/jbrown/arm-eabi/obj/tools-i686-pc-linux-gnu-2012.09-63-arm-none-eabi-i686-mingw32/arm-none-eabi/bin", res);
        CCCCompiler.parseUserMacros("COLLECT_GCC_OPTIONS='-E' '-v' '-D' '__CS_SOURCERYGXX_MAJ__=2012' '-D' '__CS_SOURCERYGXX_MIN__=9' '-D' '__CS_SOURCERYGXX_REV__=63' c:/codesourceryg++lite/bin/../libexec/gcc/arm-none-eabi/4.7.2/cc1.exe -E -quiet -v -iprefix c:\\codesourceryg++lite\\bin\\../lib/gcc/arm-none-eabi/4.7.2/ -isysroot c:\\codesourceryg++lite\\bin\\../arm-none-eabi -D__USES_INITFINI__ -D __CS_SOURCERYGXX_MAJ__=2012 -D __CS_SOURCERYGXX_MIN__=9 -D __CS_SOURCERYGXX_REV__=63 t.c", res);
        CCCCompiler.parseUserMacros("COLLECT_GCC_OPTIONS='-E' '-v' '-D' '__CS_SOURCERYGXX_MAJ__=2012' '-D' '__CS_SOURCERYGXX_MIN__=9' '-D' '__CS_SOURCERYGXX_REV__=63'", res);
        System.err.println(res);
    }
}
