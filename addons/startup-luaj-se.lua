
-- startup utils and useful functions for LuaJ SE Shell plugin

local macros = luajava.bindClass("org.gjt.sp.jedit.Macros")
local jedit = luajava.bindClass("org.gjt.sp.jedit.jEdit")
local joptionPane = luajava.bindClass("javax.swing.JOptionPane")

function Alert(text)
	-- Shows alert message
	macros:message(view, text)
end

-- Alert("auuu")

function Prompt(question, defaultValue)
	-- Shows a prompt box to the user and returns the answer
	return macros:input(view, question, defaultValue)
end
-- str = Prompt("Insert value", "MIAU")
-- print(str)

function Confirm(question)
	-- Shows a confirmation message box to the user.
	-- https://docs.oracle.com/javase/7/docs/api/javax/swing/JOptionPane.html
	-- YES returns: 0; NO returns: 1; CANCEL retruns: 2.
	buttons = joptionPane.YES_NO_CANCEL_OPTION
	return macros:confirm(view, question, buttons)
end
-- a = Confirm("if?")
-- print(a)

function PrintBuf(anything)
	-- Prints anything to new buffer.
	newbuffer = jedit:newFile(view)
	newbuffer:insert(0, anything)
end
-- PrintBuf("kukuku")

