<?php

interface Interface1
{
    public function Interface1Function();
}
interface Interface2
{
    public function Interface2Function();
}
class Foooo
{
}
class Barrr implements Interface1, Interface2
{
    public function Interface1Function()
    {
    }
    public function Interface2Function()
    {
    }
}

$Foooo = new Foooo();
$Foooo->param = array(new Barrr(), new Barrr());

/* @var $Barrr Interface1|Interface2 */
foreach($Foooo->param as $Barrr):
    echo $Barrr->Interface2Function();
endforeach;
?>