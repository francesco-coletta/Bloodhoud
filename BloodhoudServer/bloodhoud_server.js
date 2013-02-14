/*
 * Bloodhound Server
 * 
 * request by curl curl http://192.168.137.129:1337
 * 
 * send file with curl and cygwin
 * 
 * curl -T ./prociv.png http://192.168.137.129:1337 curl -T ./prociv.tif http://192.168.137.129:1337 curl -T ./prociv.png http://192.168.137.129:1337 --trace
 * ./curl.log
 * 
 * 
 */

/*
 * carico i moduli nativi necessari
 */

// Sinatra inspired web development framework
var express = require('express');
var phone = require('./routes/phone');
var sms = require('./routes/sms');
var call = require('./routes/call');

// crea una applicazione
var app = express();


app.configure(
	function () {
		app.use(express.logger('dev')); /* 'default', 'short', 'tiny', 'dev' */
		app.use(express.bodyParser());
	}
);


// PHONE
app.get('/phones', phone.find);
app.get('/phones/phone-:imei', phone.find);

//info del generico telefono
//app.get('/phones/phone-:id', phone.findById);


// SMS
//tutti gli sms
app.get('/phones/smss', sms.findAll);


/*
 * sms di un telefono che rispettano determinate condizioni
 * I parametri della query string possono essere:
 * - day=yyyy-mm-dd
 * - interval[start]=yyyy-mm-dd&interval[end]=yyyy-mm-dd
 * - direction=outgoing/incoming
 * - phoneNumber=1234567890
 * 
 */
app.get('/phones/phone-:imei/smss', sms.find);


//dettaglio di un sms di un telefono
//app.get('/phones/phone-:id/smss/sms-:idSms', sms.findById);



// ADD new phone
/*
 * nel post vanno specificati i seguenti dati
 * imei=123456789012341
 * name=tizio
 * phoneNumberSim1=0123456789
 * phoneNumberSim2=0123456789
*/
app.post('/phones', phone.create);

// DELETE phone
//app.delete('/phones/phone-:imei', phone.remove);


// ADD new SMS
/*
 * nel post vanno specificati i seguenti dati
 * direction=outgoing/incoming
 * phoneNumber=0123456789
 * timespamp=YYYY-MM-DDTHH:mm:ss.000Z (UTC)
 * text=
*/
app.post('/phones/phone-:imei/smss', sms.create);



var INADDR_ANY = '0.0.0.0';
var serverIp = INADDR_ANY; // '127.0.0.1';
var serverPort = 1337;
app.listen(serverPort);
console.log("> SERVER STARTED");
console.log("> SERVER LISTENING at http://" + serverIp + ":" + serverPort);
