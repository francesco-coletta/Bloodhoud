var call = function() {
	var CLASS = "call";

	var callDb = require('./callDb');
	var phoneDb = require('./phoneDb');

	var findAll = function(request, response) {
		var METHOD = CLASS + ".findAllByIdPhone: ";

		console.log(METHOD + 'Retrieved all call');

		callDb.findAll(function(err, call) {
			console.log(METHOD + "Retrieved " + call.length + " call");
			response.send(call);
		});
	};

	/*
	 * Call di un telefono che rispettano determinate condizioni I parametri
	 * della query string possono essere: - day=yyyy-mm-dd -
	 * interval[start]=yyyy-mm-dd&interval[end]=yyyy-mm-dd -
	 * direction=outgoing/incoming - phoneNumber=1234567890
	 * 
	 */
	var find = function(request, response) {
		var METHOD = CLASS + ".find: ";

		var idPhone = request.params.imei;
		console.log(METHOD + 'Retrieve call for phone with id: ' + idPhone);

		var day = request.query.day;
		if (typeof day !== 'undefined') {
			console.log(METHOD + 'Retrieve call in day: ' + day);
		}

		var interval = request.query.interval;
		if (typeof interval !== 'undefined') {
			console.log(METHOD + 'Retrieve call into interval: '
					+ JSON.stringify(interval));
		}

		var direction = request.query.direction;
		if (typeof direction !== 'undefined') {
			console.log(METHOD + 'Retrieve call with direction: ' + direction);
		}

		var phoneNumber = request.query.phoneNumber;
		if (typeof phoneNumber !== 'undefined') {
			console
					.log(METHOD + 'Retrieve call from/to number: '
							+ phoneNumber);
		}

		var params = {
			idPhone : idPhone,
			day : day,
			interval : interval,
			direction : direction,
			phoneNumber : phoneNumber
		}

		callDb.find(params, function(err, call) {
			response.send(call);
		});
	};

	var create = function(request, response) {
		var METHOD = CLASS + ".create: ";

		var idPhone = request.params.imei;
		console.log(METHOD + 'Creating call for phone with id: ' + idPhone);

		var call = request.body;
		console.log(METHOD + "Creating " + JSON.stringify(call));

		call.phone_id = idPhone;

		callDb.create(call, function(err, call) {
			console.log(METHOD + "Creato nuovo call: " + JSON.stringify(call));
			response.send(call);
		});
	}

	// metodi pubblici
	return {
		findAll : findAll,
		find : find,
		create : create
	}
}();

module.exports = call;
