package openmods.calc;

import openmods.calc.executable.IExecutable;
import openmods.calc.symbol.CompiledFunction;
import openmods.calc.utils.config.ConfigPropertyManager;
import java.util.Set;

public class Calculator<E, M> {

	public final Environment<E> environment;

	public final Compilers<E, M> compilers;

	public final IValuePrinter<E> printer;

	@SuppressWarnings("rawtypes")
	private final ConfigPropertyManager<IValuePrinter> printerConfig;

	public Calculator(Environment<E> environment, Compilers<E, M> compilers, IValuePrinter<E> printer) {
		this.environment = environment;
		this.compilers = compilers;
		this.printer = printer;
		this.printerConfig = ConfigPropertyManager.getFor(printer.getClass());
	}

	public Set<String> getProperties() {
		return printerConfig.keys();
	}

	public String getProperty(String key) {
		return printerConfig.get(printer, key);
	}

	public Object getPropertyRaw(String key) {
		return printerConfig.getRaw(printer, key);
	}

	public void setProperty(String key, String value) {
		printerConfig.set(printer, key, value);
	}

	public void setPropertyRaw(String key, Object value) {
		printerConfig.setRaw(printer, key, value);
	}

	public void compileAndExecute(M exprType, String expr) {
		final IExecutable<E> executable = compilers.compile(exprType, expr);
		environment.execute(executable);
	}

	public E compileExecuteAndPop(M exprType, String expr) {
		final IExecutable<E> executable = compilers.compile(exprType, expr);
		return environment.executeAndPop(executable);
	}

	public String compileExecuteAndPrint(M exprType, String expr) {
		final E result = compileExecuteAndPop(exprType, expr);
		return printer.repr(result);
	}

	public E compileAndSetGlobalSymbol(M exprType, String id, String expr) {
		final E value = compileExecuteAndPop(exprType, expr);
		environment.setGlobalSymbol(id, value);
		return value;
	}

	public void compileAndDefineGlobalFunction(M exprType, String id, int argCount, String bodyExpr) {
		final IExecutable<E> funcBody = compilers.compile(exprType, bodyExpr);
		environment.setGlobalSymbol(id, new CompiledFunction<E>(argCount, 1, funcBody, environment.topFrame()));
	}

}
