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
									console.log(METHOD + "Errore:" + err);
									var phone ={ _id: 0, imei: "", name: "", phoneNumberSim1: "", phoneNumberSim2: ""}; 
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
										console.log(METHOD + "Errore:" + err);
										var phone ={_id: 0, imei: "", name: "", phoneNumberSim1: "", phoneNumberSim2: ""}; 
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
											var item={_id: 0, imei: "", name: "", phoneNumberSim1: "", phoneNumberSim2: ""};
											callback(err,  items);
										}
								});
						});
				};			
				

			var create= function (phone, callback)
				{
					var METHOD = CLASS + ".create: ";
					console.log(METHOD + "Creating phone: " + JSON.stringify(phone));
					database.istanceDb.collection(database.collectionNames.PHONE, function(err, collection)
						{
							collection.insert(phone, function(err, result)
								{
									if (!err)	
										{
											var phone = result[0];
											console.log(METHOD + 'Phone created: ' + JSON.stringify(phone));
											callback(null,  phone);
										}
									else
										{
											console.log(METHOD + "Errore: " + err);
											var phone = {_id: 0, imei: "", name: "", phoneNumberSim1: "", phoneNumberSim2: ""};
											callback(err,  phone);
										}
								});
						});
				};			


			var remove = function (id, callback)
				{
					var METHOD = CLASS + ".remove: ";
					console.log(METHOD + "Deleting phone with id: " + id);

					database.istanceDb.collection(
						database.collectionNames.PHONE, 
						function(err, collection){
							collection.remove(
								{ '_id': new BSON.ObjectID(id) }, 
								function(err)
									{
										if (!err)
											{
												console.log(METHOD + "Phone deleted");
											}
										else
											{
												console.log(METHOD + "Errore:" + err);
											}
										callback(err);
							});
						});					
				};			

				
		//metodi pubblici
		return {
			//findById: findById,
			findByImei: findByImei,
			findAll: findAll,
			create: create,
			remove: remove
		}
}();


module.exports = phoneDb;
	
