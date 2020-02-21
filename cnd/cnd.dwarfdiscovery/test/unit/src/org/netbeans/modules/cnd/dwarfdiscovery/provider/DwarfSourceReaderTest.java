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
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.BaseDwarfProvider.GrepEntry;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitInterface;
import org.netbeans.modules.cnd.dwarfdump.CompileLineService;
import org.netbeans.modules.cnd.dwarfdump.source.SourceFile;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.Dwarf.CompilationUnitIterator;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 */
public class DwarfSourceReaderTest extends NbTestCase {

    public DwarfSourceReaderTest() {
        super("DwarfSourceReaderTest");
        Logger.getLogger("cnd.logger").setLevel(Level.SEVERE);
        DwarfSource.LOG.setLevel(Level.ALL);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testDllReader(){
        File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/provider/echo";
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            for(String dll : dump.readPubNames().getDlls()) {
                assertEquals(dll, "libc.so.1"); // NOI18N
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (WrongFileFormatException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
    }
    
    public void testGcc47(){
        TreeMap<String, String> golden = new TreeMap<String, String>();
        golden.put("EXT", "\"ExternalClass.h\"");
        golden.put("FOO", "foo()");
        golden.put("IMPL", "ImplClass");
        golden.put("MA", "main");
        golden.put("QQ", "namespace qq { namespace in {");
        golden.put("QQ_CLOSE", "}}");
        golden.put("QUOTE(name,extension)", "<name.extension>");
        golden.put("RET(index)", "ret[index]");
        golden.put("USE", "qq::in");
        TreeMap<String, String> ignore = new TreeMap<String, String>();
        ignore.put("_FORTIFY_SOURCE", "2");
        ignore.put("_GNU_SOURCE", "1");
        ignore.put("_LP64", "1");
        ignore.put("__ATOMIC_ACQUIRE", "2");
        ignore.put("__ATOMIC_ACQ_REL", "4");
        ignore.put("__ATOMIC_CONSUME", "1");
        ignore.put("__ATOMIC_RELAXED", "0");
        ignore.put("__ATOMIC_RELEASE", "3");
        ignore.put("__ATOMIC_SEQ_CST", "5");
        ignore.put("__BIGGEST_ALIGNMENT__", "16");
        ignore.put("__BYTE_ORDER__", "__ORDER_LITTLE_ENDIAN__");
        ignore.put("__CHAR16_TYPE__", "short unsigned int");
        ignore.put("__CHAR32_TYPE__", "unsigned int");
        ignore.put("__CHAR_BIT__", "8");
        ignore.put("__DBL_DECIMAL_DIG__", "17");
        ignore.put("__DBL_DENORM_MIN__", "double(4.94065645841246544177e-324L)");
        ignore.put("__DBL_DIG__", "15");
        ignore.put("__DBL_EPSILON__", "double(2.22044604925031308085e-16L)");
        ignore.put("__DBL_HAS_DENORM__", "1");
        ignore.put("__DBL_HAS_INFINITY__", "1");
        ignore.put("__DBL_HAS_QUIET_NAN__", "1");
        ignore.put("__DBL_MANT_DIG__", "53");
        ignore.put("__DBL_MAX_10_EXP__", "308");
        ignore.put("__DBL_MAX_EXP__", "1024");
        ignore.put("__DBL_MAX__", "double(1.79769313486231570815e+308L)");
        ignore.put("__DBL_MIN_10_EXP__", "(-307)");
        ignore.put("__DBL_MIN_EXP__", "(-1021)");
        ignore.put("__DBL_MIN__", "double(2.22507385850720138309e-308L)");
        ignore.put("__DEC128_EPSILON__", "1E-33DL");
        ignore.put("__DEC128_MANT_DIG__", "34");
        ignore.put("__DEC128_MAX_EXP__", "6145");
        ignore.put("__DEC128_MAX__", "9.999999999999999999999999999999999E6144DL");
        ignore.put("__DEC128_MIN_EXP__", "(-6142)");
        ignore.put("__DEC128_MIN__", "1E-6143DL");
        ignore.put("__DEC128_SUBNORMAL_MIN__", "0.000000000000000000000000000000001E-6143DL");
        ignore.put("__DEC32_EPSILON__", "1E-6DF");
        ignore.put("__DEC32_MANT_DIG__", "7");
        ignore.put("__DEC32_MAX_EXP__", "97");
        ignore.put("__DEC32_MAX__", "9.999999E96DF");
        ignore.put("__DEC32_MIN_EXP__", "(-94)");
        ignore.put("__DEC32_MIN__", "1E-95DF");
        ignore.put("__DEC32_SUBNORMAL_MIN__", "0.000001E-95DF");
        ignore.put("__DEC64_EPSILON__", "1E-15DD");
        ignore.put("__DEC64_MANT_DIG__", "16");
        ignore.put("__DEC64_MAX_EXP__", "385");
        ignore.put("__DEC64_MAX__", "9.999999999999999E384DD");
        ignore.put("__DEC64_MIN_EXP__", "(-382)");
        ignore.put("__DEC64_MIN__", "1E-383DD");
        ignore.put("__DEC64_SUBNORMAL_MIN__", "0.000000000000001E-383DD");
        ignore.put("__DECIMAL_BID_FORMAT__", "1");
        ignore.put("__DECIMAL_DIG__", "21");
        ignore.put("__DEC_EVAL_METHOD__", "2");
        ignore.put("__DEPRECATED", "1");
        ignore.put("__ELF__", "1");
        ignore.put("__EXCEPTIONS", "1");
        ignore.put("__FINITE_MATH_ONLY__", "0");
        ignore.put("__FLOAT_WORD_ORDER__", "__ORDER_LITTLE_ENDIAN__");
        ignore.put("__FLT_DECIMAL_DIG__", "9");
        ignore.put("__FLT_DENORM_MIN__", "1.40129846432481707092e-45F");
        ignore.put("__FLT_DIG__", "6");
        ignore.put("__FLT_EPSILON__", "1.19209289550781250000e-7F");
        ignore.put("__FLT_EVAL_METHOD__", "0");
        ignore.put("__FLT_HAS_DENORM__", "1");
        ignore.put("__FLT_HAS_INFINITY__", "1");
        ignore.put("__FLT_HAS_QUIET_NAN__", "1");
        ignore.put("__FLT_MANT_DIG__", "24");
        ignore.put("__FLT_MAX_10_EXP__", "38");
        ignore.put("__FLT_MAX_EXP__", "128");
        ignore.put("__FLT_MAX__", "3.40282346638528859812e+38F");
        ignore.put("__FLT_MIN_10_EXP__", "(-37)");
        ignore.put("__FLT_MIN_EXP__", "(-125)");
        ignore.put("__FLT_MIN__", "1.17549435082228750797e-38F");
        ignore.put("__FLT_RADIX__", "2");
        ignore.put("__GCC_ATOMIC_BOOL_LOCK_FREE", "2");
        ignore.put("__GCC_ATOMIC_CHAR16_T_LOCK_FREE", "2");
        ignore.put("__GCC_ATOMIC_CHAR32_T_LOCK_FREE", "2");
        ignore.put("__GCC_ATOMIC_CHAR_LOCK_FREE", "2");
        ignore.put("__GCC_ATOMIC_INT_LOCK_FREE", "2");
        ignore.put("__GCC_ATOMIC_LLONG_LOCK_FREE", "2");
        ignore.put("__GCC_ATOMIC_LONG_LOCK_FREE", "2");
        ignore.put("__GCC_ATOMIC_POINTER_LOCK_FREE", "2");
        ignore.put("__GCC_ATOMIC_SHORT_LOCK_FREE", "2");
        ignore.put("__GCC_ATOMIC_TEST_AND_SET_TRUEVAL", "1");
        ignore.put("__GCC_ATOMIC_WCHAR_T_LOCK_FREE", "2");
        ignore.put("__GCC_HAVE_DWARF2_CFI_ASM", "1");
        ignore.put("__GCC_HAVE_SYNC_COMPARE_AND_SWAP_1", "1");
        ignore.put("__GCC_HAVE_SYNC_COMPARE_AND_SWAP_2", "1");
        ignore.put("__GCC_HAVE_SYNC_COMPARE_AND_SWAP_4", "1");
        ignore.put("__GCC_HAVE_SYNC_COMPARE_AND_SWAP_8", "1");
        ignore.put("__GNUC_GNU_INLINE__", "1");
        ignore.put("__GNUC_MINOR__", "7");
        ignore.put("__GNUC_PATCHLEVEL__", "2");
        ignore.put("__GNUC__", "4");
        ignore.put("__GNUG__", "4");
        ignore.put("__GXX_ABI_VERSION", "1002");
        ignore.put("__GXX_RTTI", "1");
        ignore.put("__GXX_WEAK__", "1");
        ignore.put("__INT16_C(c)", "c");
        ignore.put("__INT16_MAX__", "32767");
        ignore.put("__INT16_TYPE__", "short int");
        ignore.put("__INT32_C(c)", "c");
        ignore.put("__INT32_MAX__", "2147483647");
        ignore.put("__INT32_TYPE__", "int");
        ignore.put("__INT64_C(c)", "c ## L");
        ignore.put("__INT64_MAX__", "9223372036854775807L");
        ignore.put("__INT64_TYPE__", "long int");
        ignore.put("__INT8_C(c)", "c");
        ignore.put("__INT8_MAX__", "127");
        ignore.put("__INT8_TYPE__", "signed char");
        ignore.put("__INTMAX_C(c)", "c ## L");
        ignore.put("__INTMAX_MAX__", "9223372036854775807L");
        ignore.put("__INTMAX_TYPE__", "long int");
        ignore.put("__INTPTR_MAX__", "9223372036854775807L");
        ignore.put("__INTPTR_TYPE__", "long int");
        ignore.put("__INT_FAST16_MAX__", "9223372036854775807L");
        ignore.put("__INT_FAST16_TYPE__", "long int");
        ignore.put("__INT_FAST32_MAX__", "9223372036854775807L");
        ignore.put("__INT_FAST32_TYPE__", "long int");
        ignore.put("__INT_FAST64_MAX__", "9223372036854775807L");
        ignore.put("__INT_FAST64_TYPE__", "long int");
        ignore.put("__INT_FAST8_MAX__", "127");
        ignore.put("__INT_FAST8_TYPE__", "signed char");
        ignore.put("__INT_LEAST16_MAX__", "32767");
        ignore.put("__INT_LEAST16_TYPE__", "short int");
        ignore.put("__INT_LEAST32_MAX__", "2147483647");
        ignore.put("__INT_LEAST32_TYPE__", "int");
        ignore.put("__INT_LEAST64_MAX__", "9223372036854775807L");
        ignore.put("__INT_LEAST64_TYPE__", "long int");
        ignore.put("__INT_LEAST8_MAX__", "127");
        ignore.put("__INT_LEAST8_TYPE__", "signed char");
        ignore.put("__INT_MAX__", "2147483647");
        ignore.put("__LDBL_DENORM_MIN__", "3.64519953188247460253e-4951L");
        ignore.put("__LDBL_DIG__", "18");
        ignore.put("__LDBL_EPSILON__", "1.08420217248550443401e-19L");
        ignore.put("__LDBL_HAS_DENORM__", "1");
        ignore.put("__LDBL_HAS_INFINITY__", "1");
        ignore.put("__LDBL_HAS_QUIET_NAN__", "1");
        ignore.put("__LDBL_MANT_DIG__", "64");
        ignore.put("__LDBL_MAX_10_EXP__", "4932");
        ignore.put("__LDBL_MAX_EXP__", "16384");
        ignore.put("__LDBL_MAX__", "1.18973149535723176502e+4932L");
        ignore.put("__LDBL_MIN_10_EXP__", "(-4931)");
        ignore.put("__LDBL_MIN_EXP__", "(-16381)");
        ignore.put("__LDBL_MIN__", "3.36210314311209350626e-4932L");
        ignore.put("__LONG_LONG_MAX__", "9223372036854775807LL");
        ignore.put("__LONG_MAX__", "9223372036854775807L");
        ignore.put("__LP64__", "1");
        ignore.put("__MMX__", "1");
        ignore.put("__NO_INLINE__", "1");
        ignore.put("__ORDER_BIG_ENDIAN__", "4321");
        ignore.put("__ORDER_LITTLE_ENDIAN__", "1234");
        ignore.put("__ORDER_PDP_ENDIAN__", "3412");
        ignore.put("__PRAGMA_REDEFINE_EXTNAME", "1");
        ignore.put("__PTRDIFF_MAX__", "9223372036854775807L");
        ignore.put("__PTRDIFF_TYPE__", "long int");
        ignore.put("__REGISTER_PREFIX__", "");
        ignore.put("__SCHAR_MAX__", "127");
        ignore.put("__SHRT_MAX__", "32767");
        ignore.put("__SIG_ATOMIC_MAX__", "2147483647");
        ignore.put("__SIG_ATOMIC_MIN__", "(-__SIG_ATOMIC_MAX__ - 1)");
        ignore.put("__SIG_ATOMIC_TYPE__", "int");
        ignore.put("__SIZEOF_DOUBLE__", "8");
        ignore.put("__SIZEOF_FLOAT__", "4");
        ignore.put("__SIZEOF_INT128__", "16");
        ignore.put("__SIZEOF_INT__", "4");
        ignore.put("__SIZEOF_LONG_DOUBLE__", "16");
        ignore.put("__SIZEOF_LONG_LONG__", "8");
        ignore.put("__SIZEOF_LONG__", "8");
        ignore.put("__SIZEOF_POINTER__", "8");
        ignore.put("__SIZEOF_PTRDIFF_T__", "8");
        ignore.put("__SIZEOF_SHORT__", "2");
        ignore.put("__SIZEOF_SIZE_T__", "8");
        ignore.put("__SIZEOF_WCHAR_T__", "4");
        ignore.put("__SIZEOF_WINT_T__", "4");
        ignore.put("__SIZE_MAX__", "18446744073709551615UL");
        ignore.put("__SIZE_TYPE__", "long unsigned int");
        ignore.put("__SSE2_MATH__", "1");
        ignore.put("__SSE2__", "1");
        ignore.put("__SSE_MATH__", "1");
        ignore.put("__SSE__", "1");
        ignore.put("__SSP__", "1");
        ignore.put("__STDC_HOSTED__", "1");
        ignore.put("__STDC__", "1");
        ignore.put("__UINT16_C(c)", "c");
        ignore.put("__UINT16_MAX__", "65535");
        ignore.put("__UINT16_TYPE__", "short unsigned int");
        ignore.put("__UINT32_C(c)", "c ## U");
        ignore.put("__UINT32_MAX__", "4294967295U");
        ignore.put("__UINT32_TYPE__", "unsigned int");
        ignore.put("__UINT64_C(c)", "c ## UL");
        ignore.put("__UINT64_MAX__", "18446744073709551615UL");
        ignore.put("__UINT64_TYPE__", "long unsigned int");
        ignore.put("__UINT8_C(c)", "c");
        ignore.put("__UINT8_MAX__", "255");
        ignore.put("__UINT8_TYPE__", "unsigned char");
        ignore.put("__UINTMAX_C(c)", "c ## UL");
        ignore.put("__UINTMAX_MAX__", "18446744073709551615UL");
        ignore.put("__UINTMAX_TYPE__", "long unsigned int");
        ignore.put("__UINTPTR_MAX__", "18446744073709551615UL");
        ignore.put("__UINTPTR_TYPE__", "long unsigned int");
        ignore.put("__UINT_FAST16_MAX__", "18446744073709551615UL");
        ignore.put("__UINT_FAST16_TYPE__", "long unsigned int");
        ignore.put("__UINT_FAST32_MAX__", "18446744073709551615UL");
        ignore.put("__UINT_FAST32_TYPE__", "long unsigned int");
        ignore.put("__UINT_FAST64_MAX__", "18446744073709551615UL");
        ignore.put("__UINT_FAST64_TYPE__", "long unsigned int");
        ignore.put("__UINT_FAST8_MAX__", "255");
        ignore.put("__UINT_FAST8_TYPE__", "unsigned char");
        ignore.put("__UINT_LEAST16_MAX__", "65535");
        ignore.put("__UINT_LEAST16_TYPE__", "short unsigned int");
        ignore.put("__UINT_LEAST32_MAX__", "4294967295U");
        ignore.put("__UINT_LEAST32_TYPE__", "unsigned int");
        ignore.put("__UINT_LEAST64_MAX__", "18446744073709551615UL");
        ignore.put("__UINT_LEAST64_TYPE__", "long unsigned int");
        ignore.put("__UINT_LEAST8_MAX__", "255");
        ignore.put("__UINT_LEAST8_TYPE__", "unsigned char");
        ignore.put("__USER_LABEL_PREFIX__", "");
        ignore.put("__VERSION__", "\"4.7.2\"");
        ignore.put("__WCHAR_MAX__", "2147483647");
        ignore.put("__WCHAR_MIN__", "(-__WCHAR_MAX__ - 1)");
        ignore.put("__WCHAR_TYPE__", "int");
        ignore.put("__WINT_MAX__", "4294967295U");
        ignore.put("__WINT_MIN__", "0U");
        ignore.put("__WINT_TYPE__", "unsigned int");
        ignore.put("__amd64", "1");
        ignore.put("__amd64__", "1");
        ignore.put("__cplusplus", "199711L");
        ignore.put("__gnu_linux__", "1");
        ignore.put("__k8", "1");
        ignore.put("__k8__", "1");
        ignore.put("__linux", "1");
        ignore.put("__linux__", "1");
        ignore.put("__unix", "1");
        ignore.put("__unix__", "1");
        ignore.put("__x86_64", "1");
        ignore.put("__x86_64__", "1");
        ignore.put("linux", "1");
        ignore.put("unix", "1");
        List<String> system = new ArrayList<String>();
        String prefix = "/export/home/hudson/.netbeans/remote/spb-astra/asimon-SunOS-x86_64/var/tmp/alsimon-cnd-test-downloads/DiscoveryTestApplication/src";
        system.add("/usr/include");
        system.add("/usr/lib");
        Map<String,GrepEntry> grepBase = new HashMap<String, GrepEntry>();
        GrepEntry entry = new GrepEntry();
        grepBase.put("/usr/include", entry);
        entry.includes.add("/c++/4.7");
        entry.includes.add("/c++/4.7/x86_64-linux-gnu/bits");
        entry.includes.add("/x86_64-linux-gnu/bits");
        entry.includes.add("/x86_64-linux-gnu/sys");
        entry.includes.add("/x86_64-linux-gnu/gnu");
        entry = new GrepEntry();
        grepBase.put("/usr/lib", entry);
        entry.includes.add("/gcc/x86_64-linux-gnu/4.7/include");
        DwarfSource source = getDwarfSource("/org/netbeans/modules/cnd/dwarfdiscovery/provider/discoverytest_gcc47", system, ignore, grepBase, false, null);
        assertNotNull(source);
        TreeMap<String, String> map = new TreeMap<String, String>(source.getUserMacros());
        assertTrue(compareMap(map, golden));
        assertTrue(source.getUserInludePaths().contains("impl"));
        assertTrue(source.getUserInludePaths().contains("incl"));
        assertTrue(source.getUserInludePaths().contains("external"));
        printInclidePaths(source);
    }

    public void testSunStudioCompiler(){
        TreeMap<String, String> golden = new TreeMap<String, String>();
        golden.put("TEXT_DOMAIN", "\"SUNW_OST_OSCMD\"");
        golden.put("_TS_ERRNO", "null");
        golden.put("_iBCS2", "null");
        TreeMap<String, String> ignore = new TreeMap<String, String>();
        List<String> system = new ArrayList<String>();
        Map<String,GrepEntry> grepBase = new HashMap<String, GrepEntry>();
        DwarfSource source = getDwarfSource("/org/netbeans/modules/cnd/dwarfdiscovery/provider/echo", system, ignore, grepBase, false, null);
        assertNotNull(source);
        TreeMap<String, String> map = new TreeMap<String, String>(source.getUserMacros());
        assertTrue(compareMap(map, golden));
        assertTrue(source.getUserInludePaths().size()==1);
        assertEquals(source.getUserInludePaths().get(0), "/export1/sside/pomona/java_cp/wsb131/proto/root_i386/usr/include");
        printInclidePaths(source);
        List<SourceFile> list = CompileLineService.getSourceFileProperties(getDataDir().getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/provider/echo", false);
        assertEquals(1, list.size());
        SourceFile sf = list.get(0);
        assertEquals(sf.getCompilationDir(), "/export1/sside/pomona/java_cp/wsb131/usr/src/cmd/echo");
        assertEquals(sf.getSourceFileName(), "echo.c");
//      /export/opt/sunstudio/12ml/SUNWspro/prod/bin/cc -O -xspace -Xa -xildoff -errtags=yes -errwarn=%all -erroff=E_EMPTY_TRANSLATION_UNIT -erroff=E_STATEMENT_NOT_REACHED -xc99=%none -W0,-xglobalstatic -v -D_iBCS2 -DTEXT_DOMAIN='"SUNW_OST_OSCMD"' -D_TS_ERRNO -I/export1/sside/pomona/java_cp/wsb131/proto/root_i386/usr/include -Bdirect -M/export1/sside/pomona/java_cp/wsb131/usr/src/common/mapfiles/common/map.noexstk -M/export1/sside/pomona/java_cp/wsb131/usr/src/common/mapfiles/i386/map.pagealign -M/export1/sside/pomona/java_cp/wsb131/usr/src/common/mapfiles/i386/map.noexdata -L/export1/sside/pomona/java_cp/wsb131/proto/root_i386/lib -L/export1/sside/pomona/java_cp/wsb131/proto/root_i386/usr/lib -c  echo.c
        //System.err.println(sf.getCompileLine());
        map = new TreeMap<String, String>(sf.getUserMacros());
        assertTrue(compareMap(map, golden));
        assertEquals(sf.getUserPaths().get(0), "/export1/sside/pomona/java_cp/wsb131/proto/root_i386/usr/include");
        list = CompileLineService.getSourceFolderProperties(getDataDir().getAbsolutePath(), false);
        //assertEquals(8, list.size());
        int i = 0;
        for(SourceFile file : list){
            if (!file.getCommandLine().isEmpty()) {
                i++;
            }
        }
        assertEquals(14, i);
    }

    public void testLeopard(){
        if (Utilities.isWindows()) {
            return;
        }
        TreeMap<String, String> golden = new TreeMap<String, String>();
        golden.put("OBJC_NEW_PROPERTIES", "1");
        TreeMap<String, String> ignore = new TreeMap<String, String>();
        ignore.put("__APPLE_CC__", "5465");
        ignore.put("__APPLE__", "1");
        ignore.put("__CHAR_BIT__", "8");
        ignore.put("__CONSTANT_CFSTRINGS__", "1");
        ignore.put("__DBL_DENORM_MIN__", "4.9406564584124654e-324");
        ignore.put("__DBL_DIG__", "15");
        ignore.put("__DBL_EPSILON__", "2.2204460492503131e-16");
        ignore.put("__DBL_HAS_INFINITY__", "1");
        ignore.put("__DBL_HAS_QUIET_NAN__", "1");
        ignore.put("__DBL_MANT_DIG__", "53");
        ignore.put("__DBL_MAX_10_EXP__", "308");
        ignore.put("__DBL_MAX_EXP__", "1024");
        ignore.put("__DBL_MAX__", "1.7976931348623157e+308");
        ignore.put("__DBL_MIN_10_EXP__", "(-307)");
        ignore.put("__DBL_MIN_EXP__", "(-1021)");
        ignore.put("__DBL_MIN__", "2.2250738585072014e-308");
        ignore.put("__DECIMAL_DIG__", "21");
        ignore.put("__DEPRECATED", "1");
        ignore.put("__DYNAMIC__", "1");
        ignore.put("__ENVIRONMENT_MAC_OS_X_VERSION_MIN_REQUIRED__", "1050");
        ignore.put("__EXCEPTIONS", "1");
        ignore.put("__FINITE_MATH_ONLY__", "0");
        ignore.put("__FLT_DENORM_MIN__", "1.40129846e-45F");
        ignore.put("__FLT_DIG__", "6");
        ignore.put("__FLT_EPSILON__", "1.19209290e-7F");
        ignore.put("__FLT_EVAL_METHOD__", "0");
        ignore.put("__FLT_HAS_INFINITY__", "1");
        ignore.put("__FLT_HAS_QUIET_NAN__", "1");
        ignore.put("__FLT_MANT_DIG__", "24");
        ignore.put("__FLT_MAX_10_EXP__", "38");
        ignore.put("__FLT_MAX_EXP__", "128");
        ignore.put("__FLT_MAX__", "3.40282347e+38F");
        ignore.put("__FLT_MIN_10_EXP__", "(-37)");
        ignore.put("__FLT_MIN_EXP__", "(-125)");
        ignore.put("__FLT_MIN__", "1.17549435e-38F");
        ignore.put("__FLT_RADIX__", "2");
        ignore.put("__GNUC_MINOR__", "0");
        ignore.put("__GNUC_PATCHLEVEL__", "1");
        ignore.put("__GNUC__", "4");
        ignore.put("__GNUG__", "4");
        ignore.put("__GXX_ABI_VERSION", "1002");
        ignore.put("__GXX_WEAK__", "1");
        ignore.put("__INTMAX_MAX__", "9223372036854775807LL");
        ignore.put("__INTMAX_TYPE__", "long long int");
        ignore.put("__INT_MAX__", "2147483647");
        ignore.put("__LDBL_DENORM_MIN__", "3.64519953188247460253e-4951L");
        ignore.put("__LDBL_DIG__", "18");
        ignore.put("__LDBL_EPSILON__", "1.08420217248550443401e-19L");
        ignore.put("__LDBL_HAS_INFINITY__", "1");
        ignore.put("__LDBL_HAS_QUIET_NAN__", "1");
        ignore.put("__LDBL_MANT_DIG__", "64");
        ignore.put("__LDBL_MAX_10_EXP__", "4932");
        ignore.put("__LDBL_MAX_EXP__", "16384");
        ignore.put("__LDBL_MAX__", "1.18973149535723176502e+4932L");
        ignore.put("__LDBL_MIN_10_EXP__", "(-4931)");
        ignore.put("__LDBL_MIN_EXP__", "(-16381)");
        ignore.put("__LDBL_MIN__", "3.36210314311209350626e-4932L");
        ignore.put("__LITTLE_ENDIAN__", "1");
        ignore.put("__LONG_LONG_MAX__", "9223372036854775807LL");
        ignore.put("__LONG_MAX__", "2147483647L");
        ignore.put("__MACH__", "1");
        ignore.put("__MMX__", "1");
        ignore.put("__NO_INLINE__", "1");
        ignore.put("__PIC__", "1");
        ignore.put("__PTRDIFF_TYPE__", "int");
        ignore.put("__REGISTER_PREFIX__", "");
        ignore.put("__SCHAR_MAX__", "127");
        ignore.put("__SHRT_MAX__", "32767");
        ignore.put("__SIZE_TYPE__", "long unsigned int");
        ignore.put("__SSE2_MATH__", "1");
        ignore.put("__SSE2__", "1");
        ignore.put("__SSE_MATH__", "1");
        ignore.put("__SSE__", "1");
        ignore.put("__STDC_HOSTED__", "1");
        ignore.put("__UINTMAX_TYPE__", "long long unsigned int");
        ignore.put("__USER_LABEL_PREFIX__", "_");
        ignore.put("__VERSION__", "\"4.0.1 (Apple Inc. build 5465)\"");
        ignore.put("__WCHAR_MAX__", "2147483647");
        ignore.put("__WCHAR_TYPE__", "int");
        ignore.put("__WINT_TYPE__", "int");
        ignore.put("__cplusplus", "1");
        ignore.put("__i386", "1");
        ignore.put("__i386__", "1");
        ignore.put("__private_extern__", "extern");
        ignore.put("__strong", "");
        ignore.put("__weak", "");
        ignore.put("i386", "1");
        List<String> system = new ArrayList<String>();
        String prefix = "";
        system.add(prefix+"/usr/include");
        system.add(prefix+"/usr/include/c++/4.0.0");
        system.add(prefix+"/usr/include/c++/4.0.0/i686-apple-darwin9/bits");
        system.add(prefix+"/usr/include/libkern");
        system.add(prefix+"/usr/include/libkern/i386");
        system.add(prefix+"/usr/include/mach/i386");
        system.add(prefix+"/usr/lib/gcc/i686-apple-darwin9/4.0.1/include");
        Map<String,GrepEntry> grepBase = new HashMap<String, GrepEntry>();
        GrepEntry entry = new GrepEntry();
        grepBase.put(prefix+"/usr/include", entry);
        entry.includes.add("/sys");
        entry.includes.add("/machine");
        entry.includes.add("/i386");
        entry = new GrepEntry();
        grepBase.put(prefix+"/usr/include/c++/4.0.0", entry);
        entry.includes.add("/bits");
        entry.includes.add("/debug");
        entry.includes.add("/ext");
        DwarfSource source = getDwarfSource("/org/netbeans/modules/cnd/dwarfdiscovery/provider/cpu-g3-gdwarf-2.leopard.o", system, ignore, grepBase, false, null);
        assertNotNull(source);
        TreeMap<String, String> map = new TreeMap<String, String>(source.getUserMacros());
        assertTrue(compareMap(map, golden));
        assertTrue(source.getUserInludePaths().size()==1);
        assertEquals(source.getUserInludePaths().get(0), prefix+"/Users/guest/Quote_10");
        printInclidePaths(source);
    }

    public void testCygwin(){
        TreeMap<String, String> golden = new TreeMap<String, String>();
        golden.put("__CYGWIN32__", "1");
        golden.put("__CYGWIN__", "1");
        golden.put("unix", "1");
        golden.put("__unix__", "1");
        golden.put("__unix", "1");
        golden.put("AAA", "1");
        golden.put("BBB", "11");
        TreeMap<String, String> ignore = new TreeMap<String, String>();
        List<String> system = new ArrayList<String>();
        String prefix = "C:/cygwin";
        system.add(prefix+"/usr/include");
        system.add(prefix+"/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/i686-pc-cygwin/bits");
        system.add(prefix+"/lib/gcc/i686-pc-cygwin/3.4.4/include");
        system.add(prefix+"/lib/gcc/i686-pc-cygwin/3.4.4/include/c++");
        Map<String,GrepEntry> grepBase = new HashMap<String, GrepEntry>();
        GrepEntry entry = new GrepEntry();
        grepBase.put(prefix+"/usr/include", entry);
        entry.includes.add("/sys");
        entry.includes.add("/machine");
        entry.includes.add("/cygwin");
        entry = new GrepEntry();
        grepBase.put(prefix+"/lib/gcc/i686-pc-cygwin/3.4.4/include/c++", entry);
        entry.includes.add("/bits");
        entry.includes.add("/debug");
        entry.includes.add("/ext");
        DwarfSource source = getDwarfSource("/org/netbeans/modules/cnd/dwarfdiscovery/provider/quote.cygwin.o", system, ignore, grepBase, true, prefix);
        TreeMap<String, String> map = new TreeMap<String, String>(source.getUserMacros());
        assertTrue(compareMap(map, golden));
        assertTrue(source.getUserInludePaths().size()==1);
        assertEquals(source.getUserInludePaths().get(0), "C:/Documents and Settings/tester/My Documents/NetBeansProjects/Quote_1");
        printInclidePaths(source);
    }

    public void testCygwin2(){
        TreeMap<String, String> golden = new TreeMap<String, String>();
        golden.put("__CYGWIN32__", "1");
        golden.put("__CYGWIN__", "1");
        golden.put("unix", "1");
        golden.put("__unix__", "1");
        golden.put("__unix", "1");
        golden.put("HAVE_CONFIG_H", "1");
        TreeMap<String, String> ignore = new TreeMap<String, String>();
        List<String> system = new ArrayList<String>();
        String prefix = "D:/cygwin";
        system.add(prefix+"/usr/include");
        system.add(prefix+"/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/i686-pc-cygwin/bits");
        system.add(prefix+"/lib/gcc/i686-pc-cygwin/3.4.4/include");
        system.add(prefix+"/lib/gcc/i686-pc-cygwin/3.4.4/include/c++");
        Map<String,GrepEntry> grepBase = new HashMap<String, GrepEntry>();
        GrepEntry entry = new GrepEntry();
        grepBase.put(prefix+"/usr/include", entry);
        entry.includes.add("/sys");
        entry.includes.add("/machine");
        entry.includes.add("/cygwin");
        entry = new GrepEntry();
        grepBase.put(prefix+"/lib/gcc/i686-pc-cygwin/3.4.4/include/c++", entry);
        entry.includes.add("/bits");
        entry.includes.add("/debug");
        entry.includes.add("/ext");
        DwarfSource source = getDwarfSource("/org/netbeans/modules/cnd/dwarfdiscovery/provider/string.cygwin.o", system, ignore, grepBase, true, prefix);
        assertNotNull(source);
        TreeMap<String, String> map = new TreeMap<String, String>(source.getUserMacros());
        assertTrue(compareMap(map, golden));
        prefix = "D:";
        assertTrue(compareLists(source.getUserInludePaths(), new String[]{
                "../..",
                "./../../include/litesql",
                prefix+"/usr_/masha/projects/litesql_latest/litesql-0.3.2/src/library",
                prefix+"/usr_/masha/projects/litesql_latest/litesql-0.3.2",
                prefix+"/usr_/masha/projects/litesql_latest/litesql-0.3.2/include/litesql"
                }));
        printInclidePaths(source);
    }

    public void testGentoo43(){
        if (Utilities.isWindows()) {
            return;
        }
        TreeMap<String, String> golden = new TreeMap<String, String>();
        golden.put("COMMAND_LINE_MACROS_1", "1");
        golden.put("COMMAND_LINE_MACROS_2", "1");
        golden.put("SOURCE_CODE_MACROS_1", "1");
        TreeMap<String, String> ignore = new TreeMap<String, String>();
        ignore.put("_GNU_SOURCE", "1");
        ignore.put("_LP64", "1");
        ignore.put("__CHAR_BIT__", "8");
        ignore.put("__DBL_DENORM_MIN__", "4.9406564584124654e-324");
        ignore.put("__DBL_DIG__", "15");
        ignore.put("__DBL_EPSILON__", "2.2204460492503131e-16");
        ignore.put("__DBL_HAS_DENORM__", "1");
        ignore.put("__DBL_HAS_INFINITY__", "1");
        ignore.put("__DBL_HAS_QUIET_NAN__", "1");
        ignore.put("__DBL_MANT_DIG__", "53");
        ignore.put("__DBL_MAX_10_EXP__", "308");
        ignore.put("__DBL_MAX_EXP__", "1024");
        ignore.put("__DBL_MAX__", "1.7976931348623157e+308");
        ignore.put("__DBL_MIN_10_EXP__", "(-307)");
        ignore.put("__DBL_MIN_EXP__", "(-1021)");
        ignore.put("__DBL_MIN__", "2.2250738585072014e-308");
        ignore.put("__DEC128_DEN__", "0.000000000000000000000000000000001E-6143DL");
        ignore.put("__DEC128_EPSILON__", "1E-33DL");
        ignore.put("__DEC128_MANT_DIG__", "34");
        ignore.put("__DEC128_MAX_EXP__", "6144");
        ignore.put("__DEC128_MAX__", "9.999999999999999999999999999999999E6144DL");
        ignore.put("__DEC128_MIN_EXP__", "(-6143)");
        ignore.put("__DEC128_MIN__", "1E-6143DL");
        ignore.put("__DEC32_DEN__", "0.000001E-95DF");
        ignore.put("__DEC32_EPSILON__", "1E-6DF");
        ignore.put("__DEC32_MANT_DIG__", "7");
        ignore.put("__DEC32_MAX_EXP__", "96");
        ignore.put("__DEC32_MAX__", "9.999999E96DF");
        ignore.put("__DEC32_MIN_EXP__", "(-95)");
        ignore.put("__DEC32_MIN__", "1E-95DF");
        ignore.put("__DEC64_DEN__", "0.000000000000001E-383DD");
        ignore.put("__DEC64_EPSILON__", "1E-15DD");
        ignore.put("__DEC64_MANT_DIG__", "16");
        ignore.put("__DEC64_MAX_EXP__", "384");
        ignore.put("__DEC64_MAX__", "9.999999999999999E384DD");
        ignore.put("__DEC64_MIN_EXP__", "(-383)");
        ignore.put("__DEC64_MIN__", "1E-383DD");
        ignore.put("__DECIMAL_BID_FORMAT__", "1");
        ignore.put("__DECIMAL_DIG__", "21");
        ignore.put("__DEC_EVAL_METHOD__", "2");
        ignore.put("__DEPRECATED", "1");
        ignore.put("__ELF__", "1");
        ignore.put("__EXCEPTIONS", "1");
        ignore.put("__FINITE_MATH_ONLY__", "0");
        ignore.put("__FLT_DENORM_MIN__", "1.40129846e-45F");
        ignore.put("__FLT_DIG__", "6");
        ignore.put("__FLT_EPSILON__", "1.19209290e-7F");
        ignore.put("__FLT_EVAL_METHOD__", "0");
        ignore.put("__FLT_HAS_DENORM__", "1");
        ignore.put("__FLT_HAS_INFINITY__", "1");
        ignore.put("__FLT_HAS_QUIET_NAN__", "1");
        ignore.put("__FLT_MANT_DIG__", "24");
        ignore.put("__FLT_MAX_10_EXP__", "38");
        ignore.put("__FLT_MAX_EXP__", "128");
        ignore.put("__FLT_MAX__", "3.40282347e+38F");
        ignore.put("__FLT_MIN_10_EXP__", "(-37)");
        ignore.put("__FLT_MIN_EXP__", "(-125)");
        ignore.put("__FLT_MIN__", "1.17549435e-38F");
        ignore.put("__FLT_RADIX__", "2");
        ignore.put("__GCC_HAVE_SYNC_COMPARE_AND_SWAP_1", "1");
        ignore.put("__GCC_HAVE_SYNC_COMPARE_AND_SWAP_2", "1");
        ignore.put("__GCC_HAVE_SYNC_COMPARE_AND_SWAP_4", "1");
        ignore.put("__GCC_HAVE_SYNC_COMPARE_AND_SWAP_8", "1");
        ignore.put("__GNUC_GNU_INLINE__", "1");
        ignore.put("__GNUC_MINOR__", "3");
        ignore.put("__GNUC_PATCHLEVEL__", "2");
        ignore.put("__GNUC__", "4");
        ignore.put("__GNUG__", "4");
        ignore.put("__GXX_ABI_VERSION", "1002");
        ignore.put("__GXX_RTTI", "1");
        ignore.put("__GXX_WEAK__", "1");
        ignore.put("__INTMAX_MAX__", "9223372036854775807L");
        ignore.put("__INTMAX_TYPE__", "long int");
        ignore.put("__INT_MAX__", "2147483647");
        ignore.put("__LDBL_DENORM_MIN__", "3.64519953188247460253e-4951L");
        ignore.put("__LDBL_DIG__", "18");
        ignore.put("__LDBL_EPSILON__", "1.08420217248550443401e-19L");
        ignore.put("__LDBL_HAS_DENORM__", "1");
        ignore.put("__LDBL_HAS_INFINITY__", "1");
        ignore.put("__LDBL_HAS_QUIET_NAN__", "1");
        ignore.put("__LDBL_MANT_DIG__", "64");
        ignore.put("__LDBL_MAX_10_EXP__", "4932");
        ignore.put("__LDBL_MAX_EXP__", "16384");
        ignore.put("__LDBL_MAX__", "1.18973149535723176502e+4932L");
        ignore.put("__LDBL_MIN_10_EXP__", "(-4931)");
        ignore.put("__LDBL_MIN_EXP__", "(-16381)");
        ignore.put("__LDBL_MIN__", "3.36210314311209350626e-4932L");
        ignore.put("__LONG_LONG_MAX__", "9223372036854775807LL");
        ignore.put("__LONG_MAX__", "9223372036854775807L");
        ignore.put("__LP64__", "1");
        ignore.put("__MMX__", "1");
        ignore.put("__NO_INLINE__", "1");
        ignore.put("__PTRDIFF_TYPE__", "long int");
        ignore.put("__REGISTER_PREFIX__", "");
        ignore.put("__SCHAR_MAX__", "127");
        ignore.put("__SHRT_MAX__", "32767");
        ignore.put("__SIZEOF_DOUBLE__", "8");
        ignore.put("__SIZEOF_FLOAT__", "4");
        ignore.put("__SIZEOF_INT__", "4");
        ignore.put("__SIZEOF_LONG_DOUBLE__", "16");
        ignore.put("__SIZEOF_LONG_LONG__", "8");
        ignore.put("__SIZEOF_LONG__", "8");
        ignore.put("__SIZEOF_POINTER__", "8");
        ignore.put("__SIZEOF_PTRDIFF_T__", "8");
        ignore.put("__SIZEOF_SHORT__", "2");
        ignore.put("__SIZEOF_SIZE_T__", "8");
        ignore.put("__SIZEOF_WCHAR_T__", "4");
        ignore.put("__SIZEOF_WINT_T__", "4");
        ignore.put("__SIZE_TYPE__", "long unsigned int");
        ignore.put("__SSE2_MATH__", "1");
        ignore.put("__SSE2__", "1");
        ignore.put("__SSE_MATH__", "1");
        ignore.put("__SSE__", "1");
        ignore.put("__STDC_HOSTED__", "1");
        ignore.put("__STDC__", "1");
        ignore.put("__UINTMAX_TYPE__", "long unsigned int");
        ignore.put("__USER_LABEL_PREFIX__", "");
        ignore.put("__VERSION__", "\"4.3.2\"");
        ignore.put("__WCHAR_MAX__", "2147483647");
        ignore.put("__WCHAR_TYPE__", "int");
        ignore.put("__WINT_TYPE__", "unsigned int");
        ignore.put("__amd64", "1");
        ignore.put("__amd64__", "1");
        ignore.put("__cplusplus", "1");
        ignore.put("__gnu_linux__", "1");
        ignore.put("__k8", "1");
        ignore.put("__k8__", "1");
        ignore.put("__linux", "1");
        ignore.put("__linux__", "1");
        ignore.put("__unix", "1");
        ignore.put("__unix__", "1");
        ignore.put("__x86_64", "1");
        ignore.put("__x86_64__", "1");
        ignore.put("linux", "1");
        ignore.put("unix", "1");
        List<String> system = new ArrayList<String>();
        String prefix = "";
        system.add(prefix+"/usr/include");
        system.add(prefix+"/usr/lib/gcc/x86_64-pc-linux-gnu/4.3.2/include");
        system.add(prefix+"/usr/lib/gcc/x86_64-pc-linux-gnu/4.3.2/include/g++-v4");
        system.add(prefix+"/usr/lib/gcc/x86_64-pc-linux-gnu/4.3.2/include/g++-v4/x86_64-pc-linux-gnu/bits");
        Map<String,GrepEntry> grepBase = new HashMap<String, GrepEntry>();
        GrepEntry entry = new GrepEntry();
        grepBase.put(prefix+"/usr/include", entry);
        entry.includes.add("/sys");
        entry.includes.add("/bits");
        entry.includes.add("/gnu");
        entry = new GrepEntry();
        grepBase.put(prefix+"/usr/lib/gcc/x86_64-pc-linux-gnu/4.3.2/include/g++-v4", entry);
        entry.includes.add("/bits");
        entry.includes.add("/debug");
        entry.includes.add("/ext");
        entry.includes.add("/backward");
        DwarfSource source = getDwarfSource("/org/netbeans/modules/cnd/dwarfdiscovery/provider/cpu.gentoo.4.3.o", system, ignore, grepBase, false, null);
        assertNotNull(source);
        TreeMap<String, String> map = new TreeMap<String, String>(source.getUserMacros());
        assertTrue(compareMap(map, golden));
        assertTrue(source.getUserInludePaths().size()==1);
        assertEquals(source.getUserInludePaths().get(0), prefix+"/export/home/av202691/NetBeansProjects/Quote_1");
        printInclidePaths(source);
    }

    public void testRedhat(){
        if (Utilities.isWindows()) {
            return;
        }
        TreeMap<String, String> golden = new TreeMap<String, String>();
        golden.put("HAVE_CONFIG_H", "1");
        golden.put("HTIOP_BUILD_DLL", "1");
        golden.put("PIC", "1");
        TreeMap<String, String> ignore = new TreeMap<String, String>();
        ignore.put("_GNU_SOURCE", "1");
        ignore.put("_LP64", "1");
        ignore.put("_REENTRANT", "1");
        ignore.put("__CHAR_BIT__", "8");
        ignore.put("__DBL_DENORM_MIN__", "4.9406564584124654e-324");
        ignore.put("__DBL_DIG__", "15");
        ignore.put("__DBL_EPSILON__", "2.2204460492503131e-16");
        ignore.put("__DBL_HAS_INFINITY__", "1");
        ignore.put("__DBL_HAS_QUIET_NAN__", "1");
        ignore.put("__DBL_MANT_DIG__", "53");
        ignore.put("__DBL_MAX_10_EXP__", "308");
        ignore.put("__DBL_MAX_EXP__", "1024");
        ignore.put("__DBL_MAX__", "1.7976931348623157e+308");
        ignore.put("__DBL_MIN_10_EXP__", "(-307)");
        ignore.put("__DBL_MIN_EXP__", "(-1021)");
        ignore.put("__DBL_MIN__", "2.2250738585072014e-308");
        ignore.put("__DECIMAL_DIG__", "21");
        ignore.put("__DEPRECATED", "1");
        ignore.put("__ELF__", "1");
        ignore.put("__EXCEPTIONS", "1");
        ignore.put("__FINITE_MATH_ONLY__", "0");
        ignore.put("__FLT_DENORM_MIN__", "1.40129846e-45F");
        ignore.put("__FLT_DIG__", "6");
        ignore.put("__FLT_EPSILON__", "1.19209290e-7F");
        ignore.put("__FLT_EVAL_METHOD__", "0");
        ignore.put("__FLT_HAS_INFINITY__", "1");
        ignore.put("__FLT_HAS_QUIET_NAN__", "1");
        ignore.put("__FLT_MANT_DIG__", "24");
        ignore.put("__FLT_MAX_10_EXP__", "38");
        ignore.put("__FLT_MAX_EXP__", "128");
        ignore.put("__FLT_MAX__", "3.40282347e+38F");
        ignore.put("__FLT_MIN_10_EXP__", "(-37)");
        ignore.put("__FLT_MIN_EXP__", "(-125)");
        ignore.put("__FLT_MIN__", "1.17549435e-38F");
        ignore.put("__FLT_RADIX__", "2");
        ignore.put("__GNUC_GNU_INLINE__", "1");
        ignore.put("__GNUC_MINOR__", "1");
        ignore.put("__GNUC_PATCHLEVEL__", "2");
        ignore.put("__GNUC_RH_RELEASE__", "42");
        ignore.put("__GNUC__", "4");
        ignore.put("__GNUG__", "4");
        ignore.put("__GXX_ABI_VERSION", "1002");
        ignore.put("__GXX_WEAK__", "1");
        ignore.put("__INTMAX_MAX__", "9223372036854775807L");
        ignore.put("__INTMAX_TYPE__", "long int");
        ignore.put("__INT_MAX__", "2147483647");
        ignore.put("__LDBL_DENORM_MIN__", "3.64519953188247460253e-4951L");
        ignore.put("__LDBL_DIG__", "18");
        ignore.put("__LDBL_EPSILON__", "1.08420217248550443401e-19L");
        ignore.put("__LDBL_HAS_INFINITY__", "1");
        ignore.put("__LDBL_HAS_QUIET_NAN__", "1");
        ignore.put("__LDBL_MANT_DIG__", "64");
        ignore.put("__LDBL_MAX_10_EXP__", "4932");
        ignore.put("__LDBL_MAX_EXP__", "16384");
        ignore.put("__LDBL_MAX__", "1.18973149535723176502e+4932L");
        ignore.put("__LDBL_MIN_10_EXP__", "(-4931)");
        ignore.put("__LDBL_MIN_EXP__", "(-16381)");
        ignore.put("__LDBL_MIN__", "3.36210314311209350626e-4932L");
        ignore.put("__LONG_LONG_MAX__", "9223372036854775807LL");
        ignore.put("__LONG_MAX__", "9223372036854775807L");
        ignore.put("__LP64__", "1");
        ignore.put("__MMX__", "1");
        ignore.put("__NO_INLINE__", "1");
        ignore.put("__PIC__", "1");
        ignore.put("__PTRDIFF_TYPE__", "long int");
        ignore.put("__REGISTER_PREFIX__", "");
        ignore.put("__SCHAR_MAX__", "127");
        ignore.put("__SHRT_MAX__", "32767");
        ignore.put("__SIZE_TYPE__", "long unsigned int");
        ignore.put("__SSE2_MATH__", "1");
        ignore.put("__SSE2__", "1");
        ignore.put("__SSE_MATH__", "1");
        ignore.put("__SSE__", "1");
        ignore.put("__STDC_HOSTED__", "1");
        ignore.put("__STDC__", "1");
        ignore.put("__UINTMAX_TYPE__", "long unsigned int");
        ignore.put("__USER_LABEL_PREFIX__", "");
        ignore.put("__VERSION__", "\"4.1.2 20071124 (Red Hat 4.1.2-42)\"");
        ignore.put("__WCHAR_MAX__", "2147483647");
        ignore.put("__WCHAR_TYPE__", "int");
        ignore.put("__WINT_TYPE__", "unsigned int");
        ignore.put("__amd64", "1");
        ignore.put("__amd64__", "1");
        ignore.put("__cplusplus", "1");
        ignore.put("__gnu_linux__", "1");
        ignore.put("__k8", "1");
        ignore.put("__k8__", "1");
        ignore.put("__linux", "1");
        ignore.put("__linux__", "1");
        ignore.put("__pic__", "1");
        ignore.put("__unix", "1");
        ignore.put("__unix__", "1");
        ignore.put("__x86_64", "1");
        ignore.put("__x86_64__", "1");
        ignore.put("linux", "1");
        ignore.put("unix", "1");
        List<String> system = new ArrayList<String>();
        String prefix = "";
        system.add(prefix+"/usr/include");
        system.add(prefix+"/usr/include/c++/4.1.2");
        system.add(prefix+"/usr/lib/gcc/x86_64-redhat-linux/4.1.2/include");
        system.add(prefix+"/usr/include/c++/4.1.2/x86_64-redhat-linux/bits");
        Map<String,GrepEntry> grepBase = new HashMap<String, GrepEntry>();
        GrepEntry entry = new GrepEntry();
        grepBase.put(prefix+"/usr/include", entry);
        entry.includes.add("/sys");
        entry.includes.add("/bits");
        entry.includes.add("/gnu");
        entry.includes.add("/linux");
        entry.includes.add("/asm");
        entry.includes.add("/asm-x86_64");
        entry.includes.add("/asm-generic");
        entry = new GrepEntry();
        grepBase.put(prefix+"/usr/include/c++/4.1.2", entry);
        entry.includes.add("/bits");
        entry.includes.add("/debug");
        entry.includes.add("/ext");
        GrepEntry grep =new GrepEntry();
        grep.firstMacro="HTIOP_ACCEPTOR_IMPL_CPP";
        grep.firstMacroLine=4;
        String name = "/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/TAO/orbsvcs/orbsvcs/HTIOP/HTIOP_Acceptor_Impl.cpp";
        if (Utilities.isWindows()) {
            name = "C:\\net\\dxespb04x127x81\\export\\devarea\\osprojects\\ACE_TAO\\ACE_wrappers\\TAO\\orbsvcs\\orbsvcs\\HTIOP\\HTIOP_Acceptor_Impl.cpp";
        }
        grepBase.put(name, grep);
        if (Utilities.isWindows()) {
            name = "D:\\net\\dxespb04x127x81\\export\\devarea\\osprojects\\ACE_TAO\\ACE_wrappers\\TAO\\orbsvcs\\orbsvcs\\HTIOP\\HTIOP_Acceptor_Impl.cpp";
            grepBase.put(name, grep);
        }
        DwarfSource source = getDwarfSource("/org/netbeans/modules/cnd/dwarfdiscovery/provider/x86_64-redhat-4.1.2.o", system, ignore, grepBase, false, null);
        assertNotNull(source);
        TreeMap<String, String> map = new TreeMap<String, String>(source.getUserMacros());
        assertTrue(compareMap(map, golden));
        assertTrue(compareLists(source.getUserInludePaths(), new String[]{
        "../../../../TAO/orbsvcs/orbsvcs/HTIOP",
        "../../../../TAO/../ace",
        "../../../ace",
        "../../../../TAO/../ace/os_include/sys",
        "../../../../TAO/../ace/os_include",
        "../../../../TAO/../ace/os_include/netinet",
        prefix+"/usr/include/netinet",
        "../../../../TAO/../ace/os_include/net",
        prefix+"/usr/include/net",
        "../../../../TAO/../ace/os_include/arpa",
        prefix+"/usr/include/arpa",
        "../../../../TAO/tao",
        "../../tao",
        "../../orbsvcs/orbsvcs",
        "../../../../TAO/tao/AnyTypeCode",
        "../../../../TAO/../protocols/ace/HTBP",
        prefix+"/usr/include/rpc",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/TAO/orbsvcs/orbsvcs/HTIOP",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/ace",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/build/ace",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/ace/os_include/sys",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/ace/os_include",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/ace/os_include/netinet",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/ace/os_include/net",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/ace/os_include/arpa",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/TAO/tao",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/build/TAO/tao",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/build/TAO/orbsvcs/orbsvcs",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/TAO/tao/AnyTypeCode",
        prefix+"/net/dxespb04x127x81/export/devarea/osprojects/ACE_TAO/ACE_wrappers/protocols/ace/HTBP"
        // next dirs are detected if source file is available
        //"../../../../TAO/orbsvcs",
        //"../../../../TAO/../protocols",
        //"../../../../TAO",
        //"../../../../TAO/..",
        //"../../..",
        //"../..",
        //"../../orbsvcs"
                }));
        printInclidePaths(source);
    }

    private boolean compareMap(TreeMap<String, String> result, TreeMap<String, String> golden) {
        boolean res = true;
        for (Map.Entry<String,String> entry : result.entrySet()) {
            if (golden.containsKey(entry.getKey())){
                continue;
            }
            //printError("Redundant entry ", entry);
            System.err.println("ignore.put(\""+entry.getKey()+"\", \""+entry.getValue()+"\");");
            res = false;
        }
        for (Map.Entry<String,String> entry : golden.entrySet()) {
            if (result.containsKey(entry.getKey())){
                continue;
            }
            //printError("Not found entry ", entry);
            System.err.println("golden.put(\""+entry.getKey()+"\", \""+entry.getValue()+"\");");
            res = false;
        }
        return res;
    }

    private boolean compareLists(List<String> result, String[] golden){
        Loop:for(String x : result){
            for(String g : golden) {
                if (x.equals(g)) {
                    continue Loop;
                }
            }
            System.err.println("Result:"+x+" not found in golden");
            System.err.println("Expected:");
            for(String g : golden){
                System.err.println("\t"+g);
            }
            System.err.println("Actual:");
            for(String g : result){
                System.err.println("\t"+g);
            }
            return false;
        }
        Loop:for(String g : golden) {
            for(String x : result){
                if (x.equals(g)) {
                    continue Loop;
                }
            }
            System.err.println("Golden:"+g+" not found in results");
            return false;
        }
        if (result.size() != golden.length) {
            System.err.println("Result size:"+result.size()+" not equals golden size:"+golden.length);
            return false;
        }
        return true;
    }

    private void printError(String message, Map.Entry<String,String> entry){
        if (true) {
            if (entry.getValue() == null) {
                System.err.println(message+" "+entry.getKey());
            } else {
                System.err.println(message+" "+entry.getKey()+"="+entry.getValue());
            }
        }
    }

    private void printInclidePaths(DwarfSource source){
        if (false) {
            System.err.println("User include paths for file "+source.getItemPath());
            for(String p : source.getUserInludePaths()){
                System.err.println("\t"+p);
            }
            System.err.println("System include paths for file "+source.getItemPath());
            for(String p : source.getSystemInludePaths()){
                System.err.println("\t"+p);
            }
        }
    }

    private DwarfSource getDwarfSource(String resource, final List<String> systemPath,
            final Map<String, String> ignore, final Map<String,GrepEntry> grepBase,
            final boolean isWindows, final String cygwinPath){
        File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+resource;
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            CompilationUnitIterator units = dump.iteratorCompilationUnits();
            if (units != null && units.hasNext()) {
                while (units.hasNext()) {
                    CompilationUnitInterface cu = units.next();
                    CompilerSettings settings = new CompilerSettings(new ProjectProxy() {
                        @Override
                        public boolean createSubProjects() { return false; }
                        @Override
                        public Project getProject() { return null; }
                        @Override
                        public String getMakefile() { return null; }
                        @Override
                        public String getSourceRoot() { return null; }
                        @Override
                        public String getExecutable() { return null; }
                        @Override
                        public String getWorkingFolder() { return null; }
                        @Override
                        public boolean mergeProjectProperties() { return false;}
                        @Override
                        public boolean resolveSymbolicLinks() { return false; }
                    }){
                        @Override
                        public Map<String, String> getSystemMacroDefinitions(ItemProperties.LanguageKind lang) {
                            return ignore;
                        }

                        @Override
                        public List<String> getSystemIncludePaths(ItemProperties.LanguageKind lang) {
                            return systemPath;
                        }

                        @Override
                        public CompilerFlavor getCompileFlavor() {
                            return CompilerFlavor.toFlavor("Cygwin", PlatformTypes.PLATFORM_WINDOWS);
                        }

                        @Override
                        public String getCygwinDrive() {
                            if (cygwinPath != null) {
                                return cygwinPath;
                            }
                            return super.getCygwinDrive();
                        }

                        @Override
                        public boolean isWindows() {
                            return isWindows;
                        }

                        @Override
                        protected String normalizePath(String path) {
                            //if (isWindows == Utilities.isWindows()) {
                            //    return super.normalizePath(path);
                            //}
                            path = path.replace('\\', '/');
                            while (path.indexOf("/..") > 0) {
                                int i = path.indexOf("/..");
                                String beg = path.substring(0,i);
                                String rest = path.substring(i+3);
                                if (beg.endsWith(".")) {
                                    break;
                                }
                                int j = beg.lastIndexOf('/');
                                if (j < 0) {
                                    break;
                                }
                                path = beg.substring(0,j)+rest;
                            }
                            return path;
                        }

                    };
                    DwarfSource source = new DwarfSource(cu, ItemProperties.LanguageKind.C, ItemProperties.LanguageStandard.C, settings, grepBase, null);
                    source.process(cu);
                    return source;
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (WrongFileFormatException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return null;
    }
}
