/*
 * TSB - Routes.JS file 
 * Defines the overall form of our API
 */

// modules
var register = require('./modules/user/register');
var login = require('./modules/user/login');
var order = require('./modules/delivery/order');
var middleware = require('./modules/support/middleware');

module.exports = function(app) {
    app.get('/', function(req, res) {
	res.end("TSB Backend v0.0.1");
    });

    // authenticate with the backend by supplying
    //   - a persistent device uuid
    //   - an auth_token that was given by the backend
    app.post('/api/login',function(req,res){
	
	var uuid = req.body.device_uuid;
    var passphrase = req.body.passphrase;
	
		login.login(uuid, passphrase, function (result, status) {
		    console.log(result);
		    res.status(status).json(result);
		});
    
    });
    
    // register with the backend by supplying 
    //     - a (persistent) device uuid
    //     - a signup_code that is distributed to each user
    app.post('/api/register',function(req,res){
	
	var device_uuid = req.body.device_uuid;
    var passphrase = req.body.passphrase;
	var signup_token = req.body.signup_token;
	var device_os = req.body.device_os;
	
	register.register(device_uuid, passphrase, signup_token, device_os, function (result, status) {
	    console.log(result);
	    res.status(status).json(result);
		});
    });


    // request a delivery 

    app.post('/api/delivery/request', middleware.is_authenticated, function(req, res) {
	var session_token = req.body.session_token;
	var dorm_name = req.body.dorm_name;
	var dorm_room = req.body.dorm_room;
	var delivery_type = req.body.delivery_type;
	var coordinates = req.body.coordinates;
	
	order.request(session_token, dorm_name, dorm_room, delivery_type, coordinates, function (result, status) {
				  console.log(result, status);
				  res.status(status).json(result);
		      });
    });

    // get the delivery status

    app.post('/api/delivery/status', middleware.is_authenticated_and_requester, function(req, res) {
	var session_token = req.body.session_token;
	var order_number = req.body.order_number;
	
	order.status(order_number, function (result, status) {
	    console.log(result);
	    res.status(status).json(result);
		});

    });

	
};
