<?php
class teacher140784 {
    private $name;
    function getName() {
        return $this->name;
    }
    function setName($name) {
        $this->name = $name;
    }
}
class pupil140784 {
    private $teacher;
    function __construct() {
        $this->teacher = new teacher140784;
        $this->teacher->setName("Johny");
    }
    function getTeacherName() {
        return $this->teacher->getName();
    }
}
$pupil = new pupil140784;
echo "Teacher name: ".$pupil->getTeacherName();
?>