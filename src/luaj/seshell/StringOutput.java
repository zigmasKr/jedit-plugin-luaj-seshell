/*
 * StringOutput.java - Output to string implementation
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package luaj.seshell;

//{{{ Imports
import java.awt.Color;

import javax.swing.text.AttributeSet;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;

import console.*;

//}}}
/**
 *  Output to String implementation.
 *
 *  Substitute for console Output object to collect output from a script into a string.
 */
public class StringOutput implements Output {

	protected StringBuffer buf;

	//{{{ StringOutput constructors
	public StringOutput() {
		this(null,"text");
	}

	public StringOutput(Console console) {
		this(null, "text");
	}

	public StringOutput(Console console, CharSequence mode)	{
		buf = new StringBuffer();
	}//}}}

	//{{{ toString() method
	public String toString() {
		return buf.toString();
	} //}}}

	//{{{ print() method
	public void print(Color color, String msg)	{
		buf.append(msg);
		buf.append('\n');
	} //}}}

	//{{{ write() method
	public void writeAttrs(AttributeSet attrs, String msg) {
		buf.append(msg);
	} //}}}

	//{{{ Do Nothing methods
	public void setAttrs(int length, AttributeSet attrs){}
	public void commandDone(){}
	public void printColored(String message) {}
	//}}}
}
/* :folding=explicit:collapseFolds=1:tabSize=4:indentSize=4:noTabs=false: */
