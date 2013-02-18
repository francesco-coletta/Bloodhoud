var bloodhoud_ws_server = function() {
	var CLASS = "bloodhoud_ws_server";

	var phoneDb = require('./routes/phoneDb');
	var smsDb = require('./routes/smsDb');
	var callDb = require('./routes/callDb');
	
	var connect = function(socket) {
		var METHOD = CLASS + ".connect: ";
		console.log(METHOD + 'Connect');
		socket.emit('welcome', { msg: 'server ready' });
	}	

	var clientReady = function(socket, data) {
		var METHOD = CLASS + ".clientReady: ";
		console.log(METHOD + 'data:' + JSON.stringify(data));
		
		// sends a list of phone
		phoneDb.findAll(function(err, phones){
			console.log(METHOD + "Retrieved " + phones.length + " phones");
			socket.emit('phonesList', { phones: phones });
		});
	}	
	
	var sendTodaySms = function(socket, data) {
		var METHOD = CLASS + ".sendTodaySms: ";
		console.log(METHOD + 'data:' + JSON.stringify(data));
		
		smsDb.findAllToday(function(err, sms){
			console.log(METHOD + "Retrieved " + sms.length + " sms");
			socket.emit('todaySmsList', { sms: sms });
		});
	}	

	var sendTodayCall = function(socket, data) {
		var METHOD = CLASS + ".sendTodayCall: ";
		console.log(METHOD + 'data:' + JSON.stringify(data));
		
		callDb.findAllToday(function(err, call){
			console.log(METHOD + "Retrieved " + call.length + " call");
			socket.emit('todayCallList', { call: call });
		});
	}	
	
	var disconnect = function(socket) {
		var METHOD = CLASS + ".disconnect: ";
		console.log(METHOD + 'disconnect');
		socket.send('by', { by: 'by' });
	}	
	
	
	// metodi pubblici
	return {
		connect: connect,
		clientReady: clientReady,
		sendTodaySms: sendTodaySms,
		sendTodayCall: sendTodayCall,
		disconnect: disconnect
	}

}();

module.exports = bloodhoud_ws_server;
