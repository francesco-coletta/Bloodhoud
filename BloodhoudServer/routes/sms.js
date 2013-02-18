var sms = function (){
		var CLASS = "sms";

		var smsDb = require('./smsDb');
		var phoneDb = require('./phoneDb');
		
		/*
		var findById = function(request, response)
		{
			var METHOD = CLASS + ".findById: ";
					
			var id = request.params.id;
			smsDb.findById(id, function(err, sms){
						console.log(METHOD + 'Retrieved sms: ' + JSON.stringify(sms));
						response.send(sms);
			});
		};
		*/		
			


		var findAll = function(request, response)
			{
				var METHOD = CLASS + ".findAllByIdPhone: ";
				
				console.log(METHOD + 'Retrieved all sms');
				
				smsDb.findAll(
					function(err, sms)
						{
						console.log(METHOD + "Retrieved " + sms.length + " sms");
						response.send(sms);
				});
			};

		/*
		var findAllByIdPhone = function(request, response)
			{
				var METHOD = CLASS + ".findAllByIdPhone: ";
				
				var id = request.params.id;
				console.log(METHOD + 'Retrieved sms for phone with id: ' + id);
				
				smsDb.findAllByIdPhone(
					id, 
					function(err, sms)
						{
						console.log(METHOD + "Retrieved " + sms.length + " sms");
						response.send(sms);
				});
			};
		*/
			
		/*
		 * sms di un telefono che rispettano determinate condizioni
		 * I parametri della query string possono essere:
		 * - day=yyyy-mm-dd
		 * - interval[start]=yyyy-mm-dd&interval[end]=yyyy-mm-dd
		 * - direction=outgoing/incoming
		 * - phoneNumber=1234567890
		 * 
		 */
		var find = function(request, response)
			{
				var METHOD = CLASS + ".find: ";
			
				//var idPhone = request.params.id;
				var idPhone = request.params.imei;
				console.log(METHOD + 'Retrieve sms for phone with id: ' + idPhone);
					
				var day = request.query.day;
				if (typeof day !== 'undefined'){
					console.log(METHOD + 'Retrieve sms in day: ' + day);
				}
				
				var interval = request.query.interval;
				if (typeof interval !== 'undefined'){
					console.log(METHOD + 'Retrieve sms into interval: ' + JSON.stringify(interval));
				}
				
				var direction = request.query.direction;
				if (typeof direction !== 'undefined'){
					console.log(METHOD + 'Retrieve sms with direction: ' + direction);
				}
				
				var phoneNumber = request.query.phoneNumber;
				if (typeof phoneNumber !== 'undefined'){
					console.log(METHOD + 'Retrieve sms from/to number: ' + phoneNumber);
				}
				
				var params = {
					idPhone: idPhone,
					day: day,
					interval: interval,
					direction: direction,
					phoneNumber: phoneNumber
				}
				
				smsDb.find(
					params,
					function(err, sms)
						{
							response.send(sms);
						}
				);
			};	
			
		var create = function(request, response)
			{
				var METHOD = CLASS + ".create: ";
				
				//var idPhone = request.params.id;
				var idPhone = request.params.imei;
				console.log(METHOD + 'Creating sms for phone with id: ' + idPhone);

 				var sms = request.body;
				console.log(METHOD + "Creating " + JSON.stringify(sms));
				
				sms.phone_id = idPhone;
				
				var newSms = sms;
				smsDb.create(
					sms, 
					function(err, sms)
						{
							console.log(METHOD + "Creato nuovo sms: " + JSON.stringify(sms));
							response.send(sms);
							newSms = sms;
						}
				);
				return newSms;
			}

			
		//metodi pubblici
		return {
			findAll: findAll,
			//findAllByIdPhone: findAllByIdPhone,
			find: find,
			create: create
		}
}();

	
module.exports = sms;


	
	