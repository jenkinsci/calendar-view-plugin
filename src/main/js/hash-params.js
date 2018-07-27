/*
 * The MIT License
 *
 * Copyright (c) 2018 Sven Schoenung
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

export function parse(hash) {
  const hashParams = {};
  if (hash) {
    hash.replace('#', '').split(/\|/).forEach(hashParam => {
      const keyAndValue = hashParam.split(/=/);
      const key = keyAndValue[0];
      const value = keyAndValue[1];
      if (value !== undefined) {
        hashParams[key] = value;
      }
    });
  }
  return hashParams;
}

export function serialize(hashParams) {
  var params = [];
  for (var key in hashParams) {
    if (hashParams[key] !== undefined && hashParams[key] !== null) {
      params.push(key + '=' + hashParams[key]);
    }
  }
  return '#' + params.join('|');
}
