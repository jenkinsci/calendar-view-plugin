'use strict';

/* global describe:false, it:false */

require('babel-register')({ presets: [ 'env' ] });

var chai = require('chai');
var expect = chai.expect;

var hashParams = require('../../main/js/hash-params.js');

describe('hashParams.parse()', function() {
  it('should return empty object for null/undefined', function() {
    expect(hashParams.parse(null)).to.eql({});
    expect(hashParams.parse(undefined)).to.eql({});
  });

  it('should return object with single property for string with single param', function() {
    expect(hashParams.parse('param1=val1')).to.eql({param1: 'val1'});
    expect(hashParams.parse('#param1=val1')).to.eql({param1: 'val1'});
  });

  it('should return object with multiple properties for string with multiple params', function() {
    expect(hashParams.parse('param1=val1|param2=val2|param3=val3')).to.eql({
      param1: 'val1',
      param2: 'val2',
      param3: 'val3'
    });

    expect(hashParams.parse('#param1=val1|param2=val2|param3=val3')).to.eql({
      param1: 'val1',
      param2: 'val2',
      param3: 'val3'
    });
  });

  it('should not return properties for params without value', function() {
    expect(hashParams.parse('param1')).to.eql({});
    expect(hashParams.parse('param1|param2|param3')).to.eql({});
    expect(hashParams.parse('param1|param2=val2|param3')).to.eql({param2: 'val2'});

    expect(hashParams.parse('#param1')).to.eql({});
    expect(hashParams.parse('#param1|param2|param3')).to.eql({});
    expect(hashParams.parse('#param1|param2=val2|param3')).to.eql({param2: 'val2'});
  });

  it('should return value for later parameter if two parameters have the same name', function() {
    expect(hashParams.parse('param1=val1|param1=val2')).to.eql({ param1: 'val2' });
    expect(hashParams.parse('#param1=val1|param1=val2')).to.eql({ param1: 'val2' });
  });
});

describe('hashParams.serialize()', function() {
  it('should return string with only a hash sign for empty object/null/undefined', function() {
    expect(hashParams.serialize(null)).to.eql('#');
    expect(hashParams.serialize(undefined)).to.eql('#');
    expect(hashParams.serialize({})).to.eql('#');
  });

  it('should return string with one param for object with single property', function() {
    expect(hashParams.serialize({params1: 'val1'})).to.eql('#params1=val1');
  });

  it('should return string with multiple params for object with multiple properties', function() {
    expect(hashParams.serialize({params1: 'val1', params2: 'val2', params3: 'val3'})).to.be.oneOf([
      '#params1=val1|params2=val2|params3=val3',
      '#params1=val1|params3=val3|params2=val2',
      '#params2=val2|params1=val1|params3=val3',
      '#params2=val2|params3=val3|params1=val1',
      '#params3=val3|params1=val1|params2=val2',
      '#params3=val3|params2=val2|params1=val1'
    ]);
  });

  it('should not include properties with value undefined or null', function() {
    expect(hashParams.serialize({params1: 'val1', params2: undefined})).to.be.eql('#params1=val1');
    expect(hashParams.serialize({params1: 'val1', params2: null})).to.be.eql('#params1=val1');
  });
});
