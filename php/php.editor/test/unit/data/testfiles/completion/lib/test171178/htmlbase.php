<?php
//************************************************************************************************************
class HTMLbase{

	public $sHTML;
	public $sHTML2; 		//Used to stack different sHTML's
	public $sHTML3; 		//Used for temporary storage of HTML data WILL NOT PRINT
	public $sHTML4; 		//Used for temporary storage of HTML data WILL NOT PRINT
	public $sTitle;
	public $sTitle2;
	public $sBBStyle;
	public $aFieldNames;
	public $sBBEndText; 	//Extra text to be put at the end of BlueBox
	public $sBBStartText; 	//Extra text to be put at the beginning of BlueBox

	//*******************************************************************************************************
	public function BlueBox($sForcePageBreak = ''){			//v1.1

		global $aDATA;

		if($this->sHTML2 == ''){

			if($this->sHTML != '') $sDATA = $this->sHTML;

		}
		else $sDATA = $this->sHTML2;

		if(trim($sDATA) == '') return;

		$sTitle = trim($this->sTitle) ? '<div class="sectiontitle-print">'.strtoupper($this->sTitle).
				'</div>' : '';

		$sPageBreak = $sForcePageBreak ? ';page-break-'.$sForcePageBreak.':always;' : '';

		echo '<div class="bluebox-print" id="sec'.Sec().'" style="'.$this->sBBStyle.$sPageBreak.'">'.
				$sTitle.$this->sBBStartText.$sDATA.$this->sBBEndText.'<div class="clear"></div></div>';

		if($sForcePageBreak == 'after')
			echo '<div class="thinrow" style="text-align:left"><table style="width:100%"><tr>'.
				'<td>Patient Name: '.$aDATA['last_name'].', '.$aDATA['first_name'].'</td><td>SS#: '.
				FormatSSN($aDATA['ssn']).'</td><td style="text-align:right"> Test Date: '.
				FormatDate($aDATA['testdate']).'</td></tr></table></div>';

	}

	//*******************************************************************************************************
	public function IsEmpty(array $aFieldsOrValues){

		global $aDATA;

		$sTemp = '';

		foreach($aFieldsOrValues as $sField){

			if(IsField($sField)) $sTemp .= $aDATA[$sField];
			else $sTemp .= $sField;

		}

		if($sTemp != '') return true;
		else return false;

	}

	//*******************************************************************************************************
	public function Grid4($sField, $sCol1, $sCol2, $sCol3, $sCol4 = ''){

		global $aDATA;

		$iTotalFields 		= 50;

		for($iMenu = 0; $iMenu < $iTotalFields; $iMenu ++){

			$s = $aDATA[$sField.$iMenu.'_0'].$aDATA[$sField.$iMenu.'_1']
				. $aDATA[$sField.$iMenu.'_2'].$aDATA[$sField.$iMenu.'_3'];

			if($s != ''){

				$sTemp .= '<div class="row">' .
							'<span class="colfive1">'.$aDATA[$sField.$iMenu].'</span>';

				for($i = 0; $i < 4; $i ++){

					$sMark = ($aDATA[$sField.$iMenu] == 'Lift' &&
								$aDATA[$sField.$iMenu.'_'.$i] != '')
								? ' #' : '';

					$sTemp .= '<span class="colfive2">'.$aDATA[$sField.$iMenu.'_'.$i] .
								$sMark.'</span>';

				}

				$sTemp .= '</div>';

			}

		}

		if($sTemp != ''){

			$this->sHTML .= '<div class="row"><span class="colfive1"></span>' .
				'<span class="colfive2" style="text-decoration:underline">'.$sCol1 .
					'</span>' .
				'<span class="colfive2" style="text-decoration:underline">'.$sCol2 .
					'</span>' .
				'<span class="colfive2" style="text-decoration:underline">'.$sCol3 .
					'</span>' .
				'<span class="colfive2" style="text-decoration:underline">'.$sCol4.
					'</span>' .
				'</div>'.$sTemp;

		}

	}

	//*******************************************************************************************************
	//Clear Others
	public function ClearOthers(&$s){

		$s = str_ireplace('Other: ', '', $s);
		$s = str_ireplace('other: ', '', $s);
		//$s = str_ireplace(': ', '', $s);

	}

	//*******************************************************************************************************
	// Fieldset Section Function(1st argument is the Legend variable, all others are LI items)
	public function FieldsetSection(){		//v1.1

		global $aDATA;

		$aArgs = func_get_args();
//		$sLegend = $aArgs[0];
		array_splice($aArgs, 0, 1);

		foreach($aArgs as $sFieldName){

			if(IsField($sFieldName)) $s .= $aDATA[$sFieldName];
			else $s .= $sFieldName;

		}

		if($s != ''){

			$temp .= '<fieldset><legend>'.func_get_arg(0).'</legend>';

			foreach($aArgs as $sFieldName){

				if(IsField($sFieldName) && $aDATA[$sFieldName] != '')
					$temp .= $this->StringSection($sFieldName).' ';
				elseif($sFieldName != '')
					$temp .= $this->StringSection($sFieldName).' ';

			}

			$temp .= '</fieldset>';

		}

		return $temp;

	}

	//*******************************************************************************************************
	public function One_Time_Title($sSession, $sTitle){

		if($_SESSION[$sSession] != 1){

			$_SESSION[$sSession] = 1;

			return $sTitle;

		}
		else return '';

	}

	//*******************************************************************************************************
	//Debug
	public function Debug($s){			//v1.0

		if($GLOBALS['debug'] == 1 && UserInGroupCheck('IT') && _PRODTESTDEV != PROD)
			return '(<em>'.$s.'</em>)';
		else return '';

	}

	//*******************************************************************************************************
	//HTML2 (Displayed in BlueBox if it contains data)
	public function H2(){

		$this->sHTML2 .= $this->sHTML;
		unset($this->sHTML);

	}

	//*******************************************************************************************************
	//HTML3 (Not displayed in BlueBox, for temporary storage)
	public function H3(){

		unset($this->sHTML3);
		$this->sHTML3 = $this->sHTML;
		unset($this->sHTML);

	}

	//*******************************************************************************************************
	//HTML4 (Not displayed in BlueBox, for temporary storage)
	public function H4(){

		unset($this->sHTML4);
		$this->sHTML4 = $this->sHTML;
		unset($this->sHTML);

	}

	//*******************************************************************************************************
	//sRow is the HTML formatting, $s# is the arguments passed, in order
	public function SRow($sRow){

		global $aDATA;

		$iNumArgs = func_num_args();

		if($iNumArgs < 2) return '';

		$aArgs	= func_get_args();
		array_shift($aArgs);
		$bEmpty	= true;

		foreach($aArgs as $iKey => $sArg){

			if(IsField($sArg)){

				if(trim($aDATA[$sArg]) != '') $sData = $this->debug($sArg).$aDATA[$sArg];

			}
			else $sData = $sArg;

			if($sData){

				$sRow	= str_replace('$s'.$iKey, $sArg, $sRow);
				$bEmpty	= false;

			}

		}

		if(!$bEmpty) return $sRow;
		else return '';

	}

	//*******************************************************************************************************
	//Basic Row function - only displays if second paramenter isn't ''
	public function Row($s0, $s1 = '', $s2 = ''){

		global $aDATA;

		if(trim($s1) != ''){

			if(IsField($s1)){

				if(trim($aDATA[$s1]) != '') $sData = $this->debug($s1).$aDATA[$s1];

			}
			else $sData = $s1;

			if($sData){

				$sAddDIV = (substr($sData, 0, strlen('<div class="row">')) == '<div class="row">') ?
							false : true;

				if($sAddDIV) $this->sHTML .= '<div class="row">';

				$this->sHTML .= $s0;
				$this->sHTML .= $sData;
				$this->sHTML .= $s2;

				if($sAddDIV) $this->sHTML .= '</div>';

			}

		}

	}

	//*******************************************************************************************************
	//Basic Item function - only displays if second parameter isn't ''
	public function Item($s0, $s1, $s2 = ''){			//v1.1

		global $aDATA;

		if(IsField($s1) && trim($aDATA[$s1])) return $s0.$this->debug($s1).$aDATA[$s1].$s2;
		elseif(!IsField($s1) && trim($s1)) return $s0.$s1.$s2;
		else return '';

	}

	//*******************************************************************************************************
	//Mid Item function - only displays if 2nd and 4th parameter combined aren't ''
	public function ItemMid($s0, $s1, $s2, $s3, $s4 = ''){		//v1.1

		global $aDATA;

		if(IsField($s1)) $s1 = $aDATA[$s1];

		if(IsField($s3)) $s3 = $aDATA[$s3];

		if(trim($s1.$s3) != '') return $s0.$s1.$s2.$s3.$s4;
		else return '';

	}

	//*******************************************************************************************************
	//Comma list
	public function CommaSection($sFieldName){		//v1.2

		global $aDATA, $aFieldNames;

		foreach($aFieldNames as $sCurrentField){

			if(stristr($sCurrentField, $sFieldName) &&
				!$this->InExtension($sCurrentField, $sFieldName, '_s')){

				if(stristr($aDATA[$sCurrentField], 'other')) $aDATA[$sCurrentField] = ' ';

				if($aDATA[$sCurrentField] != ''){

					$sSection .= $this->StringSection($sCurrentField);

					if($sSection != '') $sSection .= ', ';

				}

			}

		}

		$sSection = trim($sSection, ', ');

		return $sSection;

	}

	//*******************************************************************************************************
	//Extension of field from $sLength
	protected function Extension($sField, $sLength){				//v1.0

		return (substr($sField, strlen($sLength)));

	}

	//*******************************************************************************************************
	//If $sSuffix exists in Extension
	protected function InExtension($sField, $sLength, $sSuffix){ 	//v1.1

		if(stristr($this->Extension($sField, $sLength), $sSuffix)) return true;

		return false;

	}

	//*******************************************************************************************************
	//If $sSuffix equals the length of itself in Extension starting from 0
	protected function IsExtension($sField, $sLength, $sSuffix){ 	//v1.0

		if(substr($this->Extension($sField, $sLength), 0, strlen($sSuffix)) == $sSuffix) return true;

		return false;

	}

	//*******************************************************************************************************
	//Bulleted list (has a number following the fieldname for multiple fields)
	public function BulletSection($sFieldName, $sType = 'ol', $sListStyle = '', $sSubIsComma = false){	//v1.6

		global $aDATA, $aFieldNames;

		//Cycle through all fields in $aFieldNames
		foreach($aFieldNames as $s){

			//Only used fields from $aFieldNames array that contain $sFieldName
			if(stristr($s, $sFieldName)){

				if(
					!$this->InExtension($s, $sFieldName, '_s') && //If _s is NOT in the extension
					!$this->InExtension($s, $sFieldName, '_a') && //If _a is NOT in the extension
					(is_numeric(substr($this->Extension($s, $sFieldName), 0, 1)) || //Extension is a #
					$this->Extension($s, $sFieldName) == '') //OR does not have an extension
				){

					$aFields[] = $s;
					$sEmpty 	.= $aDATA[$s];

				}

			}

		}

		if($sEmpty != ''){

			natsort($aFields);

			foreach($aFields as $s){

				if(trim($aDATA[$s]) != ''){

					$aDATA[$s] = (in_array(strtolower($aDATA[$s]), array('other', 'other:', 'other: ')))
						 		? '' : $aDATA[$s];

					if(trim(strtolower($aDATA[$s])) == 'comment:' && IsField($s.'_s0') &&
						trim($aDATA[$s.'_s0']) == '') continue;

					$temp .= '<li>'.trim(ucfirst($this->StringSection($s)));

					if(!$sSubIsComma) $sSub = $this->BulletSubSection($s);
					else $sSub = ' '.$this->CommaSection($s.'_a');

					if($sSub == 'ExistsButEmpty') continue;

					$temp .= $sSub.'</li>';

					if($temp != '<li></li>') $sSection .= $temp;

					unset($temp, $sSub);

				}

			}

		}

		if($sSection != '') $sSection = '<'.$sType.' style="'.$sListStyle.'">'.$sSection.'</'.$sType.'>';

		return $sSection;

	}

	//*******************************************************************************************************
	//Bulleted list - sub list of BullectSection (_a)
	public function BulletSubSection($sFieldName){		//v1.1

		global $aDATA, $aFieldNames;

		$bExists = false;

		foreach($aFieldNames as $s){

			if(
				stristr($s, $sFieldName) && // $sFieldName is in $s
				!is_numeric(substr($this->Extension($s, $sFieldName), 0, 1)) //But not another digit
				){

				if(!$this->InExtension($s, $sFieldName, '_s') &&
					$this->InExtension($s, $sFieldName, '_a')){

					$aFields[] 	= $s;
					$bExists 		= true;
					$sEmpty 	    .= $aDATA[$s];

				}

			}

		}

		$sSection = '';

		if($sEmpty != ''){

			$sSection = '<ul>';

			natsort($aFields);

			foreach($aFields as $s){

				if($aDATA[$s] != ''){

					if(in_array(strtolower($aDATA[$s]), array('other', 'other:', 'other: ')))
						$aDATA[$s] = '';

					$sSection .= '<li>'.trim(ucfirst($this->StringSection($s))).'</li>';

				}

			}

			$sSection .= '</ul>';

		}

		if($bExists && !$sSection) return 'ExistsButEmpty';

		return $sSection;

	}

	//*******************************************************************************************************
	//Strings several Strings together (_s)
	public function StringSection($sFieldName, $sFirstMustBeTrue = false){ 		//v1.3

		global $aDATA, $aFieldNames;

		if($aDATA[$sFieldName] == '' && $sFirstMustBeTrue) return '';

		foreach($aFieldNames as $s){

			if(stristr($s, $sFieldName)){

				if((
					$this->IsExtension($s, $sFieldName, '_s') || //The Ext is _s
					$s == $sFieldName) && //Or $s is the $sFieldName
					!(stristr($s, '_a') && //And _a isn't in $s
					!stristr($sFieldName, '_a')) && //And _a isn't in the $sFieldName
					$aDATA[$s] != 'Other:' && //And the field data isn't Other:
					$aDATA[$s] != ':' //And the field data isn't the other other (:)
				){

					$aFields[]	= $s;
					$sEmpty 	    .= $aDATA[$s];

				}

			}

		}

		unset($s);

		if($sEmpty){

			natsort($aFields);

			foreach($aFields as $s){

				if($aDATA[$s] != '') $sSection .= $this->Debug($s).$aDATA[$s];

				if(stristr($s, '_s') && IsField($s.'_a0') && is_numeric(NextChar($s, '_s'))){

					$sTemp	= $this->BulletSubSection($s);
					$sSection .= $sTemp != 'ExistsButEmpty' ? $sTemp : '';

				}

			}

		}

		//Clean up any trailing commas
		$sSection = rtrim($sSection, ',');
		$sSection = rtrim($sSection, ', ');

		return $sSection;

	}

	//*******************************************************************************************************
	//Clear storage variables
	public function Clear(){

		unset($this->sHTML, $this->sTitle, $this->sTitle2, $this->sHTML2, $this->sHTML3, $this->sHTML4,
				$this->sBBStyle);

	}

}
?>
