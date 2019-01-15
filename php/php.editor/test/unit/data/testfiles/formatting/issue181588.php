<?php

class ClassName
{

    public static function PhaseTracking_SplitPhaseTeam($sPhaseTeam)
    {

        if ($sPhaseTeam == 'OPT') $sPhase = $sPhaseTeam;
        else $sPhase = substr($sPhaseTeam, 1, 1) ? : '';

        if (stristr($sPhaseTeam, 'BLUE')) $sTeam = 'BLUE';
        elseif(stristr($sPhaseTeam, 'GREEN')) $sTeam = 'GREEN';
        else $sTeam = '';

        return array($sPhase, $sTeam);
    }
}

?>