package br.usp.icmc.OracleManager;

@FunctionalInterface
public interface ResultSetUser<T> {
	void use(T element);
}
