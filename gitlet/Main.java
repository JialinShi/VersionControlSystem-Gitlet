package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Jialin Shi
 */


import static gitlet.Utils.*;
import static gitlet.AdditionalUtils.*;

public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            exit("Please enter a command.");
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                ifArgValid(args, 1);
                Repository.init();
                break;
            case "add":
                Repository.checkCWD();
                ifArgValid(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                Repository.checkCWD();
                ifArgValid(args, 2);
                if (args[1].length() == 0) {
                    exit("Please enter a commit message.");
                }
                Repository.commit(args[1]);
                break;
            case "checkout":
                Repository.checkCWD();
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        exit("Incorrect operands.");
                    }
                    Repository.checkoutFileName(args[2]);
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        exit("Incorrect operands.");
                    }
                    Repository.checkoutCommitID(args[1], args[3]);
                } else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else {
                    AdditionalUtils.exit("Incorrect operands.");
                }
                break;
            case "status":
                Repository.checkCWD();
                ifArgValid(args, 1);
                Repository.status();
                break;
            case "log":
                Repository.checkCWD();
                ifArgValid(args, 1);
                Repository.log();
                break;
            case "branch":
                Repository.checkCWD();
                ifArgValid(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                Repository.checkCWD();
                ifArgValid(args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "rm":
                Repository.checkCWD();
                ifArgValid(args, 2);
                Repository.rm(args[1]);
                break;
            case "global-log":
                Repository.checkCWD();
                ifArgValid(args, 1);
                Repository.globalLog();
                break;
            case "find":
                Repository.checkCWD();
                ifArgValid(args, 2);
                Repository.find(args[1]);
                break;
            case "reset":
                Repository.checkCWD();
                ifArgValid(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                Repository.checkCWD();
                ifArgValid(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                exit("No command with that name exists.");
        }
    }

    public static void ifArgValid(String[] args, int i) {
        if (args.length != i) {
            AdditionalUtils.exit("Incorrect operands.");
        }
    }

}
