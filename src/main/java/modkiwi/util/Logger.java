package modkiwi.util;

public class Logger
{
    private java.util.logging.Logger logger;
    private String sourceClass;

    public Logger(Class cl)
    {
        sourceClass = cl.getName();
        logger = java.util.logging.Logger.getLogger(sourceClass);
    }

    public void finest(String message)
    {
        logger.finest(message);
    }

    public void finest(String message, Object... args)
    {
        finest(String.format(message, args));
    }

    public void finer(String message)
    {
        logger.finer(message);
    }

    public void finer(String message, Object... args)
    {
        finer(String.format(message, args));
    }

    public void fine(String message)
    {
        logger.fine(message);
    }

    public void fine(String message, Object... args)
    {
        fine(String.format(message, args));
    }

    public void config(String message)
    {
        logger.config(message);
    }

    public void config(String message, Object... args)
    {
        config(String.format(message, args));
    }

    public void info(String message)
    {
        logger.info(message);
    }

    public void info(String message, Object... args)
    {
        info(String.format(message, args));
    }

    public void warning(String message)
    {
        logger.warning(message);
    }

    public void warning(String message, Object... args)
    {
        warning(String.format(message, args));
    }

    public void severe(String message)
    {
        logger.severe(message);
    }

    public void severe(String message, Object... args)
    {
        severe(String.format(message, args));
    }

    public void throwing(String sourceMethod, Throwable thrown)
    {
        logger.throwing(sourceClass, sourceMethod, thrown);
    }
}
