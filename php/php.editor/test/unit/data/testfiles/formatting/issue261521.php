<?php
class A {
}
interface I {
}
$instance = new class($a)extends A {
    
};

return new class($a)implements I {
    
};
