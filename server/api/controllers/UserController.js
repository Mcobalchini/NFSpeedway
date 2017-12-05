/**
 * UserController
 *
 * @description :: Server-side logic for managing users
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */

module.exports = {
    connection: 'mongo',
    attributes: {
        //dados que nãos o que são 
        name: {
            type: 'string'
        }
    },
    // Da pra fazer algo aqui
    beforeCreate: (values, next) => {
        console.loog(values);
        next();
    },
    // aqui também.
    afterPublishUpdate: (id, changes, req, options) => {
        console.loog(values);
        next();
    }
};

