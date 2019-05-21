/*
 * LuaJSEShell.java
 *
 * Copyright (c) 2007 Jakub Roztocil <jakub@webkitchen.cz>
 * Copyright (c) 2009 Robert Ledger <robert@pytrash.co.uk>
 * (Authors of JavaScriptShell Plugin)
 * Copyright (c) 2017 Zigmantas Kryzius <zigmas.kr@gmail.com>
 * (JavaScriptPlugin code adapted for LuaJ script engine)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA     02111-1307, USA.
 *
 */
package luaj.seshell;

//{{{ Imports
import console.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.PrintWriter;

import java.util.List;
import javax.script.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.Macros.Handler;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.gui.TextAreaDialog;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.util.Log;
import java.awt.Color;

import org.luaj.vm2.script.*;

//}}}

//{{{ class LuaJSEShell
/**
 * A Console Shell for executing LuaJ.
 */
public class LuaJSEShell extends Shell {

	/** The scripting engine instance used to evaluate scripts.  */
	private static ScriptEngine engineLuaJ;

	//{{{ LuaJSEShell constructor
	/**
	 * Creates a new LuaJSEShell object.
	 *
	 * @param name  Name of the LuaJ SE Shell
	 */
	public LuaJSEShell(String name) {
		super(name);
	}//}}}

	//{{{ init() method

	/** Initialze the LuaJSEShell object.  */
	public static void init() {

		ScriptEngineManager scriptManager  = new ScriptEngineManager();
		Log.log(Log.DEBUG, LuaJSEShell.class, "LuaJ: scriptManager: " + scriptManager.toString());
		scriptManager.registerEngineExtension("lua", new LuaScriptEngineFactory());
		engineLuaJ = scriptManager.getEngineByExtension("lua");
		if (!(engineLuaJ == null)) {
			Log.log(Log.DEBUG, LuaJSEShell.class, "LuaJ: script engine: " + engineLuaJ.toString());
			engineLuaJ.setBindings(new SimpleBindings(), ScriptContext.ENGINE_SCOPE);
			engineLuaJ.put("engine", engineLuaJ);
			// [AWT-EventQueue-0] [debug] LuaJSEShell: LuaJ: scriptManager: javax.script.ScriptEngineManager@135848f
			// [AWT-EventQueue-0] [debug] LuaJSEShell: LuaJ: script engine: org.luaj.vm2.script.LuaScriptEngine@118a0cf
		} else {
			Log.log(Log.ERROR, LuaJSEShell.class, "LuaJ: script engine: null");
		}

	}//}}}

	//{{{ execute() method

	/**
	 * Execute an LuaJ command.<p>
	 *
	 * Called from console to execute an LuaJ command.<p>
	 *
	 * The output from print() is intercepted after passing
	 * through engine.context.writer (a PrintWriter) and sent
	 * to the output object provided.
	 *
	 * @param console  Console instance requesting execution.
	 * @param input    Not Used Yet.
	 * @param output   Console Output class to which output is to be sent.
	 * @param error    Not Used Yet, error output is always sent to output.
	 * @param command  The script to be executed.
	 */
	public void execute(Console console, String input, Output output, Output error,
			String command) {
	
		command = startsEqualityS(command);
		if (command == null || command.equals("")) {
			output.commandDone();
			return;
		}

		setGlobals(console.getView(), output, console);
		engineLuaJ.getContext().setWriter(new PrintWriter(new ShellWriter(output)));

		try {
			Object  retVal  = engineLuaJ.eval(command);
			String  result  = "";

			if (retVal != null) {
				result = retVal.toString();
				if (result != "") {
					result = result + "\n";
				}
			}
			output.writeAttrs(
				ConsolePane.colorAttributes(console.getPlainColor()),
				result
			);
		} catch (Exception e) {
			output.print(console.getErrorColor(), e.toString());
		}
		finally {
			output.commandDone();
		}
	}//}}}

	//{{{ printInfoMessage() method

	/**
	 * Prints an 'info' message when starting or clearing a shell.<p>
	 *
	 * The message is taken from property luaj.seshell.info and is only
	 * displayed if the property luaj.seshell.info.toggle is set to true.
	 *
	 * @param output  Object to which output should be directed.
	 */
	public void printInfoMessage(Output output) {
		if (jEdit.getBooleanProperty("luaj.seshell.info.toggle", true)) {
			output.print(null, jEdit.getProperty("luaj.seshell.info"));
		}
	}//}}}

	//{{{ printPrompt() method

	/**
	 * Print an luaj.seshell prompt on an interactive shell.<p>
	 *
	 * The prompt to use is provided by the luaj.seshell.prompt property.
	 *
	 * @param console  The console to be used.
	 * @param output   Object to which output should be directed.
	 */
	public void printPrompt(Console console, Output output) {
		String  prompt  = //"\n" +
		jEdit.getProperty("luaj.seshell.prompt", "LuaJ");
		//output.writeAttrs(ConsolePane.colorAttributes(console.getWarningColor()), prompt);
		output.writeAttrs(ConsolePane.colorAttributes(Color.blue), prompt);
		output.writeAttrs(ConsolePane.colorAttributes(console.getPlainColor()), " ");
	}//}}}

	//{{{ setGlobals() methods

	/**
	 * Setup the global namespace for the shell before each evaluation.
	 *
	 * @param view     The view containing the console.
	 */
	private static void setGlobals(View view) {
		setGlobals(view, null, null);
	}
	/**
	 * Setup the global namespace for the shell before each evaluation.
	 *
	 * @param view     The view containing the console.
	 * @param output   The console Output object to which output should be directed.
	 */
	private static void setGlobals(View view, Output output){
		setGlobals(view, output, null);
	}
	/**
	 * Setup the global namespace for the shell before each evaluation.
	 *
	 * If console is not provided it will be derived from view.
	 *
	 * If output is not provided it will be derived from console.
	 *
	 * @param view     The view containing the console.
	 * @param output   The console Output object to which output should be directed.
	 * @param console  The console from which the command was invoked!
	 */
	 private static void setGlobals(View view, Output output, Console console) {

		Buffer buffer = null;

		if (view != null) {
			buffer = view.getBuffer();
			if (console == null) {
				console = ConsolePlugin.getConsole(view);
			}
			if (output == null && console != null) {
				output = console.getOutput();
			}
		}

		engineLuaJ.put("view", view);
		engineLuaJ.put("editPane", view == null ? null : view.getEditPane());
		engineLuaJ.put("textArea", view == null ? null : view.getTextArea());
		engineLuaJ.put("buffer", buffer);
		engineLuaJ.put("wm", view == null ? null : view.getDockableWindowManager());
		engineLuaJ.put("scriptPath", buffer == null ? null : buffer.getPath());

		engineLuaJ.put("console", console);
		engineLuaJ.put("output", output);

	}//}}}

	//{{{ evalSelection() method

	/** Evaluate the contents of selected text in the current buffer.  */
	public static void evalSelection() {
		View      view          = jEdit.getActiveView();
		TextArea  textArea      = view.getTextArea();
		String    selectedText  = textArea.getSelectedText();
		// console, where the engine output will be printed:
		Console   console       = ConsolePlugin.getConsole(view);
		String    engineOutput;
		
		selectedText = startsEqualityS(selectedText);

		if (selectedText == null) {
			view.getToolkit().beep();
		} else {

			RetVal  result  = evalCode(view, selectedText, true);

			if (result.error) {
				view.getToolkit().beep();
				engineOutput = result.out.toString();
				console.print(console.getErrorColor(), "\nLuaJ engine error: ");
				console.print(console.getErrorColor(), engineOutput);
				return;
			}
			Object  retVal  = result.retVal;
			if (retVal == null) {
				retVal = "";
			} else {
				retVal = retVal.toString();
			}
			engineOutput = result.out.toString() + retVal;
			console.print(console.getInfoColor(), "\nLuaJ engine output: ");
			console.print(console.getPlainColor(), engineOutput);

			String  prompt  = //"\n" +
				jEdit.getProperty("luaj.seshell.prompt", "LuaJ");
			console.getOutput().writeAttrs(ConsolePane.colorAttributes(Color.blue), prompt);
			console.getOutput().writeAttrs(ConsolePane.colorAttributes(console.getPlainColor()), " ");
		}
	}//}}}
	
	//{{{ printSelection() method

	/** Wraps into print statement the contents of selected text in the current buffer.  */
	public static void printSelection() {
		View      view          = jEdit.getActiveView();
		TextArea  textArea      = view.getTextArea();
		String    selectedText  = textArea.getSelectedText();
		// console, where the engine output will be printed:
		Console   console       = ConsolePlugin.getConsole(view);
		String    engineOutput;

		if (selectedText == null) {
			view.getToolkit().beep();
		} else {
			
			selectedText = "print(" + selectedText + ")";
			RetVal  result  = evalCode(view, selectedText, true);

			if (result.error) {
				view.getToolkit().beep();
				engineOutput = result.out.toString();
				console.print(console.getErrorColor(), "\nLuaJ engine error: ");
				console.print(console.getErrorColor(), engineOutput);
				return;
			}
			Object  retVal  = result.retVal;
			if (retVal == null) {
				retVal = "";
			} else {
				retVal = retVal.toString();
			}
			engineOutput = result.out.toString() + retVal;
			console.print(console.getInfoColor(), "\nLuaJ engine print output: ");
			console.print(console.getPlainColor(), engineOutput);

			String  prompt  = //"\n" +
				jEdit.getProperty("luaj.seshell.prompt", "LuaJ");
			console.getOutput().writeAttrs(ConsolePane.colorAttributes(Color.blue), prompt);
			console.getOutput().writeAttrs(ConsolePane.colorAttributes(console.getPlainColor()), " ");
		}
	}//}}}

	//{{{ evalBuffer() method

	/**
	 * Evaluate the entire contents of the curent buffer.
	 */
	public static void evalBuffer() {
		View  view  = jEdit.getActiveView();
		evalBuffer(view, true);
	}

	/**
	 * Evaluate the entire contents of the curent buffer.<p>
	 *
	 * Optionally show the output in a dialog box.
	 *
	 * @param view        Description of the Parameter
	 * @param showOutput  true if output is to be shown in a dialog.
	 */
	public static void evalBuffer(View view, boolean showOutput) {
		String  bufferText = view.getTextArea().getText();
		// console, where the engine output will be printed:
		Console console    = ConsolePlugin.getConsole(view);
		String  engineOutput;

		if (bufferText == null) {
			view.getToolkit().beep();
		} else {
			RetVal result = evalCode(view, bufferText, true);
			if (showOutput) {
				if (result.error) {
					view.getToolkit().beep();
					engineOutput = result.out.toString();
					console.print(console.getErrorColor(), "\nLuaJ engine error: ");
					console.print(console.getErrorColor(), engineOutput);
					return;
				}
				Object  retVal  = result.retVal;
				if (retVal == null) {
					retVal = "";
				} else {
					retVal = retVal.toString();
				}
				engineOutput = result.out.toString() + retVal;
				console.print(console.getInfoColor(), "\nLuaJ engine output: ");
				console.print(console.getPlainColor(), engineOutput);

				String  prompt  = //"\n" +
					jEdit.getProperty("luaj.seshell.prompt", "LuaJ");
				console.getOutput().writeAttrs(ConsolePane.colorAttributes(Color.blue), prompt);
				console.getOutput().writeAttrs(ConsolePane.colorAttributes(console.getPlainColor()), " ");
			}
		}
	}//}}}

	//{{{ evalSCript() method

	/**
	 * Evaluate the entire contents of the script (file).
	 * A substitution for the method runScript (removed from this code).
	 *
	 * Apparently, ss a LuaJ code chunk returns some LuaJValue,
	 * the RetVal abstraction is good for inserting .toString()
	 * where applicable to get a printable output.
	 */
	public static void evalScript(String path) {
		View  view  = jEdit.getActiveView();
		evalScript(path, view, true);
	}

	/**
	 * Evaluate the entire contents of the script (file).
	 *
	 * Optionally show the output in a dialog box.
	 *
	 * @param view        Description of the Parameter
	 * @param showOutput  true if output is to be shown in a dialog.
	 */
	public static void evalScript(String path, View view, boolean showOutput) {
		String fileText;
		// console, where the engine output will be printed:
		Console console = ConsolePlugin.getConsole(view);
		String engineOutput;

		File file = new File(path);
		if (file.exists()) {
			try {
				BufferedReader  reader  = new BufferedReader(new FileReader(file));
				StringBuffer    code    = new StringBuffer();
				Output          output  = new StringOutput();
				String          line;
				while ((line = reader.readLine()) != null) {
					code.append(line + "\n");
				}
				fileText = code.toString();

				if (fileText == null) {
					view.getToolkit().beep();
				} else {

					RetVal result = evalCode(view, fileText, true);

					if (showOutput) {
						if (result.error) {
							view.getToolkit().beep();
							engineOutput = result.out.toString();
							console.print(console.getErrorColor(), "\nLuaJ engine error: ");
							console.print(console.getErrorColor(), engineOutput);
							return;
						}
						Object retVal = result.retVal;
						if (retVal == null) {
							retVal = "";
						} else {
							retVal = retVal.toString();
						}
						engineOutput = result.out.toString() + retVal;
						console.print(console.getInfoColor(), "\nLuaJ engine output: ");
						console.print(console.getPlainColor(), engineOutput);
						String  prompt  = //"\n" +
							jEdit.getProperty("luaj.seshell.prompt", "LuaJ");
						console.getOutput().writeAttrs(ConsolePane.colorAttributes(Color.blue), prompt);
						console.getOutput().writeAttrs(ConsolePane.colorAttributes(console.getPlainColor()), " ");
					}
				}
			} catch (Exception e) {
				Log.log(Log.ERROR, LuaJSEShell.class, e.toString());
				new TextAreaDialog(view, "luaj-error", e);
			}
		}
	}
	//}}}

	//{{{ evalStartup() method
	/**
	 * Evaluates LuaJ scripts in the startup folder.
	 * @param view       the jEdit view from which this method was invoked.
	 */
	public static void evalStartup(View view) {
		String strSettings = MiscUtilities.constructPath(jEdit.getSettingsDirectory(), "startup/");
		File startupSett = new File(strSettings);
		File [] scripts;
		String strHome = MiscUtilities.constructPath(jEdit.getJEditHome(), "startup/");
		File startupHome = new File(strHome);

		scripts = startupSett.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".lua");
				}
		});
		for (File script : scripts) {
			String scriptPath = script.getAbsolutePath();
			evalScript(scriptPath);
			Log.log(Log.DEBUG, LuaJSEShell.class, "LuaJ script loaded: " + scriptPath);
		}

		scripts = startupHome.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".lua");
				}
		});
		for (File script : scripts) {
			String scriptPath = script.getAbsolutePath();
			evalScript(scriptPath);
			Log.log(Log.DEBUG, LuaJSEShell.class, "LuaJ script loaded: " + scriptPath);
		}

	} //}}}

	//{{{ evalCode() method
	/**
	 * Evaluate LuaJ code, collect output and return the result.
	 *
	 * @param view     the jEdit view from which this command was invoked.
	 * @param command  the LuaJ code to be evaluated.
	 * @return         RetVal instance containing result, output and error information.
	 */
	public static RetVal evalCode(View view, CharSequence command) {
		return evalCode(view, command, true);
	}


	/**
	 * Evaluate LuaJ code, collect output and return the result.
	 *
	 * @param command    the LuaJ code to be evaluated.
	 * @param view       the jEdit view from which this command was invoked.
	 * @param showError  set to true if errors are to be shown in a dialog.
	 * @return           RetVal instance containing result, output and error information.
	 */
	public static RetVal evalCode(View view, CharSequence command, boolean showError) {

		StringOutput  output  = new StringOutput();
		Object        retVal  = null;
		
		//command = startsEqualityC(command);

		if (command == null || command.equals("")) {
			return new RetVal("", "");
		}

		setGlobals(view, output);
		engineLuaJ.getContext().setWriter(new PrintWriter(new ShellWriter(output)));

		try {
			retVal = engineLuaJ.eval(command.toString());
		} catch (ScriptException e) {
			Log.log(Log.ERROR, LuaJSEShell.class, e.toString());

			if (showError) {
				new TextAreaDialog(view, "luaj-error", e);
			}

			return new RetVal(e, output.toString(), true, showError);
		}

		return new RetVal(retVal, output.toString());
	}//}}}

	//{{{ RetVal class
	/**
	 * Encapsulates the return value for the evaluateCode method.
	 */
	public static class RetVal {

		/** Flag set to true if an error dialog has been shown.*/
		public boolean errorShown;

		/** Flag set to true if an error occured */
		public boolean error;

		/** A CharSequence representing the generated output. */
		public CharSequence out;

		/** The object returned by the clojure script engine after evaluation. */
		public Object retVal;


		/**
		 * Creates a new RetVal object. This class contains the result of executing a script.
		 *
		 * @param retVal  the result of evaluating the script.
		 * @param out     the output produced by the script.
		 */
		public RetVal(Object retVal, CharSequence out) {
			this(retVal, out, false, true);
		}


		/**
		 * Creates a new RetVal object. This class contains the result of executing a script.
		 *
		 * @param retVal      the result of evaluating the script.
		 * @param out         the output produced by the script.
		 * @param error       true if an error occured while executing the script.
		 * @param errorShown  true if the error
		 */
		public RetVal(Object retVal, CharSequence out, boolean error, boolean errorShown) {
			this.error = error;
			this.out = out;
			this.retVal = retVal;
			this.errorShown = errorShown;
		}
	}//}}}
	
	//{{{ startsEqualityS 
	protected static String startsEqualityS(String code) {
		boolean isEq = false;
		while ((code.charAt(0) == 32) || (code.charAt(0) == 61)) {
			if (code.charAt(0) == 61) {
				isEq = true;
			}
			code = code.substring(1);
		}
		if (isEq) {
			code = "print(" + code + ")";
		}
		return code;
	} 
	//}}}
}

/*
 * :folding=explicit:collapseFolds=1:tabSize=4:indentSize=4:noTabs=false:
 */

