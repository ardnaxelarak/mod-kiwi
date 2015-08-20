package modkiwi;

import modkiwi.data.ArticleInfo;
import modkiwi.data.ThreadInfo;
import modkiwi.net.GAEConnection;
import modkiwi.net.NetConnection;
import modkiwi.net.WebResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length < 3)
        {
            System.err.println("Invalid arguments supplied.");
            return;
        }

        WebResponse response;

        PrintWriter pw = new PrintWriter(System.out, true);
        try
        {
            System.err.println("fish?");

            pw.println("Creating Helper...");
            Helper h = new Helper(new NetConnection(pw));

            pw.println("Logging in...");
            response = h.login("modkiwi", "modkiwi157");
            pw.println(response.getFinalUrl());

            pw.println("Sending geekmail...");
            response = h.geekmail(args[0], args[1], args[2]);
            pw.println(response.getFinalUrl());

            pw.println("geekmail sent...");
            /*
            h.replyThread("1140569", null, "huh?");
            ThreadInfo t = h.getThread("1140569");
            ArticleInfo[] a = t.getArticles();
            System.out.println(a[a.length - 1].getBody());
            */
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            pw.flush();
            pw.close();
        }
    }
}
