require('babel-register')({ presets: [ 'env' ] })

var JSDOM = require('jsdom').JSDOM;
var mockery = require('mockery');
if (!global.jquery) {
  global.jquery = require('jquery');
} 

function mockJquery(html) {
  var w = new JSDOM(html).window;

  mockery.enable({ useCleanCache: true, warnOnReplace: false });
  mockery.warnOnUnregistered(false);
  mockery.registerMock('jquery', global.jquery(w));

  global.window = w;
  global.navigator = w.navigator;
  global.Element = w.Element;
  global.document = w.document;
}

module.exports = mockJquery;
