package modkiwi.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.*;
import org.apache.http.impl.client.*;
import org.apache.http.protocol.*;
import org.apache.http.util.*;

import org.apache.commons.io.IOUtils;

public class HttpClientConnection implements WebConnection
{
    private static final ResponseHandler<String> HANDLER = new BasicResponseHandler();
    private HttpClient client;

    private class Response extends WebResponse
    {
        private final HttpContext context;
        private final String responseBody;
        private final URI finalUrl;

        public Response(HttpUriRequest request) throws IOException
        {
            context = new BasicHttpContext();
            responseBody = client.execute(request, HANDLER, context);
            finalUrl = HttpClientConnection.getFinalUrl(request, context);
        }

        @Override
        public String getFinalUrl()
        {
            return finalUrl.toString();
        }

        @Override
        public String getResponseBody()
        {
            return responseBody;
        }
    }

    public HttpClientConnection()
    {
        client = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
    }

    @Override
    public WebResponse execute(WebRequest request) throws IOException
    {
        // *NYI*
        return null;
    }

    @Override
    public void saveCookies()
    {
        // No need to do anything, cookies already handled
    }

    private static URI getFinalUrl(HttpUriRequest request, HttpContext context)
    {
        URI finalUrl = request.getURI();
        RedirectLocations locations = (RedirectLocations)context.getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
        if (locations != null) {
            finalUrl = locations.getAll().get(locations.getAll().size() - 1);
        }
        return finalUrl;
    }
}
