package bot;

import bot.exceptions.*;
import bot.tasks.*;
import bot.utils.Formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bot {
    private static final List<Task> tasks = new ArrayList<>();

    private enum Command {
        LIST("list"),
        MARK("mark"),
        UNMARK("unmark"),
        DELETE("delete"),
        BYE("bye"),
        TODO("todo"),
        DEADLINE("deadline"),
        EVENT("event");

        public final String name;
        Command(String name) {
            this.name = name;
        }

        public static Command fromString(String input) throws UnknownCommandException {
            for (Command cmd : Command.values()) {
                if (cmd.name.equals(input)) {
                    return cmd;
                }
            }
            throw new UnknownCommandException(input);
        }
    }

    public static void main(String[] args) {
        printBotMessage("Hello! I'm ChadGPT. What can I do for you?");

        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            handleInput(sc.nextLine());
        }
    }

    private static void handleInput(String input) {
        Pattern regex = Pattern.compile("(\\w+)\\s*(.*)");
        Matcher matcher = regex.matcher(input);
        if (matcher.matches()) {
            String cmd = matcher.group(1);
            String args = matcher.group(2);

            Command cmdEnum;
            try {
                cmdEnum = Command.fromString(cmd);
            } catch (UnknownCommandException e) {
                printBotMessage(e.getMessage());
                return;
            }

            try {
                switch (cmdEnum) {
                    case LIST:
                        handleList();
                        break;
                    case TODO, DEADLINE, EVENT:
                        handleAddTask(cmd, args);
                        break;
                    case MARK:
                        handleMarkTask(args);
                        break;
                    case UNMARK:
                        handleUnmarkTask(args);
                        break;
                    case DELETE:
                        handleDeleteTask(args);
                        break;
                    case BYE:
                        printBotMessage("Bye. Hope to see you again soon!");
                        System.exit(0);
                    default:
                        // This should never happen
                        printBotMessage("Command not found");
                }
            } catch (BotException e) {
                printBotMessage(e.getMessage());
            }
        } else {
            // This should never happen
            printBotMessage("Command not found");
        }
    }

    private static void handleList() {
        printBotMessage("Here are the tasks in your list:\n" + Formatter.formatList(tasks));
    }

    private static void handleAddTask(String cmd, String args) throws InvalidTaskDescriptionException {
        if (cmd.equals(Command.TODO.name)) {
            if (args.isEmpty()) {
                throw new EmptyTodoException();
            }
            tasks.add(new Todo(args));
        } else if (cmd.equals(Command.DEADLINE.name)) {
            Pattern regex = Pattern.compile("(.*)\\s/by\\s(.*)");
            Matcher matcher = regex.matcher(args);
            if (matcher.matches()) {
                String task = matcher.group(1);
                String deadline = matcher.group(2);
                tasks.add(new Deadline(task, deadline));
            } else {
                throw new InvalidTaskDescriptionException(args);
            }
        } else {
            // Last command is guaranteed to be "event" by the switch statement
            Pattern regex = Pattern.compile("(.*)\\s/from\\s(.*)\\s/to\\s(.*)");
            Matcher matcher = regex.matcher(args);
            if (matcher.matches()) {
                String task = matcher.group(1);
                String from = matcher.group(2);
                String to = matcher.group(3);
                tasks.add(new Event(task, from, to));
            } else {
                throw new InvalidTaskDescriptionException(args);
            }
        }
        Task newTask = tasks.get(tasks.size()-1);
        String response = String.format(
                "Got it. I've added this task:\n  %s\nNow you have %d task(s) in the list.",
                newTask.toString(),
                tasks.size()
        );
        printBotMessage(response);
    }

    private static void handleDeleteTask(String args) throws InvalidTaskIdException {
        int index = getTaskIndex(args);
        if (index < 0 || index > tasks.size()-1) {
            throw new InvalidTaskIdException(index+1);
        }
        Task taskToDelete = tasks.get(index);
        tasks.remove(taskToDelete);

        String response = String.format(
                "Noted. I've removed this task:\n  %s\nNow you have %d task(s) in the list.",
                taskToDelete.toString(),
                tasks.size()
        );
        printBotMessage(response);
    }

    private static void handleMarkTask(String args) throws InvalidTaskIdException {
        int index = getTaskIndex(args);
        if (index < 0 || index > tasks.size()-1) {
            throw new InvalidTaskIdException(index+1);
        }
        tasks.get(index).markAsDone();
        printBotMessage("Nice! I've marked this task as done:\n" + tasks.get(index));
    }

    private static void handleUnmarkTask(String args) throws InvalidTaskIdException {
        int index = getTaskIndex(args);
        if (index < 0 || index > tasks.size()-1) {
            throw new InvalidTaskIdException(index+1);
        }
        tasks.get(index).markAsIncomplete();
        printBotMessage("OK, I've marked this task as not done yet:\n" + tasks.get(index));
    }

    private static int getTaskIndex(String input) {
        return Integer.parseInt(input.split(" ")[0]) - 1;
    }

    /**
     * Prints a formatted bot message
     *
     * @param msg the string message to be printed
     */
    private static void printBotMessage(String msg) {
        System.out.println(Formatter.formatBotMessage(msg));
    }
}
