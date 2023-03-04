#!/usr/bin/env perl
# -*- perl -*-
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

$totalwc = 0;
$totallc = 0;

printf "%7s %5s File\n", "Strings", "Words";
printf "%7s %5s %s\n", "-------", "-----", "-" x 40;

foreach $f (@ARGV) {
    open FH, "< $f" or die;
    {
        local $/ = undef;
        $all = <FH>;
    }
    close FH;

    @lines = split /\r|\n/, $all;

    $wc = 0;
    $lc = 0;

    foreach (@lines) {
        next if /^\s*#/;
        next if /^\s*$/;

        if (/^[^=]+=(.*)$/) {
            $_ = $1;
            s/^\s*//;
            s/\s*$//;
            @words = split /\s+/;
            $wc += @words ;
            $lc++;
        }
    }
    
    printf "%7d %5d %s\n", $lc, $wc, $f;
    $totalwc += $wc;
    $totallc += $lc;
}

printf "\n%7d %5d Total\n", $totallc, $totalwc;
