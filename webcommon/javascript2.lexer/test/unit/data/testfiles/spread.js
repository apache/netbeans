const versionsMap = new Map();
lines.forEach( line => versionsMap.set(...line.split('@')) );