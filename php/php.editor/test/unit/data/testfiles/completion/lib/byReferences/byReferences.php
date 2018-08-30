<?php

class MyClass {
}

interface MyIface {
}

interface Collection {
}

function &my_sort1(Collection $data) {
}

function &my_sort2(array &$data) {
}

function my_sort3(MyClass &$data) {
}

function &my_sort4($data) {
}

function &my_sort5(&$data) {
}

function my_sort6(&$data) {
}

function &my_sort7(array &$data1, Collection &$data2) {
}

function my_sort8(array &$data1, Collection &$data2) {
}

function &my_sort9(array $data1, Collection &$data2) {
}

function &my_sort10(array &$data1, Collection $data2) {
}

function my_sort11(array $data1, Collection &$data2) {
}

function &my_sort12(array $data1, Collection $data2) {
}

function &my_sort13(array &$data1, Collection &...$data2) {
}
