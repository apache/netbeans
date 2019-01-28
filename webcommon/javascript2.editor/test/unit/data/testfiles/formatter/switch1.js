switch (tagName) {
  case 'XXX':
       case 'THEAD':
    case 'TBODY':
        {
     div.innerHTML = '<table><tbody>' +  html.stripScripts() + '</tbody></table>';
            depth = 2;
            break;
           // test

        }

         //ee

    case 'TR':
      div.innerHTML = '<table><tbody><tr>' +  html.stripScripts() + '</tr></tbody></table>';
      depth = 3;
      break;
    case 'TD':
      div.innerHTML = '<table><tbody><tr><td>' +  html.stripScripts() + '</td></tr></tbody></table>';
      depth = 4;
    default:
                //fsdfsdfsd
        break;
       }
