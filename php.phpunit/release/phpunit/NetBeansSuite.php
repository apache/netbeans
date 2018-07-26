<?php
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

// keep BC with PHPUnit versions < 6
if (!class_exists('PHPUnit_Framework_TestSuite')) {
    class PHPUnit_Framework_TestSuite extends PHPUnit\Framework\TestSuite {}
}

/**
 * Generic test suite containing tests based on the provided CLI parameters,
 * see {@link NetBeansSuite::toRun()} for more information.
 *
 * For directory:<br/>
 * Recursively scans the test-directory and it's
 * sub-directories. All found unit-tests will be
 * added and executed.
 *
 * For file:<br/>
 * Just the file is added.
 *
 * To run this suite from CLI: phpunit NetBeansSuite.php --run=<file-or-directory>
 *
 * <b>WARNING: User changes to this file should be avoided.</b>
 *
 * @package NetBeans
 */
class NetBeansSuite extends PHPUnit_Framework_TestSuite {
    /**
     * The name of the parameter followed by equals sign ("=") of the file or directory to be run by PHPUnit.
     * @see toRun()
     */
    const RUN = "--run=";

    /**
     * Suite factory.
     *
     * This function creates a PHPUnit test-suite,
     * scans the directory for test-cases,
     * adds all test-cases found and then returns
     * a test-suite containing all available tests.
     *
     * @access public
     * @static
     * @return NetBeansSuite
     */
    public static function suite() {
        $suite = new NetBeansSuite();
        foreach (self::toRun() as $file) {
            $suite->addTestFile($file);
        }
        return $suite;
    }

    /**
     * Tries to find {@link #RUN) in CLI parameters and returns array of files to be runj by PHPUnit
     * or throws Exception if no such parameter found or directory/file does not exist.
     *
     * @access private
     * @static
     *
     * @return array an array of files to be run by PHPUnit
     * @see RUN
     */
    private static function toRun() {
        $argv = isset($_SERVER['argv']) ? $_SERVER['argv'] : array();
        $run = null;
        foreach ($argv as $arg) {
            if (preg_match("/^\"?".self::RUN."(.+?)\"?$/", $arg, $sub)) {
                $run = $sub[1];
                break;
            }
        }
        if ($run === null) {
            throw new Exception(sprintf("No argument to run (%s) found.", self::RUN));
        }
        $result = array();
        foreach (explode(";", $run) as $part) {
            if (is_dir($part)) {
                $result = array_merge($result, self::rglob("*[Tt]est.php", $part.DIRECTORY_SEPARATOR));
            } elseif (is_file($part)) {
                $result[] = $part;
            } else {
                throw new Exception(sprintf("Argument '%s' neither file nor directory.", $part));
            }
        }
        return $result;
    }

    /**
     * Recursive {@link http://php.net/manual/en/function.glob.php glob()}.
     *
     * @access private
     * @static
     *
     * @param  string $pattern the pattern passed to glob(), default is "*"
     * @param  string $path    the path to scan, default is
     *                         {@link http://php.net/manual/en/function.getcwd.php the current working directory}
     * @param  int    $flags   the flags passed to glob()
     * @return array  an array of files in the given path matching the pattern.
     * @link http://php.net/manual/en/function.glob.php
     * @link http://php.net/manual/en/function.getcwd.php
     */
    private static function rglob($pattern = '*', $path = '', $flags = 0) {
        $paths = glob($path.'*', GLOB_MARK | GLOB_ONLYDIR | GLOB_NOSORT) or array();
        $files = glob($path.$pattern, $flags) or array();
        foreach ($paths as $path) {
            $files = array_merge($files, self::rglob($pattern, $path, $flags));
        }
        return $files;
    }
}

?>
