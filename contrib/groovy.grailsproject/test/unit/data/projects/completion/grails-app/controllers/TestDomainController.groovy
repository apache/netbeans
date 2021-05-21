

class TestDomainController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {

        // test1
        this.

        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        [ testDomainInstanceList: TestDomain.list( params ), testDomainInstanceTotal: TestDomain.count() ]
    }

    def show = {
        def testDomainInstance = TestDomain.get( params.id )

        if(!testDomainInstance) {
            flash.message = "TestDomain not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ testDomainInstance : testDomainInstance ] }
    }

    def delete = {
        def testDomainInstance = TestDomain.get( params.id )
        if(testDomainInstance) {
            try {
                testDomainInstance.delete()
                flash.message = "TestDomain ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "TestDomain ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "TestDomain not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def testDomainInstance = TestDomain.get( params.id )

        if(!testDomainInstance) {
            flash.message = "TestDomain not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ testDomainInstance : testDomainInstance ]
        }
    }

    def update = {
        def testDomainInstance = TestDomain.get( params.id )
        if(testDomainInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(testDomainInstance.version > version) {
                    
                    testDomainInstance.errors.rejectValue("version", "testDomain.optimistic.locking.failure", "Another user has updated this TestDomain while you were editing.")
                    render(view:'edit',model:[testDomainInstance:testDomainInstance])
                    return
                }
            }
            testDomainInstance.properties = params
            if(!testDomainInstance.hasErrors() && testDomainInstance.save()) {
                flash.message = "TestDomain ${params.id} updated"
                redirect(action:show,id:testDomainInstance.id)
            }
            else {
                render(view:'edit',model:[testDomainInstance:testDomainInstance])
            }
        }
        else {
            flash.message = "TestDomain not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def testDomainInstance = new TestDomain()
        testDomainInstance.properties = params
        return ['testDomainInstance':testDomainInstance]
    }

    def save = {
        def testDomainInstance = new TestDomain(params)
        if(!testDomainInstance.hasErrors() && testDomainInstance.save()) {
            flash.message = "TestDomain ${testDomainInstance.id} created"
            redirect(action:show,id:testDomainInstance.id)
        }
        else {
            render(view:'create',model:[testDomainInstance:testDomainInstance])
        }
    }
}
