package plugin.console;

import java.util.Arrays;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleFactory implements IConsoleFactory {
	static MessageConsole console;

	static boolean containMessageConsole(IConsole[] consoles) {
		for (IConsole console : consoles) {
			if (console.equals(ConsoleFactory.console)) {
				return true;
			}
		}
		return false;
	}

	public static synchronized void appand(String str) {
		IConsoleManager consoleManager = ConsolePlugin.getDefault()
				.getConsoleManager();
		if (null == console) {
			// IConsole[] consoles = consoleManager.getConsoles();
			// IConsole[] tmp = Arrays.copyOf(consoles, consoles.length + 1);
			ConsoleFactory.console = new MessageConsole("message", null, null,
					true);
			// tmp[consoles.length] = ConsoleFactory.console;
			consoleManager
					.addConsoles(new IConsole[] { ConsoleFactory.console });
		}
		MessageConsoleStream mcs = console.newMessageStream();
		mcs.println(str);
	}

	@Override
	public void openConsole() {
	}
}
