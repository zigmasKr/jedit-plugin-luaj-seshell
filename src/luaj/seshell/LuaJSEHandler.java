/*
 * LuaJSEHandler.java
 * Copyright (C) 2017 Zigmantas Kryzius <zigmas.kr@gmail.com>
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
 *
 */

package luaj.seshell;

//{{{ imports
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.Macros.*;

import java.nio.file.Path;
import java.nio.file.Paths;
//}}}

public class LuaJSEHandler extends Handler {

	//{{{ LuaJSEHandler constructors
	public LuaJSEHandler(String name) {
		super(name);
	}//}}}

	//{{{ accept method
	public boolean accept(String path) {
		return path.endsWith(".lua");
	}//}}}

	//{{{
	public String macroNameShort(String pathStr) {
		Path path = Paths.get(pathStr);
		String fname = path.getFileName().toString();
		fname = fname.replaceAll("_", " ");
		return fname.substring(0, fname.length() - 4);
	}
	//}}}

	//{{{ creatMacro method
	public Macro createMacro(String macroName, String path) {
		//String name = path.substring(0, path.length() - 4);
		String name = macroNameShort(path);
		return new Macro(this,
						name,
						Macro.macroNameToLabel(name),
						path);
	}//}}}

	//{{{ runMacro method
	public void runMacro(View view, Macro macro) {
		Log.log(Log.DEBUG, this, "runMacro " + macro.getPath());
		// here comes the method from LuaJSEShell for evaluating script (file):
		LuaJSEShell.evalScript(macro.getPath());
	}//}}}

	//{{{ evalCode method
	public LuaJSEShell.RetVal evalCode(View view, CharSequence command) {
		Log.log(Log.DEBUG, this, "evalCode");
		return LuaJSEShell.evalCode(view, command);
	}//}}}

	//{{{ getName method
	public String getName() {
		return "LuaJSEHandler";
	}//}}}

	//{{{ getLabel method
	public String getLabel() {
		return "LuaJ script";
	}//}}}

}
/* :folding=explicit:collapseFolds=1:tabSize=4:indentSize=4:noTabs=false: */
