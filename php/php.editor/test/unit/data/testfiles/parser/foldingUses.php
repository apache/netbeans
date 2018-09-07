<?php
namespace A;

use App\Test1;
use App\Test2;


use App\Test3;
use App\{
    Test4,
    Test5
};

function functionName($param) {
    
}

use App\{
    Test6
};
use App\Test7;

namespace B;

use App\Test2;

function functionName($param) {
    
}

use const App\Test3\CONSTANT;
use function App\{
    test1,
    test2
};
