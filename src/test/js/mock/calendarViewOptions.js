module.exports = function() {
  var CalendarViewOptions = {
    formats: {
      month: {
        popupBuildTimeFormat: 'YY-MM-DD'
      }
    },
    popupText: {
      project: 'ppp',
      build: 'bbb',
      nextScheduledBuild: 'nxb',
      buildHistory: 'bHist',
      buildHistoryEmpty: 'bEmpty'
    }
  };
  global.CalendarViewOptions = CalendarViewOptions;
  return CalendarViewOptions;
};
