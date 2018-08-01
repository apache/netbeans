def rangeTest() {
    // "groovy.lang.Range"
    // this is buggy in test case
    (1..10).a
    // this should offer cc for integer
    1..10.d
    // test full list
    (1..10).
}

def listTest() {
    // "java.util.List"
   ["one","two"].listIter
   ["one","two"].it
}

def mapTest() {
    // "java.util.Map"
  [1:"one", 2:"two"].ent
}