/*
 * ShellWriter.java
 *
 * Copyright (c) 2009 Robert Ledger <robert@pytrash.co.uk>
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

 //{{{ imports
import java.io.*;
import console.*;
//}}}

/**
 * A character stream that sends its output to a console.Output object.<p>
 *
 * This class is used to intercept the output from the engine.writer.
 */
public class ShellWriter extends Writer {

	protected Output output;

	//{{{ ShellWriter constuctors
	/**
	 * Create a new writer using the specified console.Output as a target.
	 *
	 * @param output
	 *		  The console output object to which output should be sent.
	 */
	public ShellWriter(Output output) {
		this.output = output;
		lock = output;
	}
	//}}}

	//{{{ write method
	/**
	 * Write a portion of an array of characters.
	 *
	 * @param  cbuf	 Array of characters
	 * @param  off	 Offset from which to start writing characters
	 * @param  len	 Number of characters to write
	 */
	public void write(char cbuf[], int off, int len) {
		output.writeAttrs(null, new String(cbuf, off, len));
	}//}}}

	//{{{ getOutput method
	/**
	 * Get the underlying output object.
	 *
	 * @return Output The underlying output object.
	 */
	public Output getOutput() {
		return output;
	}//}}}

	//{{{ Do Nothing methods
	public void flush() throws IOException {}
	public void close() throws IOException {}
	//}}}
}
/* :folding=explicit:collapseFolds=1:tabSize=4:indentSize=4:noTabs=false: */
