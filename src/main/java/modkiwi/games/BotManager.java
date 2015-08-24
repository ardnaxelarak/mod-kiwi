package modkiwi.games;

import modkiwi.data.GameInfo;
import modkiwi.util.Logger;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class BotManager
{
    private static final Logger LOGGER = new Logger(BotManager.class);

    public static GameBot getBot(GameInfo game) throws IOException
    {
        String gametype = game.getGametype();
        String className = String.format("modkiwi.games.Bot%s", gametype);
        try
        {
            Class cls = Class.forName(className);
            Constructor constructor = cls.getDeclaredConstructor(GameInfo.class);
            return (GameBot)constructor.newInstance(game);
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.throwing("getBot()", e);
        }
        catch (NoSuchMethodException e)
        {
            LOGGER.throwing("getBot()", e);
        }
        catch (InstantiationException e)
        {
            LOGGER.throwing("getBot()", e);
        }
        catch (IllegalAccessException e)
        {
            LOGGER.throwing("getBot()", e);
        }
        catch (InvocationTargetException e)
        {
            LOGGER.throwing("getBot()", e);
        }

        return null;
    }
}
