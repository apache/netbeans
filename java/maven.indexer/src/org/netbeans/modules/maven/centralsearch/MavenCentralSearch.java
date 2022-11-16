
package org.netbeans.modules.maven.centralsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 *
 * @author mbien
 */
class MavenCentralSearch {

    private static final HttpClient client = HttpClient.newHttpClient();

    private static final int MAX_ROWS = 200; // seems to be the current limit, larger values fall back to 20

    private static final String SERVICE = "https://search.maven.org/solrsearch/select";
    private static final String OPTS = "&rows="+MAX_ROWS+"&wt=json";

    public MavenCentralSearch() {}

    public JSONSearchResponse findArtifactsByClass(String clazz) throws IOException {
        return find(URI.create(SERVICE + "?q=c:"+clazz+OPTS));
    }

    public JSONSearchResponse findArtifactsByFQClass(String fqClazz) throws IOException {
        return find(URI.create(SERVICE + "?q=fc:"+fqClazz+OPTS));
    }

    public JSONSearchResponse findArtifactVersions(String group, String artifact) throws IOException {
        return find(URI.create(SERVICE + "?q=g:"+group+"+AND+a:"+artifact+OPTS+"&core=gav"));
    }

    public JSONSearchResponse findArtifact(String group, String artifact) throws IOException {
        return find(URI.create(SERVICE + "?q=g:"+group+"+AND+a:"+artifact+OPTS));
    }

    public JSONSearchResponse findArtifactsByName(String artifact) throws IOException {
        return find(URI.create(SERVICE + "?q=a:"+artifact+OPTS));
    }

    public JSONSearchResponse findArtifactsByGroup(String group) throws IOException {
        return find(URI.create(SERVICE + "?q=g:"+group+OPTS));
    }

    private JSONSearchResponse find(URI request) throws IOException {
        try {
            HttpResponse<InputStream> response = client.send(HttpRequest.newBuilder(request).build(), HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.treeToValue(mapper.readTree(body).at("/response"), JSONSearchResponse.class);
            }
        } catch (InterruptedException ex) {
            return null;
        }
    }
}
