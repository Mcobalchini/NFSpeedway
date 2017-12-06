/**
 * User.js
 *
 * @description :: TODO: You might write a short summary of how this model works and what it represents here.
 * @docs        :: http://sailsjs.org/documentation/concepts/models-and-orm/models
 */

module.exports = {
  connection: 'mongo',
  attributes: {
      //dados que nãos o que são 
      codigoFb : {
        type: 'string'
      },
      nome : {
        type: 'string'
      },
      velocidade : {
        type: 'string'
      }
  },
  // Da pra fazer algo aqui
  beforeCreate: (values, next) => {
      console.log(values);
      next();
  },
  // aqui também.
  afterPublishUpdate: (id, changes, req, options) => {
      console.log(values);
      next();
  }
};

