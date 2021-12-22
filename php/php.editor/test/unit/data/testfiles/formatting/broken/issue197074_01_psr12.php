<?php

class Model_Zendcalendar extends Zend_Db_Table {

protected $_name = 'zend_calendar';

    public function getAllVytrshenByYear($year) {
$select = $this->select();
        $select->from(array('c' => 'zend_calendar'), array('id', 'cat',
'start_date', 'end_date', 'organizer', 'place', 'description', 'more_info',))
                ->where('cat = ?', 1)
                ->where('YEAR(start_date) = ?', $year)
                ->order('start_date')
                ->joinLeft(array('f' => 'zend_calendar_files'), 'c.id = f.id',
array('info', 'results', 'games'))
                ->setIntegrityCheck(false);
        return $this->fetchAll($select);
    }

public function getAllOtkritiByYear($year) {
$select = $this->select();
$select->from(array('c' => 'zend_calend