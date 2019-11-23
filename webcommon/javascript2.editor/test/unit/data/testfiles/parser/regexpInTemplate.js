const url = `http://${host}${route}`;
const dir = `build/public${route.replace(/[^\/]*$/, '')}`;
const name = route.endsWith('/') ? 'index.html' : `${route.match(/[^/]+$/)[0]}.html`;