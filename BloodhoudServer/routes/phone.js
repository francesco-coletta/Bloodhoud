var phone = function (){
		var CLASS = "phone";

		var phoneDb = require('./phoneDb');

		var findById = function(request, response)
		{
			var METHOD = CLASS + ".findById: ";
					
			var id = request.params.id;
			var phone = phoneDb.findById(id, function(err, phone){
						console.log(METHOD + 'Retrieved phone: ' + JSON.stringify(phone));
						response.send(phone);
			});
		};		
			
		var find = function(request, response)
		{
			var METHOD = CLASS + ".find: ";
		
			var imei= request.query.imei;
			if (typeof imei !== "undefined")
				{
					findByImei(imei, response);
				}
			else
				{
					findAll(response);
				}
		};
	
		var findByImei = function(imei, response){
				var METHOD = CLASS + ".findByImei: ";
				
				phoneDb.findByImei(imei, function(err, phone){
						console.log(METHOD + 'Retrieved phone: ' + JSON.stringify(phone));
						response.send(phone);
				});
			};

		var findAll = function(response)
			{
				var METHOD = CLASS + ".findAll: ";
				
				phoneDb.findAll(function(err, phones){
						console.log(METHOD + "Retrieved " + phones.length + " phones");
						response.send(phones);
				});
			};
			
			
		var create = function(request, response)
			{
				var METHOD = CLASS + ".create: ";
				
 				var phone = request.body;
				console.log(METHOD + "Creating " + JSON.stringify(phone));
				
				phoneDb.create(
					phone, 
					function(err, phone)
						{
							console.log(METHOD + "Creato nuovo phone: " + JSON.stringify(phone));
							response.send(phone);
						}
				);
			}
			
		var remove = function(request, response)
			{
				var METHOD = CLASS + ".remove: ";
				
 				var id = request.params.id;
				console.log(METHOD + "Deleting phone with id: " + id);
				
				phoneDb.remove(
					id, 
					function(err)
						{
							if (!err){
								console.log(METHOD + "Deleted phone with id: " + id);
								response.send("OK");
							}
							else{
								console.log(METHOD + "Deleted phone with id: " + id);
								response.send("KO");
							}
						}
				);
			} 
			 

		//metodi pubblici
		return {
			findById: findById,
			find: find,
			create: create,
			remove: remove
		}
}();

	
module.exports = phone;


	
	