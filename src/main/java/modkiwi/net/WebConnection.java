package modkiwi.net;

import java.io.IOException;

public interface WebConnection
{
    public WebResponse execute(WebRequest request) throws IOException;

    public void saveCookies();
}
