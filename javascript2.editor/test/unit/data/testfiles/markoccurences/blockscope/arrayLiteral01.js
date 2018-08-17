"use strict";

function _getFileList(buildType, fileList, theme) {
  let result = [];
  for (const file of fileList) {
    if (file.buildType === buildType || !file.buildType) {
      let matches = [];
      let dest;
      let cwd;
      for (let src of file.src) {
        const exclusion = src.indexOf('!') === 0;
        cwd = file.cwd;
        dest = file.dest;
        cwd = (_isFunction(file.cwd) ? cwd(theme) : cwd) || '';

        if (exclusion) { src = src.slice(1); }

        let match = glob.sync(src, { cwd: util.destPath(cwd) });
        match = _removeNonFile(match, util.destPath(cwd));
        if (exclusion) {
          matches = _difference(matches, match);
        } else {
          matches = _union(matches, match);
        }
      }
      const prefixedPaths = _addFileListPathPrefix(matches, dest, util.destPath(cwd));
      result = result.concat(prefixedPaths);
    }
  }
  return result;
} 