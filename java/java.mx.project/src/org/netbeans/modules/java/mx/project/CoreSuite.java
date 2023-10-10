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
package org.netbeans.modules.java.mx.project;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.java.mx.project.suitepy.MxDistribution;
import org.netbeans.modules.java.mx.project.suitepy.MxImports;
import org.netbeans.modules.java.mx.project.suitepy.MxLibrary;
import org.netbeans.modules.java.mx.project.suitepy.MxLibrary.Arch;
import org.netbeans.modules.java.mx.project.suitepy.MxProject;
import org.netbeans.modules.java.mx.project.suitepy.MxSuite;

final class CoreSuite {
    private static final class MapOf<K,V> {
        private final Map<K,V> map = new HashMap<>();

        MapOf<K, V> of(K k, V v) {
            map.put(k, v);
            return this;
        }


        Map<K,V> build() {
            return map;
        }
    }

    private static <K,V> MapOf<K,V> mapOf(Class<K> keyClass, Class<V> valueClass) {
        return new MapOf<>();
    }

    private static MxSuite createMxSuite(
        String defaultLicense,
        Map<String, MxDistribution> distributions,
        MxImports imports,
        Map<String, MxLibrary> jdkLibraries,
        Map<String, MxLibrary> libraries,
        String mxversion,
        String name,
        Map<String, MxProject> projects
    ) {
        return new MxSuite() {
            @Override
            public String mxversion() {
                return mxversion;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String defaultLicense() {
                return defaultLicense;
            }

            @Override
            public Map<String, MxLibrary> libraries() {
                return libraries;
            }

            @Override
            public Map<String, MxLibrary> jdklibraries() {
                return jdkLibraries;
            }

            @Override
            public MxImports imports() {
                return imports;
            }

            @Override
            public Map<String, MxProject> projects() {
                return projects;
            }

            @Override
            public Map<String, MxDistribution> distributions() {
                return distributions;
            }
        };
    }

    private static MxDistribution createMxDistribution(
        List<String> dependencies,
        List<String> distDependencies,
        List<String> exclude,
        List<String> strip
    ) {
        return new MxDistribution() {
            @Override
            public List<String> dependencies() {
                return dependencies;
            }

            @Override
            public List<String> distDependencies() {
                return distDependencies;
            }

            @Override
            public List<String> exclude() {
                return exclude;
            }

            @Override
            public List<String> strip() {
                return strip;
            }
        };
    }

    private static MxProject createMxProject(
        List<String> annotationProcessors,
        List<String> dependencies,
        String dir,
        String javaCompliance,
        List<String> sourceDirs,
        String subDir
    ) {
        return new MxProject() {
            @Override
            public String dir() {
                return dir;
            }

            @Override
            public String subDir() {
                return subDir;
            }

            @Override
            public List<String> sourceDirs() {
                return sourceDirs;
            }

            @Override
            public List<String> dependencies() {
                return dependencies;
            }

            @Override
            public List<String> generatedDependencies() {
                return Collections.emptyList();
            }



            @Override
            public List<String> annotationProcessors() {
                return annotationProcessors;
            }

            @Override
            public String javaCompliance() {
                return javaCompliance;
            }
        };
    }

    private static MxLibrary createMxLibrary(
        List<String> dependencies,
        Map<String, MxLibrary.Arch> osArch,
        String path,
        String sha1,
        List<String> urls
    ) {
        return new MxLibrary() {
            @Override
            public String sha1() {
                return sha1;
            }

            @Override
            public List<String> urls() {
                return urls;
            }

            @Override
            public Map<String, MxLibrary.Arch> os_arch() {
                return osArch;
            }

            @Override
            public List<String> dependencies() {
                return dependencies;
            }

            @Override
            public String path() {
                return path;
            }

        };
    }

    private static MxLibrary.Arch createArch(MxLibrary amd64) {
        return new MxLibrary.Arch() {
            @Override
            public MxLibrary amd64() {
                return amd64;
            }
        };
    }

    static final MxSuite CORE_5_279_0;
    static {
        // generated by running CoreSuite.main
        CORE_5_279_0 = createMxSuite(
/* defaultLicense */null,
/* distributions */mapOf(java.lang.String.class, MxDistribution.class).of(
  "JUNIT_TOOL", createMxDistribution(
    /* dependencies */Arrays.asList(
      "com.oracle.mxtool.junit"),
    /* distDependencies */Arrays.asList(),
    /* exclude */Arrays.asList(
      "JUNIT",
      "HAMCREST"),
    /* strip */Arrays.asList()
    )).of(
  "MX_JACOCO_REPORT", createMxDistribution(
    /* dependencies */Arrays.asList(
      "com.oracle.mxtool.jacoco"),
    /* distDependencies */Arrays.asList(),
    /* exclude */Arrays.asList(),
    /* strip */Arrays.asList()
    )).of(
  "MX_MICRO_BENCHMARKS", createMxDistribution(
    /* dependencies */Arrays.asList(
      "com.oracle.mxtool.jmh_1_21"),
    /* distDependencies */Arrays.asList(),
    /* exclude */Arrays.asList(),
    /* strip */Arrays.asList()
    )).build(),
/* imports */null,
/* jdklibraries */Collections.emptyMap(),
/* libraries */mapOf(java.lang.String.class, MxLibrary.class).of(
  "APIGUARDIAN-API", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"fc9dff4bb36d627bdc553de77e1f17efd790876c",
    /* urls */Arrays.asList()
    )).of(
  "ASM_7.1", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"fa29aa438674ff19d5e1386d2c3527a0267f291e",
    /* urls */Arrays.asList()
    )).of(
  "ASM_ANALYSIS_7.1", createMxLibrary(
    /* dependencies */Arrays.asList(
      "ASM_TREE_7.1"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"379e0250f7a4a42c66c5e94e14d4c4491b3c2ed3",
    /* urls */Arrays.asList()
    )).of(
  "ASM_COMMONS_7.1", createMxLibrary(
    /* dependencies */Arrays.asList(
      "ASM_7.1",
      "ASM_TREE_7.1",
      "ASM_ANALYSIS_7.1"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"431dc677cf5c56660c1c9004870de1ed1ea7ce6c",
    /* urls */Arrays.asList()
    )).of(
  "ASM_DEBUG_ALL", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"702b8525fcf81454235e5e2fa2a35f15ffc0ec7e",
    /* urls */Arrays.asList()
    )).of(
  "ASM_TREE_7.1", createMxLibrary(
    /* dependencies */Arrays.asList(
      "ASM_7.1"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"a3662cf1c1d592893ffe08727f78db35392fa302",
    /* urls */Arrays.asList()
    )).of(
  "CHECKSTYLE_6.0", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"2bedc7feded58b5fd65595323bfaf7b9bb6a3c7a",
    /* urls */Arrays.asList(
      "https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/checkstyle-6.0-all.jar",
      "https://github.com/checkstyle/checkstyle/releases/download/checkstyle-6.0/checkstyle-6.0-all.jar")
    )).of(
  "CHECKSTYLE_6.15", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"db9ade7f4ef4ecb48e3f522873946f9b48f949ee",
    /* urls */Arrays.asList(
      "https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/checkstyle-6.15-all.jar",
      "https://github.com/checkstyle/checkstyle/releases/download/checkstyle-6.15/checkstyle-6.15-all.jar")
    )).of(
  "CHECKSTYLE_8.8", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"9712a8124c40298015f04a74f61b3d81a51513af",
    /* urls */Arrays.asList(
      "https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/checkstyle-8.8-all.jar",
      "https://github.com/checkstyle/checkstyle/releases/download/checkstyle-8.8/checkstyle-8.8-all.jar")
    )).of(
  "CODESNIPPET-DOCLET", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"688f42e00c8d013d59b9dc173e53ede9462fa906",
    /* urls */Arrays.asList()
    )).of(
  "COMMONS_MATH3_3_2", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"ec2544ab27e110d2d431bdad7d538ed509b21e62",
    /* urls */Arrays.asList()
    )).of(
  "HAMCREST", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"42a25dc3219429f0e5d060061f71acb49bf010a0",
    /* urls */Arrays.asList()
    )).of(
  "JACKPOT", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"9e945acafbdfd585d9079769098f1d78bc8e9921",
    /* urls */Arrays.asList()
    )).of(
  "JACOCOAGENT", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"a6ac9cca89d889222a40dab9dd5039bfd22a4cff",
    /* urls */Arrays.asList()
    )).of(
  "JACOCOAGENT_0.8.4", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"869021a6d90cfb008b12e83fccbe42eca29e5355",
    /* urls */Arrays.asList()
    )).of(
  "JACOCOCORE", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"66215826a684eb6866d4c14a5a4f9c344f1d1eef",
    /* urls */Arrays.asList()
    )).of(
  "JACOCOCORE_0.8.4", createMxLibrary(
    /* dependencies */Arrays.asList(
      "ASM_7.1",
      "ASM_COMMONS_7.1",
      "ASM_TREE_7.1"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"53addc878614171ff0fcbc8f78aed12175c22cdb",
    /* urls */Arrays.asList()
    )).of(
  "JACOCOREPORT", createMxLibrary(
    /* dependencies */Arrays.asList(
      "JACOCOCORE",
      "ASM_DEBUG_ALL"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"8a7f78fdf2a4e58762890d8e896a9298c2980c10",
    /* urls */Arrays.asList()
    )).of(
  "JACOCOREPORT_0.8.4", createMxLibrary(
    /* dependencies */Arrays.asList(
      "JACOCOCORE_0.8.4"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"e5ca9511493b7e3bc2cabdb8ded92e855f3aac32",
    /* urls */Arrays.asList()
    )).of(
  "JMH_1_18", createMxLibrary(
    /* dependencies */Arrays.asList(
      "JOPTSIMPLE_4_6",
      "JMH_GENERATOR_ANNPROCESS_1_18",
      "COMMONS_MATH3_3_2"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"0174aa0077e9db596e53d7f9ec37556d9392d5a6",
    /* urls */Arrays.asList()
    )).of(
  "JMH_1_21", createMxLibrary(
    /* dependencies */Arrays.asList(
      "JOPTSIMPLE_4_6",
      "JMH_GENERATOR_ANNPROCESS_1_21",
      "COMMONS_MATH3_3_2"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"442447101f63074c61063858033fbfde8a076873",
    /* urls */Arrays.asList()
    )).of(
  "JMH_GENERATOR_ANNPROCESS_1_18", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"b852fb028de645ad2852bbe998e084d253f450a5",
    /* urls */Arrays.asList()
    )).of(
  "JMH_GENERATOR_ANNPROCESS_1_21", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"7aac374614a8a76cad16b91f1a4419d31a7dcda3",
    /* urls */Arrays.asList()
    )).of(
  "JOPTSIMPLE_4_6", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"306816fb57cf94f108a43c95731b08934dcae15c",
    /* urls */Arrays.asList()
    )).of(
  "JUNIT", createMxLibrary(
    /* dependencies */Arrays.asList(
      "HAMCREST"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"2973d150c0dc1fefe998f834810d68f278ea58ec",
    /* urls */Arrays.asList()
    )).of(
  "JUNIT-JUPITER", createMxLibrary(
    /* dependencies */Arrays.asList(
      "JUNIT-JUPITER-API",
      "JUNIT-JUPITER-PARAMS"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"b5c481685b6a8ca91c0d46f28f886a444354daa5",
    /* urls */Arrays.asList()
    )).of(
  "JUNIT-JUPITER-API", createMxLibrary(
    /* dependencies */Arrays.asList(
      "APIGUARDIAN-API",
      "OPENTEST4J"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"c9ba885abfe975cda123bf6f8f0a69a1b46956d0",
    /* urls */Arrays.asList()
    )).of(
  "JUNIT-JUPITER-ENGINE", createMxLibrary(
    /* dependencies */Arrays.asList(
      "JUNIT-JUPITER-API",
      "APIGUARDIAN-API",
      "JUNIT-PLATFORM-ENGINE"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"c0833bd6de29dd77f8d071025b97b8b434308cd3",
    /* urls */Arrays.asList()
    )).of(
  "JUNIT-JUPITER-PARAMS", createMxLibrary(
    /* dependencies */Arrays.asList(
      "APIGUARDIAN-API"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"f2a64a42cf73077062c2386db0598062b7480d91",
    /* urls */Arrays.asList()
    )).of(
  "JUNIT-PLATFORM-COMMONS", createMxLibrary(
    /* dependencies */Arrays.asList(
      "APIGUARDIAN-API"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"7644a14b329e76b5fe487628b50fb5eab6ba7d26",
    /* urls */Arrays.asList()
    )).of(
  "JUNIT-PLATFORM-CONSOLE", createMxLibrary(
    /* dependencies */Arrays.asList(
      "APIGUARDIAN-API",
      "JUNIT-PLATFORM-REPORTING"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"dfdeb2688341f7566c5943be7607a413d753ab70",
    /* urls */Arrays.asList()
    )).of(
  "JUNIT-PLATFORM-ENGINE", createMxLibrary(
    /* dependencies */Arrays.asList(
      "APIGUARDIAN-API",
      "OPENTEST4J"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"1752cad2579e20c2b224602fe846fc660fb35805",
    /* urls */Arrays.asList()
    )).of(
  "JUNIT-PLATFORM-LAUNCHER", createMxLibrary(
    /* dependencies */Arrays.asList(
      "APIGUARDIAN-API",
      "JUNIT-PLATFORM-ENGINE"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"d866de2950859ca1c7996351d7b3d97428083cd0",
    /* urls */Arrays.asList()
    )).of(
  "JUNIT-PLATFORM-REPORTING", createMxLibrary(
    /* dependencies */Arrays.asList(
      "APIGUARDIAN-API",
      "JUNIT-PLATFORM-LAUNCHER"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"517d3b96b4ed89700a5086ec504fc02d8b526e79",
    /* urls */Arrays.asList()
    )).of(
  "NINJA", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */mapOf(java.lang.String.class, Arch.class).of(
      "darwin", createArch(
        /* amd64 */createMxLibrary(
          /* dependencies */Arrays.asList(),
          /* os_arch */Collections.emptyMap(),
          /* path */null,
          /* sha1 */"8142c497f7dfbdb052a1e31960fdfe2c6f9a5ca2",
          /* urls */Arrays.asList(
            "https://github.com/ninja-build/ninja/releases/download/v{version}/ninja-mac.zip")
          )
        )).of(
      "linux", createArch(
        /* amd64 */createMxLibrary(
          /* dependencies */Arrays.asList(),
          /* os_arch */Collections.emptyMap(),
          /* path */null,
          /* sha1 */"987234c4ce45505c21302e097c24efef4873325c",
          /* urls */Arrays.asList(
            "https://github.com/ninja-build/ninja/releases/download/v{version}/ninja-linux.zip")
          )
        )).of(
      "solaris", createArch(
        /* amd64 */null
        )).of(
      "windows", createArch(
        /* amd64 */createMxLibrary(
          /* dependencies */Arrays.asList(),
          /* os_arch */Collections.emptyMap(),
          /* path */null,
          /* sha1 */"637cc6e144f5cc7c6388a30f3c32ad81b2e0442e",
          /* urls */Arrays.asList(
            "https://github.com/ninja-build/ninja/releases/download/v{version}/ninja-win.zip")
          )
        )).build(),
    /* path */null,
    /* sha1 */null,
    /* urls */Arrays.asList()
    )).of(
  "NINJA_SYNTAX", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"702ca2d0ae93841c5ab75e4d119b29780ec0b7d9",
    /* urls */Arrays.asList(
      "https://pypi.org/packages/source/n/ninja_syntax/ninja_syntax-{version}.tar.gz")
    )).of(
  "OPENTEST4J", createMxLibrary(
    /* dependencies */Arrays.asList(
      "JUNIT-PLATFORM-COMMONS"),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"28c11eb91f9b6d8e200631d46e20a7f407f2a046",
    /* urls */Arrays.asList()
    )).of(
  "PROGUARD", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"996a984a7e230fdcfc269d66a6c91fd1587edd50",
    /* urls */Arrays.asList()
    )).of(
  "PROGUARD_6_0_3", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"7135739d2d3834964c543ed21e2936ce34747aca",
    /* urls */Arrays.asList()
    )).of(
  "PROGUARD_6_1_1", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"1d351efe6ada35a40cd1a0fdad4a255229e1c41b",
    /* urls */Arrays.asList()
    )).of(
  "PROGUARD_RETRACE", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"4a57d643d2ded6ebcf4b7bcdab8fcf3d2588aa1b",
    /* urls */Arrays.asList()
    )).of(
  "PROGUARD_RETRACE_6_0_3", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"4f249d487b06bedd29f0b7d9277a63d12d5d0a7e",
    /* urls */Arrays.asList()
    )).of(
  "PROGUARD_RETRACE_6_1_1", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"8b86348867593bd221521b01554724411f939d3c",
    /* urls */Arrays.asList()
    )).of(
  "SIGTEST", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"d5cc2cd2a20963b86cf95397784bc7e74101c7a9",
    /* urls */Arrays.asList()
    )).of(
  "SONARSCANNER_CLI_4_2_0_1873", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"fda01e04cd3c7fab6661aaadad2821c44577f80a",
    /* urls */Arrays.asList()
    )).of(
  "SPOTBUGS_3.0.0", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"6e56d67f238dbcd60acb88a81655749aa6419c5b",
    /* urls */Arrays.asList(
      "https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/findbugs-3.0.0.zip")
    )).of(
  "SPOTBUGS_3.1.11", createMxLibrary(
    /* dependencies */Arrays.asList(),
    /* os_arch */Collections.emptyMap(),
    /* path */null,
    /* sha1 */"8f961e0ddd445cc4e89b18563ac5730766d220f1",
    /* urls */Arrays.asList(
      "https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/spotbugs-3.1.11.zip")
    )).build(),
/* mxversion */null,
/* name */"mx",
/* projects */mapOf(java.lang.String.class, MxProject.class).of(
  "com.oracle.mxtool.checkcopy", createMxProject(
    /* annotationProcessors */Arrays.asList(),
    /* dependencies */Arrays.asList(),
    /* dir */null,
    /* javaCompliance */"1.8+",
    /* sourceDirs */Arrays.asList(
      "src"),
    /* subDir */"java"
    )).of(
  "com.oracle.mxtool.compilerserver", createMxProject(
    /* annotationProcessors */Arrays.asList(),
    /* dependencies */Arrays.asList(),
    /* dir */null,
    /* javaCompliance */"1.7+",
    /* sourceDirs */Arrays.asList(
      "src"),
    /* subDir */"java"
    )).of(
  "com.oracle.mxtool.jacoco", createMxProject(
    /* annotationProcessors */Arrays.asList(),
    /* dependencies */Arrays.asList(
      "JACOCOREPORT_0.8.4",
      "JOPTSIMPLE_4_6"),
    /* dir */null,
    /* javaCompliance */"1.8+",
    /* sourceDirs */Arrays.asList(
      "src"),
    /* subDir */"java"
    )).of(
  "com.oracle.mxtool.jmh_1_21", createMxProject(
    /* annotationProcessors */Arrays.asList(
      "JMH_1_21"),
    /* dependencies */Arrays.asList(
      "JMH_1_21"),
    /* dir */null,
    /* javaCompliance */"1.8+",
    /* sourceDirs */Arrays.asList(
      "src"),
    /* subDir */"java"
    )).of(
  "com.oracle.mxtool.junit", createMxProject(
    /* annotationProcessors */Arrays.asList(),
    /* dependencies */Arrays.asList(
      "JUNIT"),
    /* dir */null,
    /* javaCompliance */"1.8+",
    /* sourceDirs */Arrays.asList(
      "src"),
    /* subDir */"java"
    )).of(
  "com.oracle.mxtool.junit.jdk9", createMxProject(
    /* annotationProcessors */Arrays.asList(),
    /* dependencies */Arrays.asList(),
    /* dir */null,
    /* javaCompliance */"9+",
    /* sourceDirs */Arrays.asList(
      "src"),
    /* subDir */"java"
    )).of(
  "com.oracle.mxtool.webserver", createMxProject(
    /* annotationProcessors */Arrays.asList(),
    /* dependencies */Arrays.asList(),
    /* dir */null,
    /* javaCompliance */"1.8+",
    /* sourceDirs */Arrays.asList(
      "src"),
    /* subDir */"java"
    )).build()
);
    }
}
