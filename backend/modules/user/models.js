/*
 * TSB - User/Models.JS file
 * Defines the TSB Backend's database models for user related transactions
 */

var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var userSchema = mongoose.Schema({
    device_uuid : String,
    device_os : String, 

    hashed_passphrase: String,
    salt : String,

    session_token : String,
    sesion_token_expires : Date,

    register_date : Date,

    //add more fields for various stuff

});


// export the different schemas as models
module.exports = {
    users : mongoose.model('users', userSchema),
};
