package bot.exceptions;

/**
 * A <code>BotException</code> thrown when a user types
 * in an invalid command that cannot be parsed.
 */
public class InvalidCommandException extends BotException {
    public InvalidCommandException(String cmd) {
        super("Invalid command: " + cmd);
    }
}
