<?php
class AAA implements IAAA{}
class BBB extends AAA implements IBBB{}
class CCC extends BBB implements IBBB, ICCC{}
interface IAAA {}
interface IBBB extends IAAA {}
interface ICCC extends IBBB {}

?>
