define([], function() {

    /**
     * Transaction state
     */
    return {
        /**
         * New transaction not yet commited.
         */
        UNCOMMITED: "UNCOMMITED",
        
        /**
         * Transaction has been successfully commited.
         */
        COMMITED: "COMMITED",
        
        /**
         * Transaction commit failed, but previous successfull operations were rolled back.
         */
        COMMIT_FAILED: "COMMIT_FAILED",
        
        /**
         * An error state: Commit failed and also the previously successully applied operation
         * couldn't be reverted. 
         * 
         * The system become inconsistent.
         */
        COMMIT_FAILED_ERROR: "COMMIT_FAILED_ERROR",
        
        /**
         * Transaction has been successfully rolled back after previous successfull commit.
         */
        ROLLEDBACK: "ROLLEDBACK",
        
        /**
         * Failed rollback.
         */
        ROLLBACK_FAILED: "ROLLBACK_FAILED",
        
        /**
         * Failed rollback, failed to reapply previously successfully reverted operations.
         * 
         * The system become inconsistent.
         */
        ROLLBACK_FAILED_ERROR: "ROLLBACK_FAILED_ERROR",

    };
    
});