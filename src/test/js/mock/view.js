var moment = require('moment');

module.exports = function(builds) {
  return {
    type: 'month',
    intervalStart: moment('2018-06-01'),
    intervalEnd: moment('2018-07-01'),
    calendar: {
      gotoDate: function() { },
      clientEvents: function(fn) { return builds.filter(fn); }
    }
  };
};
