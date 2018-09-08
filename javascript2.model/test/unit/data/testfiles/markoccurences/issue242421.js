var myF = function MyLib_Function (path, ref, pfx, options) {
   var v = expand(path, ref, pfx, options);
    
   if( options && options.option1 && (v === path) )
       return path;

   v = page.root + '~/' + v;

   return v;
};