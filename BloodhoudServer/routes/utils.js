var util = function() {
	var CLASS = "util";

	var returnResponse = function(code, message) {
		return {'code': code, 'message': message};
	};
	
	var returnResponse = function(code, message, object) {
		return {'code': code, 'message': message, 'object': object};
	};	

	// metodi pubblici
	return {
		returnResponse: returnResponse
	}
}();

module.exports = util;
