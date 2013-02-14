var database = function(){
	var CLASS = "database";

	var mongo = require('mongodb');
	var dbName = 'bloodhounDB';
	
	var Server = mongo.Server;
	var Db = mongo.Db;
	var BSON = mongo.BSONPure;
	
	var server = new Server('localhost', 27017,	{ auto_reconnect: true });
	var db = new Db(dbName, server);
	db.open(function(err, db)
			{
				var METHOD = CLASS + ".open: ";
				console.log(METHOD + "Connected to database <" + dbName + ">");
				if (!err)
					{
							/*
						db.collection(collectionNames.PHONE).drop();
						db.collection(collectionNames.SMS).drop();
						populateDB();
							 */
						
						/*
						 */
						db.collection(collectionNames.SMS,
							{ strict: true }, 
							function(err, collection)
								{
									if (err != null)
										{
											console.log("The " + collectionNames.PHONE  +" collection doesn't exist. Creating it with sample data...");
											//db.collection(smsCollection).drop();
											db.collection(collectionNames.PHONE).drop();
											populateDB();
										}
									else{
										console.log(METHOD + "Collection already present");
									}
								});
					}
			});
	

	/*--------------------------------------------------------------------------------------------------------------------*/
	// Populate database with sample data -- Only used once: the first time the application is started.
	// You'd typically not find this code in a real-life app, since the database would already exist.
	var populateDB = function()
		{
			populatePhonesCollection();
			//populateSmssCollection();
		};

	function populatePhonesCollection ()
		{
			var METHOD = CLASS + ".populatePhonesCollection: ";
			var phones = [
		    			{ imei: "123456789012340", name: "XXXX", phoneNumberSim1: "0123456789", phoneNumberSim2: ""},
		    			{ imei: "123456789012341", name: "SAMSUNG", phoneNumberSim1: "0123456789", phoneNumberSim2: ""},
		    			{ imei: "123456789012342", name: "HTC", phoneNumberSim1: "0123456789", phoneNumberSim2: ""}];
			              
			db.collection(collectionNames.PHONE, function(err, collection)
				{
					collection.insert(
					    phones,
						function(err, result)
							{
								console.log(METHOD + 'Populate phones collection: ' + phones.length);
								populateSmssCollection();
							}
					);
				});
			

		};
		
		
		function populateSmssCollection ()
			{
				var METHOD = CLASS + ".populateSmssCollection: ";
				var sms = [
			    			{ phone_id: null, direction: "outgoing", timespamp: new Date(Date.UTC(2013, 0, 20, 18, 15, 29, 0)), phoneNumber: "1111111111", text: "sms a 1111111111"},
			    			{ phone_id: null, direction: "incoming", timespamp: new Date(Date.UTC(2013, 0, 20, 20, 20, 29, 0)), phoneNumber: "1111111111", text: "sms da 1111111111"},
			    			{ phone_id: null, direction: "outgoing", timespamp: new Date(Date.UTC(2013, 0, 24, 20, 20, 29, 0)), phoneNumber: "2222222222", text: "sms a 22222222"},
			    			{ phone_id: null, direction: "incoming", timespamp: new Date(Date.UTC(2013, 0, 25, 10, 15, 29, 0)), phoneNumber: "2222222222", text: "sms da 222222"},
			    			{ phone_id: null, direction: "outgoing", timespamp: new Date(Date.UTC(2013, 0, 27, 27, 38, 29, 0)), phoneNumber: "1111111111", text: "sms a 1111111111"}
			    			];
				db.collection(
				              collectionNames.SMS, 
				              function(err, collectionSms)
				              	{
				    				db.collection(collectionNames.PHONE).find(
                                          {},
                                          function(err, cursorPhones)
                                          	{
                                          		var indexPhone = 0; 
                    							cursorPhones.each(
                    				                 function(err, phone) 
                    				                 	{
                    										if(phone != null) 
                    											{
                    												indexPhone++;
                    												console.log(METHOD);
                    												console.log(METHOD + indexPhone + ' phone : ' + JSON.stringify(phone));
                    												
                    												for (var i = 0; i < sms.length; i++)
                    													{
                    														console.log(METHOD + ' sms[' + i + "] = " + JSON.stringify(sms[i]));
                    														//sms[i].phone_id = phone._id;
                    														sms[i].phone_id = phone.imei;
                    														collectionSms.insert(
                    														                     { "phone_id": sms[i].phone_id, "direction": sms[i].direction, "timespamp": sms[i].timespamp, "phoneNumber": sms[i].phoneNumber, "text": sms[i].text},
                    														                     function(err, result)
                    														                     {
                    														                    	 console.log(METHOD + i + ' inserted sms: ' + JSON.stringify(sms[i]));
                    														                     });
                    													}		
                    											}
                    								}); //each
                                          	}); // collectionPhone.find
								}); //collectionSms
			};
					
			
	var showDB = function()
		{
			showPhones();
			showSmss();
		};
			

		function showPhones ()
			{
				var METHOD = CLASS + ".showPhones: ";
				console.log(METHOD + "PHONES:");
				db.collection(collectionNames.PHONE).find(
		  					                              {},
		  					                              function(err, cursorPhone)
		  					                              	{
			  					                              	var index = 0; 
			  					                              	cursorPhone.each(
			  					        				                 function(err, phone) 
			  					        				                 	{
			  					        										if(phone != null) 
			  					        											{
			  					        												index++;
			  					        												console.log(METHOD + index + "= " + JSON.stringify(phone));
			  					        											}
			  					        									});
		  					                              	});
			};
		
	function showSmss ()
		{
			var METHOD = CLASS + ".showSmss: ";
			console.log(METHOD + "SMS:");
			
			db.collection(collectionNames.SMS).find(
  					                              {},
  					                              function(err, cursorSms)
  					                              	{
  					                              		var index = 0; 
  					                              		cursorSms.each(
  					        				                 function(err, sms) 
  					        				                 	{
  					        										if(sms != null) 
  					        											{
  					        												index++;
  					        												console.log(METHOD + index + "= " + JSON.stringify(sms));
  					        											}
  					        									});
  					                              	});
		};

		
		
		
	//constant
	var collectionNames =  {
		PHONE: "phones",
		SMS: "sms"
	};

		
	
	//public
	return {
		collectionNames: collectionNames, 
		istanceDb: db
	};
}();


module.exports = database;



