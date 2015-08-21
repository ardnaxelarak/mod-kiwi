package modkiwi.games;

import modkiwi.data.GameInfo;
import modkiwi.util.Logger;

import java.io.IOException;

public class BotManager
{
    private static final Logger LOGGER = new Logger(BotManager.class);

    public static GameBot getBot(GameInfo game) throws IOException
    {
        String gametype = game.getGametype();
        if (gametype.equals("KNG"))
            return new BotKNG(game);
        else if (gametype.equals("TB"))
            return new BotTB(game);
        else
        {
            LOGGER.severe("Unrecognized game type '%s'", gametype);
            return null;
        }
    }
}
