function Nic(){
    this.fixed=1;
}
function Test(){
    /**
     *
     * @type Nic
     */
    this.case = new Nic();
}

window.TEST = new Test();
window.TEST.case.f
    