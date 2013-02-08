var phoneDb = function (){
		var CLASS = "phoneDb";
	
		var database = require('./database');
		var BSON = require('mongodb').BSONPure;

		var findById = function(id, callback)
		{
			var METHOD = CLASS + ".findById: ";
			console.log(METHOD + "Retrieving phone: " + id);
			database.istanceDb.collection(database.collectionNames.PHONE, function(err, collection)
				{
					collection.findOne(
						{ '_id': new BSON.ObjectID(id) }, function(err, phone)
						{
							if (!err)
								{
									console.log(METHOD + "Retrieved phones: " + JSON.stringify(phone));
									callback(null,  phone);
								}
							else
								{
									console.log("Errore:" + err);
									var phone ={ imei: "", name: "", phoneNumberSim1: "", phoneNumberSim2: ""}; 
									callback(err,  phone);
								}
						});
				});
		};		
		
		
		var findByImei = function(imei, callback)
			{
				var METHOD = CLASS + ".findByIMei: ";
				console.log(METHOD +"Retrieving phone by imei: " + imei);
				
				database.istanceDb.collection(database.collectionNames.PHONE, function(err, collection)
					{
						collection.findOne(
							{ 'imei': imei }, function(err, phone)
							{
								if (!err)
									{
										console.log(METHOD + "Retrieved phones: " + JSON.stringify(phone));
										callback(null,  phone);
									}
								else
									{
										console.log("Errore:" + err);
										var phone ={ imei: "", name: "", phoneNumberSim1: "", phoneNumberSim2: ""}; 
										callback(err,  phone);
									}
							});
					});

			};

			var findAll= function (callback)
				{
					var METHOD = CLASS + ".findAll: ";
					console.log(METHOD + "Retrieving all phones" );

					database.istanceDb.collection(database.collectionNames.PHONE, function(err, collection)
						{
							collection.find().toArray(function(err, items)
								{
									if (!err)	
										{
											console.log(METHOD + "Retrieved " + items.length + " phones: " + JSON.stringify(items));
											callback(null,  items);
										}
									else
										{
											console.log(METHOD + "Errore:" + err);
											var item={ imei: "", name: "", phoneNumberSim1: "", phoneNumberSim2: ""};
											callback(err,  items);
										}
								});
						});
				};			
				
				
		//metodi pubblici
		return {
			findById: findById,
			findByImei: findByImei,
			findAll: findAll
		}
}();


module.exports = phoneDb;
	
