"use strict"
const vm = require('vm');

class Activity {
    /**
     * 
     * @param {*} tName New type name for the activity
     * @param {*} rFunction Restriction function
     * @param {*} mainFunction Main function
     * @param {*} cFunction Compatibility function
     * @param {*} context Context for the preparation, execution and publication 
     */
    constructor(tName, rFunction, mainFunction, cFunction, context = {}){
        this._templateName = tName; // !This will be the field used for matching agains the 'type'
        this._restrictionFunction = rFunction; // !Comprobes and prepare the resources needed for its execution
        this._mainFunction = mainFunction; // ! Main function which defines the logic of the activity
        this._compatibilityFunction = cFunction; // !Prepare its output for the next activity
        /**
         * Context containing the required resources for executing the activity
         * ! For the compatibility function it may contains the following variables defined:
         *      - nextActivity: A description of the following activity
         */
        this._sContext = context; // The context for the VM
    }

    match(tName){
        return this._templateName === tName;
    }

    set context(ctx) {
        ctx.result = null;
        this._context = vm.createContext(ctx);
    }

    /**
     * Set the initial context and executes the restriction function.
     */
    prepare(){
        this.context = this._sContext;
        vm.runInContext(this._restrictionFunction, this._context);
        if(!this._context.result)
            throw new Error("Cannot prepare the activity!");
        
    }

    /**
     * Executes the main function in the prepared context
     */
    execute() {
        vm.runInContext(this._mainFunction, this._context);
    }

    /**
     * Prepare the result for the next activity
     */
    publish() {
        this._context.result = null;
        vm.runInContext(this._compatibilityFunction, this._context);
        if(!this._context.result)
            throw new Error("Cannot prepare the resources for the next activity, it is not compatible.");
    }

    get result() {
        return typeof(this._context.result) != "undefined" ? this._context.result : null;
    }
}

module.exports = Activity;