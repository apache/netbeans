<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<?php
require_once '../library/common.php';
require_once 'report_common.php';

global $sForm;

$debug = 0; // 0 for off, 1 for on

$sDATA = SelectSQL($sForm ? $sForm : substr($_SERVER['PHP_SELF'], 16, 3));
$rDATA = MYSQLQuery($sDATA, 'Report RCR - Select Records');

if($rDATA) $aDATA = mysql_fetch_array($rDATA);

if(is_array($aDATA)) array_walk($aDATA, 'Text2HTMLReplace');

if($debug) echo 'skey: '.$aDATA['skey'];

$aFieldNames 	= FieldNames($rDATA);

//>>>>>END PROGRAM FLOW>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

//************************************************************************************************************
// Class: HTML
//************************************************************************************************************
class HTML extends HTMLbase{

	//Blue Box******************************************************************************************
	public function BlueBox($sBorder = true){

		$sClass = $sBorder ? 'bluebox-print' : 'bluebox-print_nb';
		$sTitle = $this->sTitle != '' ? '<div class="sectiontitle-print">'.$this->sTitle.'</div>' : '';

		if($this->sHTML2 == ''){

			if($this->sHTML != '')
				echo '<div class="'.$sClass.'" id="sec'.Sec().'" style="'.$this->sBBStyle.'margin:3px">'.
						$sTitle.$this->sHTML.'<div style="clear:both"></div></div>';

		}
		else echo '<div class="'.$sClass.'"  style="margin:3px" id="sec'.Sec().'">'.$sTitle.$this->sHTML2.
					'</div>';

	}

}

$oPrint = new HTML();

Body();

//*****************************************************************************************************
function Body(){

	global $aDATA, $sFormName;
	?>
		<head>
			<title>
				<?=$sFormName?> for <?=$aDATA['last_name'].', '.$aDATA['first_name']?>
			</title>
			<?php Head('nocommon', 'report');?>
			<style type="text/css">
				body{
					font-family:"Times New Roman",Times,serif;
					color:#000000;
					background-color:#CCCCCC;
					text-align:center;
					margin:5px;
				}
				div.bluebox-print{
					background-color:#FFFFFF;
					padding:2px;
					border:3px outset #000000;
					text-align:left;
					page-break-before:auto;
				}
				div.bluebox-print_nb{
					background-color:#FFFFFF;
					padding:2px;
					text-align:left;
					page-break-before:auto;
				}
				div.paper{
					background-color:#FFF;
					font-family:Arial, Helvetica, sans-serif;
					font-size:14px;
					width:700px;
					margin-right:auto;
					margin-left:auto;
				}
				div.sectiontitle-print{
					text-align:center;
					width:100%;
					margin-top:2px;
					font-size:18px;
					font-weight:bolder;
					margin-bottom:5px;
				}
				.clear{
					clear:both;
				}
				.noprint{
					display:none;
				}
				div.title-print{
					text-align:center;
					width:100%;
					margin-top:5px;
					font-size:24px;
					font-weight:bolder;
				}
				div.row{
					width:100%;
					margin-bottom:5px;
				}
				H4{
					font-size:14px;
					font-weight:900;
				}
				.ems{
					list-style:upper-alpha;
				}
				.pso{
					list-style:lower-alpha;
				}
				.colleft{
					float:left;width:45%;
				}
				.colleft2{
					float:left;width:70%;
				}
				.colleft3{
					float:left;width:43%;
				}
				.colright{
					float:right;width:49%;
				}
				.colright2{
					float:right;width:25%;
				}
				.colright3{
					float:right;width:56%;
				}
				.rextralong{
					float:right;
					width:400px;
					text-align:left;
				}
				.rlong{
					float:right;
					width:200px;
					text-align:left;
				}
				.rmedium{
					float:right;
					text-align:left;
					width:150px;
				}
				.rsmall{
					float:right;
					text-align:left;
					width:100px;
				}
				.rtiny{
					float:right;
					width:50px;
					text-align:left;
				}
				.label{
					float:left;
				}
				.label2{
					float:left;
					font-weight:bold;
				}
				.label3{
					width:190px;
					float:left;
					text-align:right;
					font-weight:bold;
				}
				.colfive1{
					float:left;
					width:31%;
				}
				.colfive2{
					float:left;
					width:17%;
				}
				.general{
					width:395px;
				}
				@media print{
					.reportbuttons{
						display:none;
					}
				}
			</style>
		</head>
		<body>
			<div class="paper">
			    <div class="title-print" style="position:relative;" id="sec<?=Sec()?>">
					<span style="position:absolute;left:30px;top:10px;">
						<img src="http://intranet/images/logo.jpg" style="vertical-align:middle;"
							alt="PRIDE LOGO">
					</span>
					<span
						style="position:absolute;right:10px;top:5px;font-weight:normal;font-size:10px;">
				     	5701 Maple Ave. Suite 100<br />Dallas, TX 75235
					</span>
					<span style="position:absolute;right:10px;top:58px;font-size:8px;">
						Form Revision Date: <?=$aDATA['form_ver']?>
					</span>
			    		<br />
			    		<?=$aDATA['formtitle'].(($aDATA['form_num'] != 1) ? ' #'.$aDATA['form_num'] : '')?>
			    		<br />
			    </div>
				<br />
				<?php
					General();
					DemoInfo();
					CaseIssues();
					Employment();
					WorkHistory();
					EducationTraining();
					WorkAdjustment();
					RTW();
					Barriers();
					Recommendations();
					Signatures();
				?>
			</div>
			<script language="javascript" type="text/javascript">
				PageBreak('sec', 15, 950);
				Maximize();
			</script>
		</body>
	<?php
}

//************************************************************************************************************
function General(){

	global $aDATA;

	$oPrint = new HTML();

	$oPrint->Row('', $oPrint->ItemMid('<div><span class="colleft3">' .
			'<span class="label2">Patient Name: </span>' .
			'<span class="rlong">', $aDATA['first_name'].' '.$aDATA['last_name'], '</span></span>' .
			'<span class="colright3"><span class="label2">Date: </span>' .
			'<span class="rlong">',
			FormatDate($aDATA['date'], false), '</span></span></div>'));
	$oPrint->Row('', $oPrint->ItemMid('<div><span class="colleft3">' .
			'<span class="label2">SS#: </span>' .
			'<span class="rlong">', FormatSSN($aDATA['ssn']), '</span></span>' .
			'<span class="colright3"><span class="label2">RC Staff: </span>' .
			'<span class="rlong">',
			'rc', '</span></span></div>'));
	$oPrint->BlueBox();

}

//************************************************************************************************************
function DemoInfo(){

	global $aDATA;

	$oPrint = new HTML();

	$oPrint->sTitle = 'Demographic Information';
	$oPrint->Row('', $oPrint->ItemMid('<div><span class="colleft"><span class="label2">Age:</span>' .
			'<span class="rlong">', 'dem_age', '</span></span>' .
			'<span class="colright"><span class="label2">Gender:</span>' .
			'<span class="rlong">',
			'dem_gender', '</span></span></div>'));
	$oPrint->Row('', $oPrint->ItemMid('<div><span class="colleft">' .
			'<span class="label2">Marital Status:</span>' .
			'<span class="rlong">', 'dem_marital_status', '</span></span>' .
			'<span class="colright"><span class="label2">City, State:</span>' .
			'<span class="rlong">',
			$aDATA['dem_city'].', '.$aDATA['dem_state'], '</span></span></div>'));
	$oPrint->Row('<strong>Cohabitants:</strong> ', $oPrint->CommaSection('dem_cohabitants'));
	$oPrint->Row('', 'dem_comments');
	$oPrint->BlueBox();

}

//************************************************************************************************************
function CaseIssues(){

	global $aDATA;

	$oPrint = new HTML();
	$oPrint->sTitle = 'Case Issues';
	$oPrint->Row('', $oPrint->ItemMid('<div><span class="colleft">' .
			'<span class="label2">Case Type:</span>' .
			'<span class="rlong">', 'dis_case_type', '</span></span>' .
			'<span class="colright"><span class="label2">Attorney Name:</span>' .
			'<span class="rlong">',
			'dis_attorney_name', '</span></span></div>'));
	$oPrint->Row('', $oPrint->ItemMid('<div><span class="colleft">' .
			'<span class="label2">DOI:</span>' .
			'<span class="rlong">', 'dis_on_date', '</span></span>' .
			'<span class="colright"><span class="label2">Carrier:</span>' .
			'<span class="rlong">',
			$aDATA['dem_city'].', '.$aDATA['dis_carrier'], '</span></span></div>'));
	$oPrint->Row('<strong style="float:left">Attorney Representation on Case: </strong>' .
			'<div style="margin-left: 5px;float:left;width: 60%;display: block">',
			$oPrint->CommaSection('dis_attorney'), '</div>');
	$oPrint->Row('<strong style="float:left">Primary Income Source: </strong>' .
			'<div style="margin-left: 5px;float:left;width: 70%;display: block">',
			$oPrint->CommaSection('dis_pri_income'), '</div>');
	$oPrint->Row('<strong style="float:left">Secondary Income Source(s): </strong>' .
			'<div style="margin-left: 5px;float:left;width: 70%;display: block">',
			$oPrint->CommaSection('dis_sec_income'), '</div>');
	$oPrint->BlueBox();

}

//************************************************************************************************************
function Employment(){

	$oPrint = new HTML();
	$oPrint->sTitle = 'Employment Information';
	$oPrint->Row('', $oPrint->ItemMid('<div><span class="colleft">' .
			'<span class="label2">Employer of Injury:</span>' .
			'<span class="rmedium">', 'emp_employer', '</span></span>' .
			'<span class="colright"><span class="label2">Job Title:</span>' .
			'<span class="rlong">',
			'emp_job_title', '</span></span></div>'));
	$oPrint->Row('<strong>Work Status: </strong>', 'emp_work_status');
	$oPrint->Row('<strong>Preinjury Job Availability: </strong>', 'emp_job_availability');
	$oPrint->H2();
	$oPrint->Grid4('emp_analysis', 'Continuous', 'Frequent', 'Occasional', 'Rare');
	$oPrint->H3();
	$oPrint->Row('<strong>Patient Report of Job Demands:</strong>', $oPrint->sHTML3);
	$oPrint->Row('<strong>Job Demand Category: </strong>', 'emp_job_demand');
	$oPrint->Row('', 'emp_comments');
	$oPrint->H2();
	$oPrint->BlueBox();

}

//************************************************************************************************************
function WorkHistory(){

	global $aDATA;

	$oPrint = new HTML();
	$oPrint->sTitle = 'Work History / Transferable Skills';
	$oPrint->Row($aDATA['work_intro'].'<br />' .
			'<strong style="float:left">Transferable Skills:</strong>' .
			'<div style="margin-left: 5px;float:left;width: 70%;display: block">',
			$oPrint->CommaSection('work'), '</div>');
	$oPrint->Row('', 'work_comments');
	$oPrint->BlueBox();

}

//************************************************************************************************************
function EducationTraining(){

	$oPrint = new HTML();
	$oPrint->sTitle = 'Education / Training';
	$oPrint->Row('<span class="label2">Highest Level of Education Completed:</span>' .
			'<span class="rextralong">', 'edu_level', '</span>');
	$oPrint->Row('<span class="label2">Degree(s) Earned:</span>' .
			'<span class="rextralong">', 'edu_degrees', '</span>');
	$oPrint->Row('<span class="label2">Vocational/Technical Training Completed:</span>' .
			'<span class="rextralong">', 'edu_voctech', '</span>');
	$oPrint->Row('<span class="label2">On-The-Job Training Completed:</span>' .
			'<span class="rextralong">', 'edu_onthejob', '</span>');
	$oPrint->Row('<span class="label2">Certification/Licensure:</span>' .
			'<span class="rextralong">', 'edu_certificationlicensure', '</span>');
	$oPrint->Row('', 'edu_comments');
	$oPrint->BlueBox();

}

//************************************************************************************************************
function WorkAdjustment(){

	$oPrint = new HTML();
	$oPrint->sTitle = 'Work Adjustment Issues';
	$oPrint->Row('', $oPrint->BulletSection('adjust', 'ul'));
	$oPrint->BlueBox();

}

//************************************************************************************************************
function RTW(){

	$oPrint = new HTML();
	$oPrint->sTitle = 'Return to Work Issues';
	$oPrint->Row('', $oPrint->BulletSection('rtw', 'ul'));
	$oPrint->BlueBox();

}

//************************************************************************************************************
function Barriers(){

	$oPrint = new HTML();
	$oPrint->sTitle = 'Potential Barriers To Recovery';
	$oPrint->Row('', $oPrint->BulletSection('bar', 'ul'));
	$oPrint->BlueBox();

}

//************************************************************************************************************
function Recommendations(){

	$oPrint = new HTML();
	$oPrint->sTitle = 'Recommendations';
	$oPrint->Row('', $oPrint->BulletSection('rec', 'ul'));
	$oPrint->BlueBox();

}

//************************************************************************************************************
function Signatures(){

	global $aDATA, $oPrint;

	$oPrint = new HTML();

	if($aDATA['status'] == 'Complete'){

		$oPrint->sHTML = '<div style="text-align: left;margin-left: 15px;margin-top: 30px">';

		$id = $aDATA['complete_usersid'];

		switch($id){

			case 17:

				$oPrint->Row('<img src="http://intranet/images/sig_'.
					UserNameByID($aDATA['complete_usersid']).'.jpg">',
					'<br />'.GetProvider('id', $id, 'name'));

				$oPrint->Row('<img src="http://intranet/images/sig_'.
					UserNameByID(25).'.jpg">',
					'<br />'.GetProvider('id', 25, 'name'));

				break;

			case 7:

				$oPrint->Row('<img src="http://intranet/images/sig_'.
					UserNameByID($aDATA['complete_usersid']).'.jpg">',
					'<br />'.GetProvider('id', $id, 'name'));

				$oPrint->Row('<img src="http://intranet/images/sig_'.
					UserNameByID(59).'.jpg">',
					'<br />'.GetProvider('id', 59, 'name'));

				break;

			default:

				$oPrint->Row('<img src="http://intranet/images/sig_'.
					UserNameByID($aDATA['complete_usersid']).'.jpg">',
					'<br />'.GetProvider('id', $aDATA['complete_usersid'], 'name'));

		}

		$oPrint->sHTML .= '</div>';
		$oPrint->BlueBox(false);

	}

}
$hTML = new HTML();
$hTML->
?>
</html>