
export function parseHashParams(hash) {
  const hashParams = {};
  if (hash) {
    hash.replace('#', '').split(/\|/).forEach(hashParam => {
      const keyAndValue = hashParam.split(/=/);
      const key = keyAndValue[0];
      const value = keyAndValue[1];
      hashParams[key] = value;
    });
  }
  return hashParams;
}

export function serializeHashParams(hashParams) {
  var params = [];
  for (var key in hashParams) {
    params.push(key + '=' + hashParams[key]);
  }
  return '#' + params.join('|');
}

