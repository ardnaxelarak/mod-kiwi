package modkiwi.data;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class BGGUserInfo
{
    private String id, name;
    private String firstname, lastname, avatarlink;

    public BGGUserInfo(Element node)
    {
        id = node.attr("id");
        name = node.attr("name");
        firstname = node.select("firstname").first().attr("value");
        lastname = node.select("lastname").first().attr("value");
        avatarlink = node.select("avatarlink").first().attr("value");
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getFirstName()
    {
        return firstname;
    }

    public String getLastName()
    {
        return lastname;
    }

    public String getAvatarLink()
    {
        return avatarlink;
    }
}
