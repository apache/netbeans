<?php
/*
This is a Flight helper class. Instance of
this class can store flight information.
*/

class Flight{
    private $FID;
    private $fName;
    private $source;
    private $dest;

    function set_FID($FID){
            $this->FID = $FID;
        }
    function set_FName($fName){
            $this->fName = $fName;
        }
    function set_source($source){
            $this->source = $source;
        }
    function set_dest($dest){
            $this->dest = $dest;
        }

    function get_FID(){
            return $this->FID;
        }
    function get_FName(){
            return $this->fName;
        }
    function get_source(){
            return $this->source;
        } 
    function get_dest(){
            return $this->dest;
        } 
}
?>