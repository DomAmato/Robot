package com.dyn.robot.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dyn.DYNServerMod;
import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.CodeGen;
import com.dyn.robot.code.CodeGenResult;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.entity.brain.DynRobotBrain;
import com.dyn.robot.programs.Program;
import com.dyn.robot.programs.UserProgramLibrary;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;

public class DynApi implements ILuaAPI {
	private static IMount s_autorunMount = null;
	private static IMount s_programsMount = null;
	private final IAPIEnvironment m_environment;
	private final IDYNRobotAccess m_turtle;

	public DynApi(IAPIEnvironment environment, IDYNRobotAccess turtle) {
		m_environment = environment;
		m_turtle = turtle;
	}

	@Override
	public void advance(double _dt) {
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] arguments)
			throws LuaException, InterruptedException {
		switch (method) {
		case 0: {
			final String title = (arguments.length >= 1) && ((arguments[0] instanceof String)) ? (String) arguments[0]
					: null;
			context.executeMainThreadTask(() -> {
				Program program = null;
				if (title != null) {
					UserProgramLibrary library = m_turtle.getProgramLibrary();
					if (library != null) {
						int programIndex = library.lookupProgramByTitle(title);
						if (programIndex >= 0) {
							program = library.getProgram(programIndex);
						}
					}
				} else {
					program = m_turtle.getProgram();
				}
				if (program != null) {
					return new Object[] { Boolean.valueOf(true) };
				}
				return new Object[] { Boolean.valueOf(false) };
			});
		}
		case 1: {
			String title = null;
			CodeBuilder.AnnotationOptions annotationOptions = CodeBuilder.AnnotationOptions.None;
			if ((arguments.length >= 1) && ((arguments[0] instanceof String))) {
				title = (String) arguments[0];
			}
			if ((arguments.length >= 2) && ((arguments[1] instanceof String))) {
				String name = (String) arguments[1];
				if (name.equals("full")) {
					annotationOptions = CodeBuilder.AnnotationOptions.Full;
				} else if (name.equals("subprogram")) {
					annotationOptions = CodeBuilder.AnnotationOptions.SubProgram;
				}
			}
			final String finalTitle = title;
			final CodeBuilder.AnnotationOptions finalAnnotationOptions = annotationOptions;
			context.executeMainThreadTask(() -> {
				Program program = null;
				if (finalTitle != null) {
					UserProgramLibrary library = m_turtle.getProgramLibrary();
					if (library != null) {
						int programIndex = library.lookupProgramByTitle(finalTitle);
						if (programIndex >= 0) {
							program = library.getProgram(programIndex);
						}
					}
				} else {
					program = m_turtle.getProgram();
				}
				if (program != null) {
					CodeGenResult result = CodeGen.compile(program, finalAnnotationOptions);
					if (result.getSuccess()) {
						Program compiledProgram = result.getProgram();
						Map<Object, Object> codeTable = new HashMap(compiledProgram.getLength());
						for (int i1 = 0; i1 < compiledProgram.getLength(); i1++) {
							String line = compiledProgram.getLine(i1);
							codeTable.put(Integer.valueOf(i1 + 1), line);
						}
						return new Object[] { Boolean.valueOf(true), codeTable };
					}
					List<SyntaxError> errors = result.getErrors();
					Map<Object, Object> errorsTable = new HashMap(errors != null ? errors.size() : 0);
					int i2;
					if (errors != null) {
						i2 = 1;
						for (SyntaxError error : errors) {
							Map<Object, Object> errorTable = new HashMap(4);
							errorTable.put("message", error.getMessage());
							errorTable.put("slot", Integer.valueOf(error.getRange().getStartLine() + 1));
							errorsTable.put(Integer.valueOf(i2), errorTable);
							i2++;
						}
					}
					return new Object[] { Boolean.valueOf(false), errorsTable };
				}
				Map<Object, Object> emptyTable = new HashMap(0);
				return new Object[] { Boolean.valueOf(true), emptyTable };
			});
		}
		case 2:
			context.issueMainThreadTask(() -> {
				m_turtle.setProgramStopped();
				return null;
			});
			return null;
		case 3:
			context.issueMainThreadTask(() -> {
				m_turtle.setProgramRunning();
				return null;
			});
			return null;
		case 4:
			context.issueMainThreadTask(() -> {
				m_turtle.setProgramPaused();
				return null;
			});
			return null;
		case 5:
			if ((arguments.length < 1) || (!(arguments[0] instanceof String))) {
				throw new LuaException("Expected string");
			}
			final String message = (String) arguments[0];
			context.issueMainThreadTask(() -> {
				m_turtle.setProgramErrored(message);
				return null;
			});
			return null;
		case 6:
			final int slot = parseSlotNumber(arguments);
			context.issueMainThreadTask(() -> {
				m_turtle.setProgramSlot(slot);
				return null;
			});
			return null;
		case 7:
			context.executeMainThreadTask(() -> {
				int slot1 = m_turtle.getProgramSlot();
				if (slot1 >= 0) {
					return new Object[] { Integer.valueOf(slot1 + 1) };
				}
				return null;
			});
		case 8:
			if ((arguments.length < 1) || (!(arguments[0] instanceof Map))) {
				throw new LuaException("Expected table");
			}
			Map contentTable = (Map) arguments[0];
			final List<String> lines = new ArrayList();
			for (int i = 1; contentTable.containsKey(Double.valueOf(i)); i++) {
				String line = contentTable.get(Double.valueOf(i)).toString();
				lines.add(line);
			}
			context.issueMainThreadTask(() -> {
				Program program = m_turtle.getProgram();
				if (program != null) {
					program.clear();
					program.addLines(lines);
					((DynRobotBrain) m_turtle).updateProgramLibrary();
					program.save();
				}
				return null;
			});
			return null;
		case 9:
			context.issueMainThreadTask(() -> {
				Program program = m_turtle.getProgram();
				if (program != null) {
					program.clear();
					((DynRobotBrain) m_turtle).updateProgramLibrary();
					program.save();
				}
				return null;
			});
			return null;
		case 10: {
			if ((arguments.length < 2) || (!(arguments[0] instanceof String)) || (!(arguments[1] instanceof String))) {
				throw new LuaException("Expected string, string");
			}
			final String variable = (String) arguments[0];
			final String value = (String) arguments[1];
			context.issueMainThreadTask(() -> {
				m_turtle.setVariable(variable, value);
				return null;
			});
			return null;
		}
		case 11: {
			if ((arguments.length < 1) || (!(arguments[0] instanceof String))) {
				throw new LuaException("Expected string");
			}
			final String variable = (String) arguments[0];
			context.issueMainThreadTask(() -> {
				m_turtle.clearVariable(variable);
				return null;
			});
			return null;
		}
		case 12:
			context.issueMainThreadTask(() -> {
				m_turtle.clearVariables();
				return null;
			});
			return null;
		case 13:
			return m_turtle.executeCommand(context, new TurtleSaveCommand());
		case 14:
			return m_turtle.executeCommand(context, new TurtleRestoreCommand());
		case 15:
			context.executeMainThreadTask(() -> {
				Map<Object, Object> results = new HashMap();
				UserProgramLibrary library = m_turtle.getProgramLibrary();
				if (library != null) {
					for (int i = 0; i < library.getSize(); i++) {
						results.put(Integer.valueOf(i + 1), library.getProgram(i).getTitle());
					}
				}
				return new Object[] { results };
			});
		case 16:
			return new Object[] {
					Boolean.valueOf(false/* dynrobot.stopTurtles */) };
		}
		return null;
	}

	@Override
	public String[] getMethodNames() {
		return new String[] { "hasProgram", "getProgram", "setStopped", "setRunning", "setPaused", "setErrored",
				"setCurrentSlot", "getCurrentSlot", "setManualProgram", "clearManualProgram", "setVariable",
				"clearVariable", "clearVariables", "saveTurtleState", "restoreTurtleState", "getLibraryPrograms",
				"areTurtlesStopped" };
	}

	@Override
	public String[] getNames() {
		return new String[] { "dyn" };
	}

	private int parseSlotNumber(Object[] arguments) throws LuaException {
		if ((arguments.length < 1) || (arguments[0] == null)) {
			return -1;
		}
		if (!(arguments[0] instanceof Number)) {
			throw new LuaException("Expected number");
		}
		int line = ((Number) arguments[0]).intValue();
		int limit = 264;
		if ((line < 1) || (line > limit)) {
			throw new LuaException("Expected number in range 1-" + limit);
		}
		return line - 1;
	}

	@Override
	public void shutdown() {
		FileSystem fileSystem = m_environment.getFileSystem();
		synchronized (fileSystem) {
			fileSystem.unmount("rom/autorun/dyn");
			fileSystem.unmount("rom/programs/dyn");
		}
		m_turtle.setProgramStopped();
		m_turtle.clearVariables();
		m_turtle.setProgramSlot(-1);
	}

	@Override
	public void startup() {
		m_turtle.setProgramStopped();
		m_turtle.clearVariables();
		m_turtle.setProgramSlot(-1);

		FileSystem fileSystem = m_environment.getFileSystem();
		synchronized (fileSystem) {
			try {
				if (s_autorunMount == null) {
					s_autorunMount = ComputerCraftAPI.createResourceMount(DYNServerMod.class, "dyn",
							"lua/rom/autorun/dyn");
				}
				if (s_autorunMount != null) {
					fileSystem.mount("rom", "rom/autorun/dyn", s_autorunMount);
				}
			} catch (FileSystemException e) {
			}
			try {
				if (s_programsMount == null) {
					s_programsMount = ComputerCraftAPI.createResourceMount(DYNServerMod.class, "dyn",
							"lua/rom/programs/dyn");
				}
				if (s_programsMount != null) {
					fileSystem.mount("rom", "rom/programs/dyn", s_programsMount);
				}
			} catch (FileSystemException e) {
			}
		}
	}
}
