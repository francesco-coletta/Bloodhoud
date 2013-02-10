Richieste per testare i servizi RESTfull:

- recupera tutti i phone:
http://192.168.137.129:1337/phones
curl -i -X GET http://localhost:1337/phones

- recupera u dettagli di un phone (idPhone = _id):
http://192.168.137.129:1337/phones/phone-{idPhone}
curl -i -X GET http://localhost:1337/phones/phone-{idPhone}

- recupera tutti gli sms di tutti gli phone:
http://192.168.137.129:1337/phones/smss
curl -i -X GET http://localhost:1337/phones/smss


- recupera tutti gli sms di un phone (idPhone = _id):
http://192.168.137.129:1337/phones/phone-{idPhone}/smss
curl -i -X GET http://localhost:1337/phones/phone-{idPhone}/smss

- recupera tutti gli sms di tutti gli phone * sms di un telefono che rispettano determinate condizioni
I parametri della query string possono essere:
 - day=yyyy-mm-dd
 - interval[start]=yyyy-mm-dd&interval[end]=yyyy-mm-dd
 - direction=outgoing/incoming
 - phoneNumber=1234567890
http://192.168.137.129:1337/phones/phone-{idPhone}/smss?interval[start]=2013-01-24&interval[end]=2013-01-25
http://192.168.137.129:1337/phones/phone-{idPhone}/smss?day=2013-01-24&direction=outgoing&phoneNumber=1234567890

curl -i -X GET  "http://localhost:1337/phones/phone-{idPhone}/smss?day=2013-01-20&direction=outgoing"
curl -i -X GET "http://localhost:1337/phones/phone-{idPhone}/smss?interval[start]=2013-01-24&interval[end]=2013-01-25"
curl -i -X GET "http://localhost:1337/phones/phone-{idPhone}/smss?day=2013-01-24&direction=outgoing&phoneNumber=1234567890"


curl -i -X GET "http://localhost:1337/phones/phone-{idPhone}/smss?interval%5Bstart%5D=2013-01-24&interval%5Bend%5D=2013-01-25"
NB: le parentesi quadre vanno sostituite con il loro encode
[=%5B
]=%5D



POST
-crea un nuovo phone
curl -i -H "Accept: application/json" -d "imei=123456789012341&name=tizio&phoneNumberSim1=0123456789&phoneNumberSim2=0123456789"  -X POST  "http://localhost:1337/phones"


DELETE
-elimina un phone
curl -i -X DELETE "http://localhost:1337/phones/phone-{idPhone}"
