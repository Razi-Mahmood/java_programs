package gitlet;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Razi Mahmood
 */
public class Main {
	private static List<String> commandlist = Arrays.asList("init", "add",
			"commit", "merge", "log", "branch", "checkout", "rm",
			"rm-branch", "global-log", "find", "status", "reset", "merge");
	private static HashSet<String> _known_commands = new HashSet<>(commandlist);

	/** Checks for incorrect operands.
	 * @param command a string
	 * @return boolean
	 */
	static boolean incorrectOperands(String command) {
		return false;
	}
	/** Checks if the gitlet dir exists.
	 * @return boolean
	 */
	static boolean gitletExists() {
		File dir = new File(System.getProperty("user.dir"));
		File gitletdir = new File(dir.getAbsolutePath() + "/.gitlet");
		return gitletdir.exists();
	}
	/** Creates a new gitlet dir.*/
	static void createGitletDir() {
		File dir = new File(System.getProperty("user.dir"));
		File gitletdir = new File(dir.getAbsolutePath() + "/.gitlet");
		if (!gitletdir.exists()) {
			gitletdir.mkdir();
		}
	}
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
	public static void main(String... args) {
		GitLetHandler gops = new GitLetHandler();
		try {
			if (args.length < 1) {
				throw new GitletException("Enter a command");
			} else {
				String command = args[0];
				if (!_known_commands.contains(command)) {
					throw new GitletException("No command with that "
							+ "name exists");
				} else if (incorrectOperands(command)) {
					throw new GitletException("Incorrect operands");
				} else if (command.equals("init")) {
					if (!gitletExists()) {
						createGitletDir();
						gops.doInit();
					} else {
						throw new GitletException("A Gitlet version-control "
								+ "system already exists in "
								+ "the current directory.");
					}
				} else if (!gitletExists()) {
					throw new GitletException("Not in an "
							+ "initialized Gitlet directory");
				} else if (command.equals("add")) {
					String filename = args[1];
					gops.doAdd(filename);
				} else if (command.equals("commit")) {
					String message = args[1];
					gops.doCommit(message);
				} else if (command.equals("log")) {
					gops.doLog();
				} else if (command.equals("global-log")) {
					gops.doGlobalLog();
				} else if (command.equals("branch")) {
					String branchname = args[1];
					gops.doBranch(branchname);
				} else if (command.equals("rm")) {
					String filename = args[1];
					gops.doRm(filename);
				} else if (command.equals("rm-branch")) {
					String branchname = args[1];
					gops.doRmBranch(branchname);
				} else if (command.equals("status")) {
					gops.doStatus();
				} else if (command.equals("find")) {
					String commitMessage = args[1];
					gops.doFind(commitMessage);
				} else if (command.equals("reset")) {
					String commitCode = args[1];
					gops.doReset(commitCode);
				} else if (command.equals("merge")) {
					String branchname = args[1];
					gops.doMerge(branchname);
				} else if (command.equals("checkout")) {
					if (args.length > 3)	 {
						String commitcode = args[1];
						String operand = args[2];
						if (!operand.equals("--")) {
							throw new GitletException("Incorrect operands.");
						}
						String filename = args[3];
						gops.doCheckoutCommitCode(commitcode, filename);
					} else if (args.length > 2) {
						String file = args[2];
						gops.doCheckoutFile(file);
					} else if (args.length > 1) {
						String branch = args[1];
						gops.doCheckoutBranch(branch);
					}
				}
			}
		} catch (GitletException e) {
			System.out.println(e.getMessage());
		}
		System.exit(0);
	}
}


