(function() {
	angular.module("Outlier", []);

	angular.module("Outlier")
			.controller("OutlierController", OutlierController);

	function OutlierController($http, $q) {
		var _this = this;
		_this.rows = [];
		_this.loadContent = loadContent;

		function loadContent() {
			if (! _this.proc_code || !_this.percentage)
				return;
			
			$http.get(
					"result-json?proc_code=" + _this.proc_code + "&percentage="+ _this.percentage).success(
			function(data) {
				console.log(data);
				_this.rows = data;
			})
		}
	}
})();